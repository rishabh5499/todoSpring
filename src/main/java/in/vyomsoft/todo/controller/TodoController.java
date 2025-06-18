package in.vyomsoft.todo.controller;

import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.service.TodoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static in.vyomsoft.todo.utils.AppConstants.*;

@RestController
@RequestMapping("/todos")
public class TodoController {
    private TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TodoDto> getAll(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                @RequestParam(value = "sortBy", defaultValue = DEFAULT_SORT_BY, required = false) String sortBy,
                                @RequestParam(value = "sortDir", defaultValue = DEFAULT_SORT_DIR, required = false) String sortDir) {
        return service.getAllForUser(userDetails.getUsername(),pageNo, pageSize, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public TodoDto getTodo(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return service.getTodoById(id, userDetails.getUsername());
    }

    @PostMapping
    public TodoDto createEntry(@RequestBody TodoDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        return service.createTodoForUser(dto, userDetails.getUsername());
    }

    @PutMapping("/{id}")
    public TodoDto updateEntry(@PathVariable Long id, @RequestBody TodoDto todo,
                               @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return service.updateTodo(id, todo, userDetails.getUsername());
    }

    @DeleteMapping("/{id}")
    public void deleteEntry(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        service.delete(id, userDetails.getUsername());
    }
}