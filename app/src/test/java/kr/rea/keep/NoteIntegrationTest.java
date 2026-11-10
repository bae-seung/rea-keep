package kr.rea.keep;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.validation.ConstraintViolationException;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class NoteIntegrationTest {
 @Autowired NoteService service;
 @Autowired NoteRepository repository;
 @Autowired MockMvc mvc;
 @BeforeEach void clean() { repository.deleteAll(); }
 private NoteForm form(String title,String body) {
  var f=new NoteForm(); f.setTitle(title); f.setBody(body); return f;
 }
 @Test void createAndUpdatePersistAcrossTransactions() {
  Note created=service.create(form("처음","본문"));
  var id=created.getId(); var time=created.getCreatedAt();
  assertThat(id).isNotNull(); assertThat(created.getUpdatedAt()).isEqualTo(time);
  service.update(id,form("수정","변경된 본문"));
  Note fetched=service.get(id);
  assertThat(fetched.getTitle()).isEqualTo("수정");
  assertThat(fetched.getBody()).isEqualTo("변경된 본문");
  assertThat(fetched.getCreatedAt()).isEqualTo(time);
  assertThat(fetched.getUpdatedAt()).isAfterOrEqualTo(time);
  assertThat(repository.count()).isEqualTo(1);
 }
 @Test void blankCreateAndUpdateAreRejectedWithoutChangingData() {
  assertThatThrownBy(() -> service.create(form(" \t","\n\u00a0\u3000"))).isInstanceOf(ConstraintViolationException.class);
  Note note=service.create(form("제목",""));
  assertThatThrownBy(() -> service.update(note.getId(),form("",""))).isInstanceOf(ConstraintViolationException.class);
  assertThat(service.get(note.getId()).getTitle()).isEqualTo("제목");
  assertThat(repository.count()).isEqualTo(1);
 }
 @Test void bodyOnlyAndLengthBoundaries() {
  service.create(form("","본문"));
  service.create(form("가".repeat(200),"나".repeat(10000)));
  assertThatThrownBy(() -> service.create(form("가".repeat(201),""))).isInstanceOf(ConstraintViolationException.class);
  assertThatThrownBy(() -> service.create(form("","나".repeat(10001)))).isInstanceOf(ConstraintViolationException.class);
 }
 @Test void emptyListAndMissingNote() throws Exception {
  mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("아직 저장한 메모가 없습니다")));
  mvc.perform(get("/notes/999999")).andExpect(status().isNotFound());
  mvc.perform(post("/notes/999999").param("title","없는 메모").param("body","")).andExpect(status().isNotFound());
 }
 @Test void webCreateEditReloadAndEscaping() throws Exception {
  mvc.perform(get("/notes/new")).andExpect(status().isOk());
  mvc.perform(post("/notes").param("title","<script>alert(1)</script>").param("body","본문")).andExpect(status().is3xxRedirection()).andExpect(flash().attribute("success","메모를 저장했습니다."));
  var id=repository.findAll().getFirst().getId();
  mvc.perform(get("/")).andExpect(content().string(org.hamcrest.Matchers.containsString("&lt;script&gt;")));
  mvc.perform(get("/notes/"+id)).andExpect(status().isOk());
  mvc.perform(post("/notes/"+id).param("title","수정 제목").param("body","수정 본문")).andExpect(status().is3xxRedirection());
  mvc.perform(get("/")).andExpect(content().string(org.hamcrest.Matchers.containsString("수정 본문")));
 }
 @Test void invalidFormIsNotSuccessful() throws Exception {
  mvc.perform(post("/notes").param("title"," ").param("body","\n")).andExpect(status().isBadRequest()).andExpect(view().name("editor"));
  assertThat(repository.count()).isZero();
 }
 @Test void archiveSeparatesListsAndPreservesAllContentAndTimes() {
  Note note=service.create(form("보관 대상","내용 보존"));
  Note other=service.create(form("일반 메모","남아 있음"));
  assertThat(note.isArchived()).isFalse();
  service.setArchived(note.getId(),true);
  assertThat(service.list()).extracting(Note::getId).containsExactly(other.getId());
  assertThat(service.archivedList()).extracting(Note::getId).containsExactly(note.getId());
  Note archived=service.get(note.getId());
  assertThat(archived.getTitle()).isEqualTo(note.getTitle());
  assertThat(archived.getBody()).isEqualTo(note.getBody());
  assertThat(archived.getCreatedAt()).isEqualTo(note.getCreatedAt());
  assertThat(archived.getUpdatedAt()).isEqualTo(note.getUpdatedAt());
  service.setArchived(note.getId(),true); // 같은 요청을 반복해도 상태가 반전되지 않음
  assertThat(service.get(note.getId()).isArchived()).isTrue();
  service.setArchived(note.getId(),false);
  service.setArchived(note.getId(),false);
  assertThat(service.archivedList()).isEmpty();
  assertThat(service.list()).extracting(Note::getId).containsExactlyInAnyOrder(note.getId(),other.getId());
  Note restored=service.get(note.getId());
  assertThat(restored.getTitle()).isEqualTo(note.getTitle());
  assertThat(restored.getBody()).isEqualTo(note.getBody());
  assertThat(restored.getCreatedAt()).isEqualTo(note.getCreatedAt());
  assertThat(restored.getUpdatedAt()).isEqualTo(note.getUpdatedAt());
 }
 @Test void archiveWebFlowAndEditingArchivedNote() throws Exception {
  Note note=service.create(form("고유 보관 제목","보관 본문"));
  long id=note.getId();
  mvc.perform(get("/archive")).andExpect(status().isOk())
   .andExpect(content().string(org.hamcrest.Matchers.containsString("보관함이 비어 있습니다")));
  mvc.perform(post("/notes/"+id+"/archive")).andExpect(status().is3xxRedirection())
   .andExpect(redirectedUrl("/")).andExpect(flash().attribute("success","메모를 보관했습니다."));
  mvc.perform(get("/")).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("고유 보관 제목"))));
  mvc.perform(get("/archive")).andExpect(status().isOk())
   .andExpect(content().string(org.hamcrest.Matchers.containsString("고유 보관 제목")))
   .andExpect(content().string(org.hamcrest.Matchers.containsString("/notes/"+id+"/unarchive")));
  mvc.perform(post("/notes/"+id).param("title","보관 중 수정").param("body","수정 내용"))
   .andExpect(redirectedUrl("/archive"));
  assertThat(service.get(id).isArchived()).isTrue();
  assertThat(service.get(id).getCreatedAt()).isEqualTo(note.getCreatedAt());
  mvc.perform(post("/notes/"+id+"/unarchive")).andExpect(redirectedUrl("/archive"));
  mvc.perform(get("/")).andExpect(content().string(org.hamcrest.Matchers.containsString("보관 중 수정")));
  mvc.perform(get("/archive")).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("보관 중 수정"))));
 }
 @Test void missingArchiveTargetDoesNotCreateNote() throws Exception {
  mvc.perform(post("/notes/999999/archive")).andExpect(status().isNotFound());
  mvc.perform(post("/notes/999999/unarchive")).andExpect(status().isNotFound());
  assertThat(repository.count()).isZero();
 }
}
