package kr.rea.keep;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.sql.*;
import static org.assertj.core.api.Assertions.*;
class ArchiveMigrationTest {
 @Test void migrationPreservesExistingRowAndDefaultsToUnarchived() throws Exception {
  // H2에서 실제 변경 SQL을 실행. MySQL DDL 검증을 대체하지 않음.
  try (var connection=DriverManager.getConnection("jdbc:h2:mem:migration;MODE=MySQL","sa","");
       var sql=connection.createStatement()) {
   sql.execute("CREATE TABLE rea_app_notes (id BIGINT PRIMARY KEY,title VARCHAR(200),body TEXT,created_at DATETIME(6),updated_at DATETIME(6))");
   sql.execute("INSERT INTO rea_app_notes VALUES (1,'기존 제목','기존 본문','2026-01-01 01:02:03','2026-02-02 04:05:06')");
   sql.execute(Files.readString(Path.of("db/002-add-archived.sql")));
   try (var rows=sql.executeQuery("SELECT * FROM rea_app_notes")) {
    assertThat(rows.next()).isTrue();
    assertThat(rows.getBoolean("archived")).isFalse();
    assertThat(rows.getString("title")).isEqualTo("기존 제목");
    assertThat(rows.getString("body")).isEqualTo("기존 본문");
    assertThat(rows.getTimestamp("created_at").toLocalDateTime()).hasToString("2026-01-01T01:02:03");
    assertThat(rows.getTimestamp("updated_at").toLocalDateTime()).hasToString("2026-02-02T04:05:06");
    assertThat(rows.next()).isFalse();
   }
  }
 }
}
