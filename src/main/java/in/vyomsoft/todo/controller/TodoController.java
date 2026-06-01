package in.vyomsoft.todo.controller;

import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.service.TodoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static in.vyomsoft.todo.utils.AppConstants.*;

@RestController
@RequestMapping("/todos")
public class TodoController {
    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public TodoDto getTodo(@PathVariable int id,
                           @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return service.getTodoById(id, userDetails.getUsername());
    }

    @GetMapping
    public List<TodoDto> getAllGroups(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        return service.getAllGroupsForUser(
                userDetails.getUsername(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                pageable.getSort().stream().findFirst().map(order -> order.getDirection().name()).orElse(DEFAULT_SORT_DIR)
        );
    }

    @GetMapping("/date")
    public List<TodoDto> getAllTimeFilteredGroupsForUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "date", required = false) String dateString,
            Pageable pageable) {

        // Default to today if no date is passed
        LocalDateTime requestedDate = (dateString != null)
                ? LocalDate.parse(dateString).atStartOfDay()
                : LocalDateTime.now();

        return service.getAllTimeFilteredGroupsForUser(
                userDetails.getUsername(),
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse(DEFAULT_SORT_BY),
                pageable.getSort().stream().findFirst().map(order -> order.getDirection().name()).orElse(DEFAULT_SORT_DIR),
                requestedDate
        );
    }

    @PostMapping("/group")
    public TodoDto createGroup(@RequestBody TodoDto dto,
                               @AuthenticationPrincipal UserDetails userDetails) {
        return service.createTodoGroup(dto, userDetails.getUsername());
    }

    @PutMapping("/{id}")
    public TodoDto updateEntry(@PathVariable int id,
                               @RequestBody TodoDto todoDto,
                               @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return service.updateTodo(id, todoDto, userDetails.getUsername());
    }

    @DeleteMapping("/group/{id}")
    public void deleteGroup(@PathVariable int id,
                            @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        service.delete(id, userDetails.getUsername());
    }
}