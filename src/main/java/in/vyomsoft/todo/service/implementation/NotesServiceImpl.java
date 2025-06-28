package in.vyomsoft.todo.service.implementation;

import in.vyomsoft.todo.entity.Media;
import in.vyomsoft.todo.entity.Notes;
import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.ResourceNotFoundException;
import in.vyomsoft.todo.payload.MediaDTO;
import in.vyomsoft.todo.payload.NotesDto;
import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.repository.NotesRepository;
import in.vyomsoft.todo.repository.UserRepository;
import in.vyomsoft.todo.service.NotesService;
import jakarta.transaction.Transactional;
import org.aspectj.weaver.ast.Not;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    @Transactional
    public NotesDto createNote(NotesDto noteDto, String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Notes note = modelMapper.map(noteDto, Notes.class);
        note.setUser(user);

        if (note.getMedias() != null) {
            note.getMedias().forEach(media -> media.setNote(note));
        }

        Notes saved = repository.save(note);
        return modelMapper.map(saved, NotesDto.class);
    }

//    @Override
//    @Transactional
//    public NotesDto createNote(NotesDto noteDto, String username) {
//        User user = userRepository.findByUsernameOrEmail(username, username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        Notes note = new Notes();
//        note.setTitle(noteDto.getTitle());
//        note.setDescription(noteDto.getDescription());
//        note.setUser(user);
//
//        List<Media> mediaList = noteDto.getMedias().stream()
//                .map(mediaDto -> {
//                    Media media = new Media();
//                    media.setMediaId(mediaDto.getData().getId());
//                    media.setTitle(mediaDto.getData().getTitle());
//                    media.setUrlViewer(mediaDto.getData().getUrl_viewer());
//                    media.setDeleteUrl(mediaDto.getData().getDelete_url());
//
//                    Media.ImageDetail image = new Media.ImageDetail();
//                    image.setFilename(mediaDto.getData().getImage().getFilename());
//                    image.setMime(mediaDto.getData().getImage().getMime());
//                    image.setUrl(mediaDto.getData().getImage().getUrl());
//                    media.setImage(image);
//
//                    Media.ImageDetail thumb = new Media.ImageDetail();
//                    thumb.setFilename(mediaDto.getData().getThumb().getFilename());
//                    thumb.setMime(mediaDto.getData().getThumb().getMime());
//                    thumb.setUrl(mediaDto.getData().getThumb().getUrl());
//                    media.setThumb(thumb);
//
//                    media.setNote(note); // ✅ Link back to the note
//                    return media;
//                })
//                .toList();
//
//        note.setMedias(mediaList);
//
//        Notes saved = repository.save(note);
//        return modelMapper.map(saved, NotesDto.class);
//    }

    @Override
    @Transactional
    public NotesDto updateNote(Long id, NotesDto notesDto, String username) throws AccessDeniedException {
        Notes note = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));

        if (note.getUser() == null) {
            throw new ResourceNotFoundException("User", "Note ID", id);
        }

        if (!note.getUser().getEmail().equals(username) && !note.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Note");
        }

        note.setTitle(notesDto.getTitle());
        note.setDescription(notesDto.getDescription());
        note.setCreatedAt(notesDto.getCreatedAt());
        note.setColour(notesDto.getColour());
        note.setLabel(notesDto.getLabel());

        List<MediaDTO> incomingMediaDTOs = notesDto.getMedias();
        List<Media> existingMedias = note.getMedias();

        // Map of existing media by mediaId
        Map<String, Media> existingMap = existingMedias.stream()
                .collect(Collectors.toMap(Media::getMediaId, Function.identity()));

        // New list after syncing
        List<Media> updatedMediaList = new ArrayList<>();

        for (MediaDTO mediaDTO : incomingMediaDTOs) {
            String mediaId = mediaDTO.getData().getId();
            Media media = existingMap.getOrDefault(mediaId, new Media());

            modelMapper.map(mediaDTO.getData(), media); // map fields from DTO to entity
            media.setMediaId(mediaId); // explicitly set ID (since it's the key)
            media.setNote(note);       // ensure relationship is intact

            updatedMediaList.add(media);
        }

        note.getMedias().clear();               // Remove unlinked media
        note.getMedias().addAll(updatedMediaList); // Add updated media list

        Notes updatedNote = repository.save(note);
        return modelMapper.map(updatedNote, NotesDto.class);
    }

