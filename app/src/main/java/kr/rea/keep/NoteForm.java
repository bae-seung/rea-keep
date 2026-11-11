package kr.rea.keep;
import jakarta.validation.constraints.*;
public class NoteForm {
 @NotNull @Size(max=21000) private String checklistText="";
 @Size(max=50) private java.util.Set<Long> labelIds=new java.util.LinkedHashSet<>();
 public String getChecklistText() { return checklistText; }
 public void setChecklistText(String text) { this.checklistText=text; }
 public java.util.Set<Long> getLabelIds() { return labelIds; }
 public void setLabelIds(java.util.Set<Long> ids) { this.labelIds=ids; }
 @AssertTrue(message="체크리스트는 빈 항목 없이 항목당 200자, 최대 100개로 입력해 주세요.")
 public boolean isChecklistValid() { try { parseItems();return true; } catch(IllegalArgumentException e) { return false; } }
 public java.util.List<CheckItem> parseItems() {
  var result=new java.util.ArrayList<CheckItem>();
  if(checklistText==null) return result;
  for(String line:checklistText.split("\\R")) {
   String value=line.strip(); if(value.isEmpty()) continue;
   if(java.util.Set.of("[x]","[X]","[ ]").contains(value)) throw new IllegalArgumentException();
   boolean done=value.startsWith("[x] ")||value.startsWith("[X] ");
   if(done||value.startsWith("[ ] ")) value=value.substring(4).strip();
   if(!hasText(value)||value.length()>200) throw new IllegalArgumentException();
   result.add(new CheckItem(value,done));
  }
  if(result.size()>100) throw new IllegalArgumentException();return result;
 }

 @NotNull @Size(max=200, message="제목은 200자 이내로 입력해 주세요.") private String title="";
 @NotNull @Size(max=10000, message="본문은 10,000자 이내로 입력해 주세요.") private String body="";
 @AssertTrue(message="제목이나 본문을 입력해 주세요.")
 public boolean isContentPresent() { return hasText(title) || hasText(body) || hasText(checklistText); }
 private static boolean hasText(String s) {
  return s != null && s.codePoints().anyMatch(c -> !Character.isWhitespace(c) && !Character.isSpaceChar(c));
 }
 public String getTitle() { return title; }
 public void setTitle(String title) { this.title=title; }
 public String getBody() { return body; }
 public void setBody(String body) { this.body=body; }
}
