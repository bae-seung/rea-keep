package kr.rea.keep;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@org.hibernate.annotations.DynamicUpdate
@Entity
@Table(name="rea_app_notes")
public class Note {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false, length=200) private String title;
 @Column(nullable=false, columnDefinition="TEXT") private String body;
 @Column(nullable=false, updatable=false) private LocalDateTime createdAt;
 @Column(nullable=false) private LocalDateTime updatedAt;
 @Column(nullable=false) private boolean archived;
 protected Note() {}
 Note(String title, String body, LocalDateTime now) {
  this.title=title; this.body=body; this.createdAt=now; this.updatedAt=now;
 }
 void edit(String title, String body, LocalDateTime now) {
  this.title=title; this.body=body; this.updatedAt=now;
 }
 void setArchived(boolean archived) { this.archived=archived; }
 public boolean isArchived() { return archived; }
 public Long getId() { return id; }
 public String getTitle() { return title; }
 public String getBody() { return body; }
 public LocalDateTime getCreatedAt() { return createdAt; }
 public LocalDateTime getUpdatedAt() { return updatedAt; }
}
