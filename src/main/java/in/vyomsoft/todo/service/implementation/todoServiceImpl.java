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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class todoServiceImpl implements TodoService {

    private final TodoRepository repository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public todoServiceImpl(TodoRepository repository, ModelMapper modelMapper, UserRepository userRepository) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }

    @Override
    public TodoDto getTodoById(int id, String username) throws AccessDeniedException {
        Todo todo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", (long) id));

        if (!todo.getUser().getUsername().equals(username) && !todo.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("Unauthorized access to this task");
        }

        return modelMapper.map(todo, TodoDto.class);
    }

    public List<TodoDto> getAllTimeFilteredGroupsForUser(
            String username, int pageNo, int pageSize, String sortBy, String sortDir,
            LocalDate requestedDate, String timezoneId) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        ZoneId userZone = ZoneId.of(timezoneId);
        ZonedDateTime localStart = requestedDate.atStartOfDay(userZone);
        ZonedDateTime localEnd = requestedDate.atTime(23, 59, 59, 999999999).atZone(userZone);
        LocalDateTime startInUtc = localStart.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        LocalDateTime endInUtc = localEnd.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        Page<Todo> groupsPage = repository.findByUserAndParentIsNullAndCreatedAtBetween(
                user, startInUtc, endInUtc, pageable);

        return groupsPage.getContent().stream()
                .map(todo -> modelMapper.map(todo, TodoDto.class))
                .toList();
    }

    @Override
    public List<TodoDto> getAllGroupsForUser(String username, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch only parents (Groups)
        Page<Todo> groupsPage = repository.findByUserAndParentIsNull(user, pageable);

        return groupsPage.getContent().stream()
                .map(todo -> modelMapper.map(todo, TodoDto.class))
                .toList();
    }

    @Override
    public TodoDto createTodoGroup(TodoDto dto, String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Todo parent = modelMapper.map(dto, Todo.class);
        parent.setUser(user);

        if (parent.getSubTasks() != null) {
            parent.getSubTasks().clear();
        } else {
            parent.setSubTasks(new ArrayList<>());
        }

        if (dto.getSubTasks() != null) {
            dto.getSubTasks().forEach(subDto -> {
                Todo child = modelMapper.map(subDto, Todo.class);
                child.setUser(user);
                child.setParent(parent);
                parent.getSubTasks().add(child);
            });
        }

        Todo saved = repository.save(parent);
        return modelMapper.map(saved, TodoDto.class);
    }

    @Override
    public TodoDto updateTodo(int id, TodoDto dto, String username) throws AccessDeniedException {
        Todo existingGroup = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", (long) id));

        if (!existingGroup.getUser().getUsername().equals(username) && !existingGroup.getUser().getEmail().equals(username)) {
            throw new AccessDeniedException("Unauthorized update");
        }

        // 1. Update Parent Fields
        existingGroup.setName(dto.getName());
        existingGroup.setDescription(dto.getDescription());
        existingGroup.setCompleted(dto.isCompleted());

        // 2. Efficiently Update Subtasks
        List<Todo> incomingSubTasks = new ArrayList<>();

        if (dto.getSubTasks() != null) {
            for (TodoDto subDto : dto.getSubTasks()) {
                if (subDto.getId() > 0) {
                    // Find existing subtask to update
                    existingGroup.getSubTasks().stream()
                            .filter(s -> s.getId() == subDto.getId())
                            .findFirst()
                            .ifPresent(existingSub -> {
                                existingSub.setName(subDto.getName());
                                existingSub.setCompleted(subDto.isCompleted());
                                incomingSubTasks.add(existingSub);
                            });
                } else {
                    // It's a new subtask
                    Todo newSub = modelMapper.map(subDto, Todo.class);
                    newSub.setUser(existingGroup.getUser());
                    newSub.setParent(existingGroup);
                    incomingSubTasks.add(newSub);
                }
            }
        }

        // 3. Remove orphans (tasks that were in DB but not in the new list)
        existingGroup.getSubTasks().clear();
        existingGroup.getSubTasks().addAll(incomingSubTasks);

        // 4. Automatic Parent Completion Check
        boolean allSubTasksDone = !incomingSubTasks.isEmpty() &&
                incomingSubTasks.stream().allMatch(Todo::isCompleted);

        existingGroup.setCompleted(allSubTasksDone);

        Todo updated = repository.save(existingGroup);
        return modelMapper.map(updated, TodoDto.class);
    }

    @Override
    public void delete(int id, String username) throws AccessDeniedException {
        Todo group = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", (long) id));

        if (!group.getUser().getEmail().equals(username) && !group.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Unauthorized deletion");
        }

        repository.delete(group);
    }
}
