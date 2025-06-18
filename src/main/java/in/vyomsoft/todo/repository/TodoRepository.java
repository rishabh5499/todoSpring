package in.vyomsoft.todo.repository;

import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    Page<Todo> findByUser(User user, Pageable pageable);
}
