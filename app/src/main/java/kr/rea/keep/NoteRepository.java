package kr.rea.keep;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface NoteRepository extends JpaRepository<Note,Long> {
 List<Note> findAllByArchivedAndTrashedFalseOrderByPinnedDescUpdatedAtDescIdDesc(boolean archived);
 List<Note> findAllByTrashedTrueOrderByUpdatedAtDescIdDesc();
 @Query("select n from Note n where n.trashed=false and (locate(lower(:term),lower(n.title)) > 0 or locate(lower(:term),lower(n.body)) > 0 or exists (select i from n.items i where locate(lower(:term),lower(i.text)) > 0)) order by n.pinned desc,n.updatedAt desc,n.id desc")
 List<Note> search(@Param("term") String term);
 List<Note> findAllByLabels_IdAndTrashedFalseOrderByPinnedDescUpdatedAtDescIdDesc(long id);
 List<Note> findAllByLabels_Id(long id);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select n from Note n where n.id=:id")
 Optional<Note> findForUpdate(@Param("id") long id);
}
