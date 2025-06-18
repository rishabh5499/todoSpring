package in.vyomsoft.todo.service;

import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.payload.TodoDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface TodoService {
    TodoDto getTodoById(Long id, String username) throws AccessDeniedException;
    List<TodoDto> getAllForUser(String username, int pageNo, int pageSize, String sortBy, String sortDir);
    TodoDto createTodoForUser(TodoDto dto, String username);
    TodoDto updateTodo(Long id, TodoDto todo, String username) throws AccessDeniedException;
    void delete(Long id, String username) throws AccessDeniedException;
}
