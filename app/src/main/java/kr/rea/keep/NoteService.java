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
 private final Clock clock;
 private final Validator validator;
 public NoteService(NoteRepository repository, Clock clock, Validator validator) {
  this.repository=repository; this.clock=clock; this.validator=validator;
 }
 public List<Note> list() { return repository.findAllByOrderByUpdatedAtDescIdDesc(); }
 public Note get(long id) { return repository.findById(id).orElseThrow(NoteNotFoundException::new); }
 @Transactional
 public Note create(NoteForm form) {
  validate(form);
  return repository.saveAndFlush(new Note(form.getTitle(),form.getBody(),now()));
 }
 @Transactional
 public Note update(long id, NoteForm form) {
  validate(form);
  Note note=get(id);
  note.edit(form.getTitle(),form.getBody(),now());
  return repository.saveAndFlush(note);
 }
 private LocalDateTime now() { return LocalDateTime.ofInstant(clock.instant(),ZoneOffset.UTC).truncatedTo(java.time.temporal.ChronoUnit.MICROS); }
 private void validate(NoteForm form) {
  var violations=validator.validate(form);
  if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
 }
}
