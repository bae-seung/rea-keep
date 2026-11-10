package kr.rea.keep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(NoteController.class)
class NoteFailureTest {
 @Autowired MockMvc mvc;
 @MockitoBean NoteService service;
 @Test void createFailurePreservesInputAndDoesNotReportSuccess() throws Exception {
  when(service.create(any())).thenThrow(new DataAccessResourceFailureException("private connection details"));
  mvc.perform(post("/notes").param("title","남겨둘 제목").param("body","남겨둘 본문"))
   .andExpect(status().isServiceUnavailable()).andExpect(view().name("editor"))
   .andExpect(model().attributeExists("saveError")).andExpect(flash().attributeCount(0))
   .andExpect(content().string(org.hamcrest.Matchers.containsString("남겨둘 본문")))
   .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private connection details"))));
 }
 @Test void updateFailurePreservesEditTarget() throws Exception {
  when(service.update(eq(7L),any())).thenThrow(new DataAccessResourceFailureException("offline"));
  mvc.perform(post("/notes/7").param("title","수정").param("body","본문"))
   .andExpect(status().isServiceUnavailable()).andExpect(model().attribute("noteId",7L))
   .andExpect(content().string(org.hamcrest.Matchers.containsString("/notes/7"))).andExpect(flash().attributeCount(0));
 }
 @Test void listFailureDoesNotLookLikeEmptyList() throws Exception {
  when(service.list()).thenThrow(new DataAccessResourceFailureException("offline"));
  mvc.perform(get("/")).andExpect(status().isServiceUnavailable()).andExpect(view().name("error"));
 }
 @Test void archiveAndUnarchiveFailuresDoNotReportSuccess() throws Exception {
  doThrow(new DataAccessResourceFailureException("private connection details"))
   .when(service).setArchived(anyLong(),anyBoolean());
  for (String action : new String[]{"archive","unarchive"}) {
   mvc.perform(post("/notes/7/"+action)).andExpect(status().isServiceUnavailable())
    .andExpect(view().name("error")).andExpect(flash().attributeCount(0))
    .andExpect(content().string(org.hamcrest.Matchers.containsString("보관 상태 변경을 완료하지 못했습니다")))
    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private connection details"))));
  }
 }
 @Test void archiveListFailureDoesNotLookEmpty() throws Exception {
  when(service.archivedList()).thenThrow(new DataAccessResourceFailureException("offline"));
  mvc.perform(get("/archive")).andExpect(status().isServiceUnavailable()).andExpect(view().name("error"));
 }
}
