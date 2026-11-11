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
class DetailsTest {
 @Autowired NoteService service;
 @Autowired NoteRepository notes;
 @Autowired LabelRepository labels;
 @Autowired MockMvc mvc;
 @BeforeEach void clean() { notes.deleteAll();labels.deleteAll(); }
 NoteForm form(String title) { var f=new NoteForm();f.setTitle(title);return f; }
 @Test void checklistOnlyCreateToggleSearchAndRestore() {
  var f=form("");f.setChecklistText("첫 항목\n[x] 완료 항목");
  var n=service.create(f);assertThat(n.getItems()).hasSize(2);
  assertThat(service.search("첫 항목")).extracting(Note::getId).contains(n.getId());
  service.check(n.getId(),0,true);assertThat(service.get(n.getId()).getItems().getFirst().isDone()).isTrue();
  service.trash(n.getId());assertThatThrownBy(()->service.check(n.getId(),0,false)).isInstanceOf(NoteStateException.class);
  service.restore(n.getId());assertThat(service.get(n.getId()).getItems()).hasSize(2);
  service.trash(n.getId());service.deletePermanently(n.getId());assertThat(notes.existsById(n.getId())).isFalse();
 }
 @Test void labelsRenameDeletePreserveNotesAndFilterTrash() {
  var label=service.createLabel("과제");var f=form("라벨 메모");f.setLabelIds(java.util.Set.of(label.getId()));
  var n=service.create(f);service.setArchived(n.getId(),true);
  assertThat(service.labeled(label.getId())).hasSize(1);
  service.trash(n.getId());assertThat(service.labeled(label.getId())).isEmpty();
  service.renameLabel(label.getId(),"학습");assertThat(service.get(n.getId()).getLabels()).extracting(Label::getName).containsExactly("학습");
  service.deleteLabel(label.getId());assertThat(service.get(n.getId()).getLabels()).isEmpty();
  assertThat(notes.count()).isEqualTo(1);
 }
 @Test void pinOrderColorAndTrashGuards() {
  var a=service.create(form("먼저"));var b=service.create(form("나중"));service.pin(a.getId(),true);
  assertThat(service.list()).extracting(Note::getId).containsExactly(a.getId(),b.getId());
  service.color(a.getId(),"GREEN");assertThat(service.get(a.getId()).getColor()).isEqualTo("GREEN");
  assertThatThrownBy(()->service.color(a.getId(),"url(evil)")).isInstanceOf(IllegalArgumentException.class);
  service.trash(a.getId());assertThatThrownBy(()->service.pin(a.getId(),false)).isInstanceOf(NoteStateException.class);
  assertThatThrownBy(()->service.color(a.getId(),"BLUE")).isInstanceOf(NoteStateException.class);
  service.restore(a.getId());assertThat(service.get(a.getId()).isPinned()).isTrue();
 }
 @Test void labelsAndChecklistValidation() {
  service.createLabel("Same");assertThatThrownBy(()->service.createLabel(" same ")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->service.createLabel(" ")).isInstanceOf(IllegalArgumentException.class);
  var f=form("");f.setChecklistText("[x] ");assertThatThrownBy(()->service.create(f)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
  f.setChecklistText("a".repeat(201));assertThatThrownBy(()->service.create(f)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
 }
 @Test void newPagesRenderAndSubmit() throws Exception {
  mvc.perform(post("/labels").param("name","브라우저 라벨")).andExpect(redirectedUrl("/labels"));
  mvc.perform(get("/labels")).andExpect(status().isOk());
  var n=service.create(form("설정 메모"));
  mvc.perform(get("/notes/"+n.getId())).andExpect(status().isOk());
  mvc.perform(post("/notes/"+n.getId()+"/color").param("color","BLUE")).andExpect(status().is3xxRedirection());
  mvc.perform(get("/")).andExpect(status().isOk());
  mvc.perform(get("/labels/"+labels.findAll().getFirst().getId())).andExpect(status().isOk());
 }
 @Test void blankChecklistMarkersAreRejectedOnCreateAndUpdate() throws Exception {
  var original=service.create(form("보존할 메모"));
  for(String blank:java.util.List.of("[x]", "[x] ", "[X]\t", "[ ]", "[ ] ", "[x] \u00a0")) {
   var invalid=form("");invalid.setChecklistText(blank);
   assertThatThrownBy(()->service.create(invalid)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
   assertThatThrownBy(()->service.update(original.getId(),invalid)).isInstanceOf(jakarta.validation.ConstraintViolationException.class);
   mvc.perform(post("/notes").param("title","").param("body","").param("checklistText",blank))
    .andExpect(status().isBadRequest());
  }
  assertThat(notes.count()).isEqualTo(1);
  assertThat(service.get(original.getId()).getTitle()).isEqualTo("보존할 메모");
 }
}
