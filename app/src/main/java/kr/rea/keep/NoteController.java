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
  catch (NoteNotFoundException e) { throw e; }
  catch (RuntimeException e) {
   response.setStatus(503);
   model.addAttribute("message","보관 상태 변경을 완료하지 못했습니다. 목록과 보관함에서 현재 상태를 확인한 뒤 다시 시도해 주세요.");
   return "error";
  }
  redirect.addFlashAttribute("success",archived ? "메모를 보관했습니다." : "메모의 보관을 해제했습니다.");
  return archived ? "redirect:/" : "redirect:/archive";
 }
 @GetMapping("/notes/new") String createForm(Model model) {
  model.addAttribute("noteForm",new NoteForm()); return "editor";
 }
 @GetMapping("/notes/{id}") String editForm(@PathVariable long id, Model model) {
  Note note=service.get(id); NoteForm form=new NoteForm();
  form.setTitle(note.getTitle()); form.setBody(note.getBody());
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
  } catch (NoteNotFoundException e) { throw e;
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
  response.setStatus(503); model.addAttribute("message","메모를 불러오지 못했습니다. DB 연결 상태를 확인하고 다시 시도해 주세요."); return "error";
 }
}
