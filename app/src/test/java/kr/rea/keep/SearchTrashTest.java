package kr.rea.keep;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class SearchTrashTest {
 @Autowired NoteService service;
 @Autowired NoteRepository repository;
 @Autowired MockMvc mvc;
 @BeforeEach void clean() { repository.deleteAll(); }
 NoteForm form(String title,String body) { var f=new NoteForm();f.setTitle(title);f.setBody(body);return f; }
 @Test void searchTitleBodyArchivedAndLiteralSpecialCharacters() {
  var a=service.create(form("Alpha 제목","본문 100%_!"));
  var b=service.create(form("보관 제목","Alpha 본문"));service.setArchived(b.getId(),true);
  assertThat(service.search("alpha")).extracting(Note::getId).containsExactlyInAnyOrder(a.getId(),b.getId());
  assertThat(service.search("본문 100%_!")).extracting(Note::getId).containsExactly(a.getId());
  assertThat(service.search("%_")).extracting(Note::getId).containsExactly(a.getId());
  assertThat(service.search("' OR 1=1 --")).isEmpty();
  assertThat(service.search("  ")).isEmpty();
  assertThatThrownBy(()->service.search("a".repeat(201))).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void trashExcludesAllNormalListsAndRestoresOriginalArchiveState() {
  for (boolean archived:new boolean[]{false,true}) {
   var note=service.create(form("고유 제목","내용 유지"));
   service.setArchived(note.getId(),archived);service.trash(note.getId());
   assertThat(service.list()).extracting(Note::getId).doesNotContain(note.getId());
   assertThat(service.archivedList()).extracting(Note::getId).doesNotContain(note.getId());
   assertThat(service.search("고유 제목")).extracting(Note::getId).doesNotContain(note.getId());
   assertThat(service.trashList()).extracting(Note::getId).contains(note.getId());
   service.restore(note.getId());var restored=service.get(note.getId());
   assertThat(restored.isArchived()).isEqualTo(archived);
   assertThat(restored.getTitle()).isEqualTo(note.getTitle());
   assertThat(restored.getBody()).isEqualTo(note.getBody());
   assertThat(restored.getCreatedAt()).isEqualTo(note.getCreatedAt());
   assertThat(restored.getUpdatedAt()).isEqualTo(note.getUpdatedAt());
   assertThat(archived?service.archivedList():service.list()).extracting(Note::getId).contains(note.getId());
  }
 }
 @Test void invalidStateOperationsAreBlocked() {
  var note=service.create(form("제목","본문"));long id=note.getId();
  assertThatThrownBy(()->service.deletePermanently(id)).isInstanceOf(NoteStateException.class);
  assertThatThrownBy(()->service.restore(id)).isInstanceOf(NoteStateException.class);
  service.trash(id);
  assertThatThrownBy(()->service.update(id,form("바뀌면 안 됨",""))).isInstanceOf(NoteStateException.class);
  assertThatThrownBy(()->service.setArchived(id,true)).isInstanceOf(NoteStateException.class);
  assertThat(service.get(id).getTitle()).isEqualTo("제목");
  service.deletePermanently(id);assertThat(repository.existsById(id)).isFalse();
 }
 @Test void confirmationAndHttpStateGuards() throws Exception {
  var note=service.create(form("삭제 대상","본문"));long id=note.getId();
  mvc.perform(post("/notes/"+id+"/delete").param("confirmed","true")).andExpect(status().isConflict());
  mvc.perform(get("/notes/"+id+"/delete-confirm")).andExpect(status().isConflict());
  mvc.perform(post("/notes/"+id+"/trash")).andExpect(redirectedUrl("/trash"));
  mvc.perform(get("/notes/"+id)).andExpect(status().isConflict());
  mvc.perform(post("/notes/"+id).param("title","수정 시도").param("body","본문")).andExpect(status().isConflict());
  mvc.perform(post("/notes/"+id+"/archive")).andExpect(status().isConflict());
  mvc.perform(post("/notes/"+id+"/unarchive")).andExpect(status().isConflict());
  mvc.perform(get("/trash")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("삭제 대상")));
  mvc.perform(get("/notes/"+id+"/delete-confirm")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("취소")));
  assertThat(repository.existsById(id)).isTrue();
  mvc.perform(post("/notes/"+id+"/delete")).andExpect(status().isBadRequest());
  assertThat(repository.existsById(id)).isTrue();
  mvc.perform(post("/notes/"+id+"/delete").param("confirmed","true")).andExpect(redirectedUrl("/trash"));
  assertThat(repository.existsById(id)).isFalse();
 }
 @Test void searchNoResultsAndRestoreHttp() throws Exception {
  mvc.perform(get("/search").param("q","없는 검색어")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("검색 결과가 없습니다")));
  mvc.perform(get("/search").param("q","a".repeat(201))).andExpect(status().isBadRequest());
  var note=service.create(form("보관 검색","본문"));service.setArchived(note.getId(),true);
  mvc.perform(get("/search").param("q","보관 검색")).andExpect(content().string(org.hamcrest.Matchers.containsString("보관된 메모")));
  service.trash(note.getId());
  mvc.perform(post("/notes/"+note.getId()+"/restore")).andExpect(redirectedUrl("/trash"));
  assertThat(service.get(note.getId()).isArchived()).isTrue();
 }
}
