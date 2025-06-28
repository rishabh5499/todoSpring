package in.vyomsoft.todo.service.implementation;

import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.ResourceNotFoundException;
import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.repository.TodoRepository;
import in.vyomsoft.todo.repository.UserRepository;
import in.vyomsoft.todo.service.TodoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class todoServiceImpl implements TodoService {

    TodoRepository repository;
    ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;

    public todoServiceImpl(TodoRepository repository, ModelMapper modelMapper, UserRepository userRepository) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }

    @Override
    public List<TodoDto> getAllForUser(String username, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Page<Todo> todosPage = repository.findByUser(user, pageable);

        return todosPage.getContent()
                .stream()
                .map(todo -> modelMapper.map(todo, TodoDto.class))
                .toList();
    }

    @Override
    public TodoDto getTodoById(Long id, String username) throws AccessDeniedException {
        Todo todo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        if (todo.getUser() == null) {
            throw new ResourceNotFoundException("User", "Todo ID", id);
        }

        if (!todo.getUser().getEmail().equals(username) && !todo.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to access this Todo");
        }

        return modelMapper.map(todo, TodoDto.class);
    }

    @Override
    public TodoDto createTodoForUser(TodoDto dto, String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Todo todo = modelMapper.map(dto, Todo.class);
        todo.setUser(user);
        Todo saved = repository.save(todo);
        return modelMapper.map(saved, TodoDto.class);
    }

    @Override
    public TodoDto updateTodo(Long id, TodoDto todo, String username) throws AccessDeniedException {
        Todo selectedTodo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        if (selectedTodo.getUser() == null) {
            throw new ResourceNotFoundException("User", "Todo ID", id);
        }

        if (!selectedTodo.getUser().getEmail().equals(username) && !selectedTodo.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Todo");
        }

        selectedTodo.setName(todo.getName());
        selectedTodo.setDescription(todo.getDescription());
        selectedTodo.setCompleted(todo.isCompleted());
        selectedTodo.setReminder(todo.getReminder());

        Todo updatedTodo = repository.save(selectedTodo);
        return modelMapper.map(updatedTodo, TodoDto.class);
    }

    @Override
    public void delete(Long id, String username) throws AccessDeniedException {
        Todo selectedTodo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        if (selectedTodo.getUser() == null) {
            throw new ResourceNotFoundException("User", "Todo ID", id);
        }

        if (!selectedTodo.getUser().getEmail().equals(username) && !selectedTodo.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to delete this Todo");
        }

        repository.delete(selectedTodo);
    }
}
