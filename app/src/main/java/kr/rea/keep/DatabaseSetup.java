package kr.rea.keep;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
/** 명시적 CLI에서만 실행하는 추가 마이그레이션. 서버 시작은 여전히 validate 전용. */
final class DatabaseSetup {
 static void run() throws Exception {
  String user=System.getenv("DB_USERNAME"),password=System.getenv("DB_PASSWORD");
  if(user==null||password==null) throw new IllegalArgumentException("인증 환경변수 필요");
  try(Connection c=DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/rea_keep",user,password)) {
   apply(c,Path.of("db"));
  }
 }
 static void apply(Connection c,Path directory) throws Exception {
  if(!table(c,"rea_app_notes")) execute(c,Files.readString(directory.resolve("001-create-notes.sql")));
  for(String col:List.of("id","title","body","created_at","updated_at"))
   if(!column(c,"rea_app_notes",col)) throw new IllegalStateException("기존 메모 테이블 구조 불일치");
  if(!column(c,"rea_app_notes","archived")) execute(c,Files.readString(directory.resolve("002-add-archived.sql")));
  if(!column(c,"rea_app_notes","trashed")) execute(c,Files.readString(directory.resolve("003-add-trashed.sql")));
  String[] statements=Files.readString(directory.resolve("004-details.sql")).replaceAll("(?m)^--.*$","").split(";");
  String[] names={"pinned","color","rea_app_labels","rea_app_check_items","rea_app_note_labels"};
  for(int i=0;i<names.length;i++) {
   boolean exists=i<2?column(c,"rea_app_notes",names[i]):table(c,names[i]);
   if(!exists) execute(c,statements[i]);
  }
  System.out.println("추가 스키마 준비 완료. 기존 행을 삭제/초기화하지 않았습니다.");
 }
 private static boolean table(Connection c,String name) throws SQLException {
  try(var rows=c.getMetaData().getTables(c.getCatalog(),null,null,new String[]{"TABLE"})) {
   while(rows.next()) if(name.equalsIgnoreCase(rows.getString("TABLE_NAME"))) return true;
  } return false;
 }
 private static boolean column(Connection c,String table,String column) throws SQLException {
  try(var rows=c.getMetaData().getColumns(c.getCatalog(),null,null,null)) {
   while(rows.next()) if(table.equalsIgnoreCase(rows.getString("TABLE_NAME"))&&column.equalsIgnoreCase(rows.getString("COLUMN_NAME"))) return true;
  } return false;
 }
 private static void execute(Connection c,String sql) throws SQLException { try(var s=c.createStatement()) { s.execute(sql); } }
}
