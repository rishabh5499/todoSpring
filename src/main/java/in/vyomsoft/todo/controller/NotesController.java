package in.vyomsoft.todo.controller;

import in.vyomsoft.todo.payload.NotesDto;
import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.service.NotesService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

import static in.vyomsoft.todo.utils.AppConstants.*;
import static in.vyomsoft.todo.utils.AppConstants.DEFAULT_SORT_DIR;

@RestController
@RequestMapping("/notes")
public class NotesController {
    private NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @GetMapping
    public List<NotesDto> getAll(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(value = "pageNo", defaultValue = DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                @RequestParam(value = "sortBy", defaultValue = DEFAULT_SORT_BY, required = false) String sortBy,
                                @RequestParam(value = "sortDir", defaultValue = DEFAULT_SORT_DIR, required = false) String sortDir) {
        return notesService.getAll(userDetails.getUsername());
    }

    @GetMapping("/{id}")
    private NotesDto getNoteById(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return notesService.getNotesById(id, userDetails.getUsername());
    }

    @PostMapping
    private NotesDto createNote(@RequestBody NotesDto notesDto, @AuthenticationPrincipal UserDetails userDetails) {
        return notesService.createNote(notesDto, userDetails.getUsername());
    }

    @PutMapping("/{id}")
    private NotesDto updateNote(@PathVariable Long id, @RequestBody NotesDto notesDto,
                                @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return notesService.updateNote(id, notesDto, userDetails.getUsername());
    }

    @DeleteMapping("/{id}")
    private void deleteNote(@PathVariable Long id,
                            @AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        notesService.deleteNote(id, userDetails.getUsername());
    }
}
