package in.vyomsoft.todo.repository;

import in.vyomsoft.todo.entity.Notes;
import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotesRepository extends JpaRepository<Notes, Long> {
    List<Notes> findByUser(User user);
}
