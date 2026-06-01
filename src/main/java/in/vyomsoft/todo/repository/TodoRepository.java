package in.vyomsoft.todo.repository;

import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Integer> {
    Page<Todo> findByUserAndParentIsNull(User user, Pageable pageable);
    Page<Todo> findByUserAndParentIsNullAndCreatedAtBetween(
            User user,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}