//    @Override
//    @Transactional
//    public NotesDto updateNote(Long id, NotesDto notesDto, String username) throws AccessDeniedException {
//        Notes note = repository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));
//
//        if (note.getUser() == null) {
//            throw new ResourceNotFoundException("User", "Note ID", id);
//        }
//
//        if (!note.getUser().getEmail().equals(username) && !note.getUser().getUsername().equals(username)) {
//            throw new AccessDeniedException("You are not authorized to update this Note");
//        }
//
//        note.setTitle(notesDto.getTitle());
//        note.setDescription(notesDto.getDescription());
//        note.setCreatedAt(notesDto.getCreatedAt());
//
//        // Assuming you updated your DTO to have a List<MediaDTO>
//        List<MediaDTO> incomingMediaDTOs = notesDto.getMedias();
//        List<Media> existingMedias = note.getMedias();
//
//        // Build a map of incoming by mediaId for easy lookup
//        Map<String, MediaDTO> incomingMap = incomingMediaDTOs.stream()
//                .collect(Collectors.toMap(dto -> dto.getData().getId(), Function.identity()));
//
//        // Remove medias that are not in incoming list
//        existingMedias.removeIf(existing ->
//                !incomingMap.containsKey(existing.getMediaId())
//        );
//
//        // Add new medias that don't already exist
//        for (MediaDTO mediaDTO : incomingMediaDTOs) {
//            String mediaId = mediaDTO.getData().getId();
//            boolean alreadyPresent = existingMedias.stream()
//                    .anyMatch(m -> m.getMediaId().equals(mediaId));
//
//            if (!alreadyPresent) {
//                Media media = new Media();
//                media.setMediaId(mediaId);
//                media.setTitle(mediaDTO.getData().getTitle());
//                media.setUrlViewer(mediaDTO.getData().getUrl_viewer());
//                media.setDeleteUrl(mediaDTO.getData().getDelete_url());
//
//                Media.ImageDetail image = new Media.ImageDetail();
//                image.setFilename(mediaDTO.getData().getImage().getFilename());
//                image.setMime(mediaDTO.getData().getImage().getMime());
//                image.setUrl(mediaDTO.getData().getImage().getUrl());
//                media.setImage(image);
//
//                Media.ImageDetail thumb = new Media.ImageDetail();
//                thumb.setFilename(mediaDTO.getData().getThumb().getFilename());
//                thumb.setMime(mediaDTO.getData().getThumb().getMime());
//                thumb.setUrl(mediaDTO.getData().getThumb().getUrl());
//                media.setThumb(thumb);
//
//                media.setNote(note); // Establish link
//                existingMedias.add(media);
//            }
//        }
//
//        Notes updatedNote = repository.save(note);
//        return modelMapper.map(updatedNote, NotesDto.class);
//    }

//    @Override
//    public void deleteNote(Long id, String username) throws AccessDeniedException {
//        Notes selectedNote = repository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Todo", "id", id));
//
//        if (selectedNote.getUser() == null) {
//            throw new ResourceNotFoundException("User", "Todo ID", id);
//        }
//
//        if (!selectedNote.getUser().getEmail().equals(username) && !selectedNote.getUser().getUsername().equals(username)) {
//            throw new AccessDeniedException("You are not authorized to delete this Todo");
//        }
//
//        repository.delete(selectedNote);
//    }
    @Override
    @Transactional
    public void deleteNote(Long id, String username) throws AccessDeniedException {
        Notes selectedNote = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", id));

        if (selectedNote.getUser() == null) {
            throw new ResourceNotFoundException("User", "Note ID", id);
        }

        if (!selectedNote.getUser().getEmail().equals(username) && !selectedNote.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to delete this Note");
        }
        selectedNote.getMedias().clear();

        repository.delete(selectedNote);
    }
}
