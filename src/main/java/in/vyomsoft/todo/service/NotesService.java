package in.vyomsoft.todo.service;

import in.vyomsoft.todo.payload.NotesDto;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface NotesService {
    List<NotesDto> getAll(String username);
    NotesDto getNotesById(Long id, String username) throws AccessDeniedException;
    NotesDto createNote(NotesDto todo, String username);
    NotesDto updateNote(Long id, NotesDto todo, String username) throws AccessDeniedException;
    void deleteNote(Long id, String username) throws AccessDeniedException;
}
