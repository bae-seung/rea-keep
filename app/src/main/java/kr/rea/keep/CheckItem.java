package kr.rea.keep;
import jakarta.persistence.*;
@Embeddable
public class CheckItem {
 @Column(name="item_text",nullable=false,length=200) private String text;
 @Column(nullable=false) private boolean done;
 protected CheckItem() {}
 public CheckItem(String text,boolean done) { this.text=text;this.done=done; }
 public String getText() { return text; }
 public boolean isDone() { return done; }
 void setDone(boolean done) { this.done=done; }
}
