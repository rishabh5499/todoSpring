package in.vyomsoft.todo.service.implementation;

import in.vyomsoft.todo.entity.Notes;
import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.ResourceNotFoundException;
import in.vyomsoft.todo.payload.NotesDto;
import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.repository.NotesRepository;
import in.vyomsoft.todo.repository.UserRepository;
import in.vyomsoft.todo.service.NotesService;
import org.aspectj.weaver.ast.Not;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotesServiceImpl implements NotesService {

    NotesRepository repository;
    ModelMapper modelMapper;
    @Autowired
    private UserRepository userRepository;

    public NotesServiceImpl(NotesRepository repository, ModelMapper modelMapper, UserRepository userRepository) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }

    @Override
    public List<NotesDto> getAll(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Notes> todos = repository.findByUser(user);
        return todos.stream()
                .map(todo -> modelMapper.map(todo, NotesDto.class))
                .toList();
    }

    @Override
    public NotesDto getNotesById(Long id, String username) throws AccessDeniedException {
        Notes notes = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notes", "id", id));

        if (notes.getUser() == null) {
            throw new ResourceNotFoundException("User", "Notes ID", id);
        }

        if (!notes.getUser().getEmail().equals(username) && !notes.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to access this Note");
        }

        return modelMapper.map(notes, NotesDto.class);
    }

    @Override
    public NotesDto createNote(NotesDto note, String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Notes notes = modelMapper.map(note, Notes.class);
        notes.setUser(user);
        Notes saved = repository.save(notes);
        return modelMapper.map(saved, NotesDto.class);
    }

    @Override
    public NotesDto updateNote(Long id, NotesDto notesDto, String username) throws AccessDeniedException {
        Notes note = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));

        if (note.getUser() == null) {
            throw new ResourceNotFoundException("User", "Note ID", id);
        }

        if (!note.getUser().getEmail().equals(username) && !note.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Todo");
        }

        note.setTitle(notesDto.getTitle());
        note.setDescription(notesDto.getDescription());
        note.setCreatedAt(notesDto.getCreatedAt());
        note.setMedias(notesDto.getMedias());

        Notes updatedNote = repository.save(note);
        return modelMapper.map(updatedNote, NotesDto.class);
    }

    @Override
    public void deleteNote(Long id, String username) throws AccessDeniedException {
        Notes selectedNote = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));

        if (selectedNote.getUser() == null) {
            throw new ResourceNotFoundException("User", "Todo ID", id);
        }

        if (!selectedNote.getUser().getEmail().equals(username) && !selectedNote.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to delete this Todo");
        }

        repository.delete(selectedNote);
    }
}
