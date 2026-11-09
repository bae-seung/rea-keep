package kr.rea.keep;
import jakarta.validation.constraints.*;
public class NoteForm {
 @NotNull @Size(max=200, message="제목은 200자 이내로 입력해 주세요.") private String title="";
 @NotNull @Size(max=10000, message="본문은 10,000자 이내로 입력해 주세요.") private String body="";
 @AssertTrue(message="제목이나 본문을 입력해 주세요.")
 public boolean isContentPresent() { return hasText(title) || hasText(body); }
 private static boolean hasText(String s) {
  return s != null && s.codePoints().anyMatch(c -> !Character.isWhitespace(c) && !Character.isSpaceChar(c));
 }
 public String getTitle() { return title; }
 public void setTitle(String title) { this.title=title; }
 public String getBody() { return body; }
 public void setBody(String body) { this.body=body; }
}
