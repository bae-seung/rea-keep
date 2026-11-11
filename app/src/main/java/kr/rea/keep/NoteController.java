package kr.rea.keep;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class NoteController {
 private final NoteService service;
 public NoteController(NoteService service) { this.service=service; }
 @ModelAttribute("availableLabels") java.util.List<Label> availableLabels() { return service.labels(); }
 @GetMapping("/labels") String labels() { return "labels"; }
 @PostMapping("/labels") String createLabel(@RequestParam String name) { service.createLabel(name);return "redirect:/labels"; }
 @PostMapping("/labels/{id}/rename") String renameLabel(@PathVariable long id,@RequestParam String name) { service.renameLabel(id,name);return "redirect:/labels"; }
 @PostMapping("/labels/{id}/delete") String deleteLabel(@PathVariable long id) { service.deleteLabel(id);return "redirect:/labels"; }
 @GetMapping("/labels/{id}") String labeled(@PathVariable long id,Model model) {
  model.addAttribute("notes",service.labeled(id));model.addAttribute("labelName",service.label(id).getName());model.addAttribute("archiveView",false);return "notes";
 }
 @PostMapping("/notes/{id}/pin") String pin(@PathVariable long id,@RequestParam boolean pinned) { service.pin(id,pinned);return "redirect:/"; }
 @PostMapping("/notes/{id}/color") String color(@PathVariable long id,@RequestParam String color) { service.color(id,color);return "redirect:/notes/"+id; }
 @PostMapping("/notes/{id}/check") String check(@PathVariable long id,@RequestParam int index,@RequestParam boolean done) { service.check(id,index,done);return "redirect:/notes/"+id; }
 @GetMapping("/") String list(Model model) {
  model.addAttribute("notes",service.list()); model.addAttribute("archiveView",false); return "notes";
 }
 @GetMapping("/archive") String archive(Model model) {
  model.addAttribute("notes",service.archivedList()); model.addAttribute("archiveView",true); return "notes";
 }
 @PostMapping("/notes/{id}/archive") String archiveNote(@PathVariable long id, Model model,
   RedirectAttributes redirect, HttpServletResponse response) {
  return changeArchive(id,true,model,redirect,response);
 }
 @PostMapping("/notes/{id}/unarchive") String unarchiveNote(@PathVariable long id, Model model,
   RedirectAttributes redirect, HttpServletResponse response) {
  return changeArchive(id,false,model,redirect,response);
 }
 private String changeArchive(long id,boolean archived,Model model,RedirectAttributes redirect,
   HttpServletResponse response) {
  try { service.setArchived(id,archived); }
  catch (NoteNotFoundException | NoteStateException e) { throw e; }
  catch (RuntimeException e) {
   response.setStatus(503);
   model.addAttribute("message","보관 상태 변경을 완료하지 못했습니다. 목록과 보관함에서 현재 상태를 확인한 뒤 다시 시도해 주세요.");
   return "error";
  }
  redirect.addFlashAttribute("success",archived ? "메모를 보관했습니다." : "메모의 보관을 해제했습니다.");
  return archived ? "redirect:/" : "redirect:/archive";
 }
 @GetMapping("/search") String search(@RequestParam(defaultValue="") String q,Model model) {
  model.addAttribute("notes",service.search(q)); model.addAttribute("q",q);
  model.addAttribute("page","search"); model.addAttribute("archiveView",false); return "notes";
 }
 @GetMapping("/trash") String trash(Model model) {
  model.addAttribute("notes",service.trashList()); model.addAttribute("page","trash");
  model.addAttribute("archiveView",false); return "notes";
 }
 @PostMapping("/notes/{id}/trash") String trashNote(@PathVariable long id,RedirectAttributes redirect) {
  service.trash(id); redirect.addFlashAttribute("success","메모를 휴지통으로 이동했습니다."); return "redirect:/trash";
 }
 @PostMapping("/notes/{id}/restore") String restore(@PathVariable long id,RedirectAttributes redirect) {
  service.restore(id); redirect.addFlashAttribute("success","메모를 삭제 전 위치로 복원했습니다."); return "redirect:/trash";
 }
 @GetMapping("/notes/{id}/delete-confirm") String confirmDelete(@PathVariable long id,Model model) {
  model.addAttribute("note",service.trashed(id)); return "delete-confirm";
 }
 @PostMapping("/notes/{id}/delete") String delete(@PathVariable long id,
   @RequestParam(defaultValue="false") boolean confirmed,RedirectAttributes redirect) {
  if (!confirmed) throw new IllegalArgumentException("영구 삭제 확인이 필요합니다.");
  service.deletePermanently(id); redirect.addFlashAttribute("success","메모를 영구 삭제했습니다."); return "redirect:/trash";
 }
 @ExceptionHandler(NoteStateException.class)
 String invalidState(NoteStateException e,HttpServletResponse response,Model model) {
  response.setStatus(409); model.addAttribute("message",e.getMessage()); return "error";
 }
 @ExceptionHandler(IllegalArgumentException.class)
 String invalidInput(IllegalArgumentException e,HttpServletResponse response,Model model) {
  response.setStatus(400); model.addAttribute("message",e.getMessage()); return "error";
 }
 @GetMapping("/notes/new") String createForm(Model model) {
  model.addAttribute("noteForm",new NoteForm()); return "editor";
 }
 @GetMapping("/notes/{id}") String editForm(@PathVariable long id, Model model) {
  Note note=service.editable(id); NoteForm form=new NoteForm();
  form.setTitle(note.getTitle()); form.setBody(note.getBody());
  form.setChecklistText(note.getChecklistText());
  form.setLabelIds(note.getLabels().stream().map(Label::getId).collect(java.util.stream.Collectors.toSet()));
  model.addAttribute("note",note);
  model.addAttribute("noteForm",form); model.addAttribute("noteId",id); return "editor";
 }
 @PostMapping("/notes") String create(@Valid @ModelAttribute NoteForm noteForm, BindingResult errors,
   Model model, RedirectAttributes redirect, HttpServletResponse response) {
  return save(null,noteForm,errors,model,redirect,response);
 }
 @PostMapping("/notes/{id}") String update(@PathVariable long id,@Valid @ModelAttribute NoteForm noteForm,
   BindingResult errors,Model model,RedirectAttributes redirect,HttpServletResponse response) {
  return save(id,noteForm,errors,model,redirect,response);
 }
 private String save(Long id,NoteForm form,BindingResult errors,Model model,
   RedirectAttributes redirect,HttpServletResponse response) {
  model.addAttribute("noteId",id);
  if (errors.hasErrors()) { response.setStatus(400); return "editor"; }
  Note saved;
  try {
   saved = id==null ? service.create(form) : service.update(id,form);
  } catch (NoteNotFoundException | NoteStateException e) { throw e;
  } catch (RuntimeException e) {
   // DB 오류의 원문에는 연결 정보가 포함될 수 있으므로 출력하지 않는다.
   response.setStatus(503);
   model.addAttribute("saveError","저장을 완료하지 못했습니다. 입력 내용을 보관하고 목록에서 저장 여부를 확인한 뒤 다시 시도해 주세요.");
   return "editor";
  }
  redirect.addFlashAttribute("success","메모를 저장했습니다."); return saved.isArchived() ? "redirect:/archive" : "redirect:/";
 }
 @ExceptionHandler(NoteNotFoundException.class)
 String notFound(HttpServletResponse response,Model model) {
  response.setStatus(404); model.addAttribute("message","메모를 찾을 수 없습니다."); return "error";
 }
 @ExceptionHandler(RuntimeException.class)
 String unavailable(HttpServletResponse response,Model model) {
  response.setStatus(503); model.addAttribute("message","요청을 완료하지 못했습니다. 목록과 휴지통에서 현재 상태를 확인하고 다시 시도해 주세요."); return "error";
 }
}
