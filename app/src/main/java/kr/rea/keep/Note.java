package kr.rea.keep;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
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
 @Column(nullable=false) private boolean trashed;
 @Column(nullable=false) private boolean pinned;
 @Column(nullable=false,length=16) private String color="DEFAULT";
 @ElementCollection(fetch=FetchType.EAGER)
 @CollectionTable(name="rea_app_check_items",joinColumns=@JoinColumn(name="note_id"))
 @OrderColumn(name="item_order") private List<CheckItem> items=new ArrayList<>();
 @ManyToMany(fetch=FetchType.EAGER)
 @JoinTable(name="rea_app_note_labels",joinColumns=@JoinColumn(name="note_id"),inverseJoinColumns=@JoinColumn(name="label_id"))
 private Set<Label> labels=new LinkedHashSet<>();
 protected Note() {}
 public boolean isPinned() { return pinned; }
 public String getColor() { return color; }
 public List<CheckItem> getItems() { return items; }
 public Set<Label> getLabels() { return labels; }
 public String getChecklistText() { return items.stream().map(i -> (i.isDone()?"[x] ":"[ ] ")+i.getText()).collect(java.util.stream.Collectors.joining("\n")); }
 void setPinned(boolean pinned) { this.pinned=pinned; }
 void setColor(String color) { this.color=color; }
 void replaceDetails(List<CheckItem> newItems,Set<Label> newLabels) {
  items.clear(); items.addAll(newItems); labels.clear();labels.addAll(newLabels);
 }

 Note(String title, String body, LocalDateTime now) {
  this.title=title; this.body=body; this.createdAt=now; this.updatedAt=now;
 }
 void edit(String title, String body, LocalDateTime now) {
  this.title=title; this.body=body; this.updatedAt=now;
 }
 void setArchived(boolean archived) { this.archived=archived; }
 void setTrashed(boolean trashed) { this.trashed=trashed; }
 public boolean isTrashed() { return trashed; }
 public boolean isArchived() { return archived; }
 public Long getId() { return id; }
 public String getTitle() { return title; }
 public String getBody() { return body; }
 public LocalDateTime getCreatedAt() { return createdAt; }
 public LocalDateTime getUpdatedAt() { return updatedAt; }
}
