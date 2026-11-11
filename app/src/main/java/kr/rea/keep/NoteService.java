package kr.rea.keep;
import java.time.*;
import java.util.List;
import jakarta.validation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional(readOnly=true)
public class NoteService {
 private final NoteRepository repository;
 private final LabelRepository labels;
 private final Clock clock;
 private final Validator validator;
 public NoteService(NoteRepository repository, Clock clock, Validator validator, LabelRepository labels) {
  this.repository=repository; this.clock=clock; this.validator=validator; this.labels=labels;
 }
 public List<Note> list() { return repository.findAllByArchivedAndTrashedFalseOrderByPinnedDescUpdatedAtDescIdDesc(false); }
 public List<Note> archivedList() { return repository.findAllByArchivedAndTrashedFalseOrderByPinnedDescUpdatedAtDescIdDesc(true); }
 @Transactional
 public void setArchived(long id, boolean archived) {
  Note note=locked(id);
  requireActive(note);
  note.setArchived(archived);
  repository.saveAndFlush(note);
 }
 public List<Note> trashList() { return repository.findAllByTrashedTrueOrderByUpdatedAtDescIdDesc(); }
 public List<Note> search(String term) {
  String query=term==null ? "" : term.strip();
  if (query.length()>200) throw new IllegalArgumentException("검색어는 200자 이내로 입력해 주세요.");
  return query.isEmpty() ? List.of() : repository.search(query);
 }
 public Note editable(long id) { Note note=get(id); requireActive(note); return note; }
 public Note trashed(long id) { Note note=get(id); requireTrashed(note); return note; }
 @Transactional public void trash(long id) {
  Note note=locked(id); requireActive(note); note.setTrashed(true); repository.saveAndFlush(note);
 }
 @Transactional public void restore(long id) {
  Note note=locked(id); requireTrashed(note); note.setTrashed(false); repository.saveAndFlush(note);
 }
 @Transactional public void deletePermanently(long id) {
  Note note=locked(id); requireTrashed(note); repository.delete(note); repository.flush();
 }
 private Note locked(long id) { return repository.findForUpdate(id).orElseThrow(NoteNotFoundException::new); }
 private void requireActive(Note note) {
  if (note.isTrashed()) throw new NoteStateException("휴지통 메모는 먼저 복원해야 합니다.");
 }
 private void requireTrashed(Note note) {
  if (!note.isTrashed()) throw new NoteStateException("휴지통에 있는 메모에만 가능한 작업입니다.");
 }
 public Note get(long id) { return repository.findById(id).orElseThrow(NoteNotFoundException::new); }
 @Transactional
 public Note create(NoteForm form) {
  validate(form);
  Note note=new Note(form.getTitle(),form.getBody(),now());
  note.replaceDetails(form.parseItems(),selectedLabels(form));
  return repository.saveAndFlush(note);
 }
 @Transactional
 public Note update(long id, NoteForm form) {
  validate(form);
  Note note=locked(id);
  requireActive(note);
  note.edit(form.getTitle(),form.getBody(),now());
  note.replaceDetails(form.parseItems(),selectedLabels(form));
  return repository.saveAndFlush(note);
 }
 public List<Label> labels() { return labels.findAllByOrderByNameAsc(); }
 public Label label(long id) { return labels.findById(id).orElseThrow(NoteNotFoundException::new); }
 public List<Note> labeled(long id) { label(id);return repository.findAllByLabels_IdAndTrashedFalseOrderByPinnedDescUpdatedAtDescIdDesc(id); }
 @Transactional public Label createLabel(String name) { return labels.saveAndFlush(new Label(validLabel(name,null))); }
 @Transactional public void renameLabel(long id,String name) { label(id).rename(validLabel(name,id));labels.flush(); }
 @Transactional public void deleteLabel(long id) {
  Label label=label(id);
  for(Note note:repository.findAllByLabels_Id(id)) { note.getLabels().removeIf(l -> l.getId().equals(id)); }
  repository.flush();labels.delete(label);labels.flush();
 }
 private String validLabel(String name,Long id) {
  String value=name==null?"":name.strip();
  if(value.isBlank()||value.length()>50) throw new IllegalArgumentException("라벨 이름은 1~50자로 입력해 주세요.");
  labels.findByNameIgnoreCase(value).ifPresent(l -> { if(!l.getId().equals(id)) throw new IllegalArgumentException("같은 이름의 라벨이 있습니다."); });
  return value;
 }
 private java.util.Set<Label> selectedLabels(NoteForm form) {
  var result=new java.util.LinkedHashSet<Label>();
  if(form.getLabelIds()!=null) for(Long id:form.getLabelIds()) result.add(label(id));return result;
 }
 @Transactional public void pin(long id,boolean pinned) { Note n=locked(id);requireActive(n);n.setPinned(pinned);repository.flush(); }
 @Transactional public void color(long id,String color) {
  if(!java.util.Set.of("DEFAULT","YELLOW","GREEN","BLUE","PINK").contains(color)) throw new IllegalArgumentException("지원하지 않는 색상입니다.");
  Note n=locked(id);requireActive(n);n.setColor(color);repository.flush();
 }
 @Transactional public void check(long id,int index,boolean done) {
  Note n=locked(id);requireActive(n);
  if(index<0||index>=n.getItems().size()) throw new IllegalArgumentException("체크리스트 항목을 찾을 수 없습니다.");
  n.getItems().get(index).setDone(done);repository.flush();
 }
 private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS); }
 private void validate(NoteForm form) {
  var violations=validator.validate(form);
  if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
 }
}
