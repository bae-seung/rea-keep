package kr.rea.keep;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LabelRepository extends JpaRepository<Label,Long> {
 List<Label> findAllByOrderByNameAsc();
 Optional<Label> findByNameIgnoreCase(String name);
}
