package kr.rea.keep;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="rea_app_notes")
public class Note {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, length=200) private String title;
 @Column(nullable=false, columnDefinition="TEXT") private String body;
 @Column(nullable=false, updatable=false) private LocalDateTime createdAt;
 @Column(nullable=false) private LocalDateTime updatedAt;
 protected Note() {}
 Note(String title, String body, LocalDateTime now) {
  this.title=title; this.body=body; this.createdAt=now; this.updatedAt=now;
 }
 void edit(String title, String body, LocalDateTime now) {
  this.title=title; this.body=body; this.updatedAt=now;
 }
 public Long getId() { return id; }
 public String getTitle() { return title; }
 public String getBody() { return body; }
 public LocalDateTime getCreatedAt() { return createdAt; }
 public LocalDateTime getUpdatedAt() { return updatedAt; }
}
