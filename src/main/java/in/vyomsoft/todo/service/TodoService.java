package in.vyomsoft.todo.service;

import in.vyomsoft.todo.payload.TodoDto;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TodoService {
    TodoDto getTodoById(int id, String username) throws AccessDeniedException;
    List<TodoDto> getAllGroupsForUser(String username, int pageNo, int pageSize, String sortBy, String sortDir);
    List<TodoDto> getAllTimeFilteredGroupsForUser(
            String username, int pageNo, int pageSize, String sortBy, String sortDir,
            LocalDate requestedDate, String timezoneId);
    TodoDto createTodoGroup(TodoDto dto, String username);
    TodoDto updateTodo(int id, TodoDto todo, String username) throws AccessDeniedException;
    void delete(int id, String username) throws AccessDeniedException;
}
