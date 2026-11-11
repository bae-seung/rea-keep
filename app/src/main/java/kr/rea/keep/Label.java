package kr.rea.keep;
import jakarta.persistence.*;
@Entity @Table(name="rea_app_labels")
public class Label {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=50) private String name;
 protected Label() {}
 Label(String name) { this.name=name; }
 public Long getId() { return id; }
 public String getName() { return name; }
 void rename(String name) { this.name=name; }
}
