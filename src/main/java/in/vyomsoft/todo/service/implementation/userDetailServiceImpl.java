package in.vyomsoft.todo.service.implementation;

import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.ResourceNotFoundException;
import in.vyomsoft.todo.payload.*;
import in.vyomsoft.todo.repository.TodoRepository;
import in.vyomsoft.todo.repository.UserRepository;
import in.vyomsoft.todo.service.ImgBBService;
import in.vyomsoft.todo.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class userDetailServiceImpl implements UserService {
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private ModelMapper modelMapper;
    private ImgBBService imgBBService;

    public userDetailServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, ImgBBService imgBBService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.imgBBService = imgBBService;
    }

    @Override
    public UserDetailsDTO getUserDetails(String username) throws AccessDeniedException {
        User user = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        return modelMapper.map(user, UserDetailsDTO.class);
    }

    @Override
    @Transactional
    public UserDetailsDTO updateUser(UserDetailsDTO user, String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDate now = LocalDate.now();

        if (selectedUser.getPictureChangeWindowStart() == null ||
                selectedUser.getPictureChangeWindowStart().plusMonths(3).isBefore(now)) {
            selectedUser.setPictureChangeWindowStart(now);
            selectedUser.setPictureChangeCount(0);
        }

        int maxAllowedChanges = 3;
        if (!Objects.equals(user.getDpUrl(), selectedUser.getDpUrl())) {
            if (selectedUser.getPictureChangeCount() >= maxAllowedChanges) {
                throw new AccessDeniedException("Picture change limit exceeded for current 3-month window.");
            }
            selectedUser.setPictureChangeCount(selectedUser.getPictureChangeCount() + 1);
        }

        selectedUser.setName(user.getName());
        selectedUser.setUsername(user.getUsername());
        selectedUser.setDpUrl(user.getDpUrl());
        selectedUser.setDeleteUrl(user.getDeleteUrl());

        User updated = repository.save(selectedUser);
        return modelMapper.map(updated, UserDetailsDTO.class);
    }

    @Override
    public String updatePassword(PassswordDTO user, String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        if (selectedUser.getUsername() == null) {
            throw new ResourceNotFoundException("User", "User Name", selectedUser.getId());
        }

        if (!selectedUser.getEmail().equals(username) && !selectedUser.getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Todo");
        }

        selectedUser.setPassword(passwordEncoder.encode(user.getNewPassword()));

        User updatedTodo = repository.save(selectedUser);
        return "Password Updated Successfully";
    }

    @Override
    public void delete(String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        if (selectedUser.getUsername() == null) {
            throw new ResourceNotFoundException("User", "User Name", selectedUser.getId());
        }

        if (!selectedUser.getEmail().equals(username) && !selectedUser.getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Todo");
        }

        repository.delete(selectedUser);
    }

    public PictureLimitDTO getPictureChangeLimit(String username) {
        User user = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDate now = LocalDate.now();

        if (user.getPictureChangeWindowStart() == null ||
                user.getPictureChangeWindowStart().plusMonths(3).isBefore(now)) {
            // 3-month window expired → reset quota
            user.setPictureChangeWindowStart(now);
            user.setPictureChangeCount(0);
            repository.save(user);
        }

        int maxAllowedChanges = 3;
        int used = user.getPictureChangeCount();
        int remaining = Math.max(0, maxAllowedChanges - used);

        return new PictureLimitDTO(maxAllowedChanges, used, remaining);
    }

    private boolean isPictureChangeLimitExceeded(User user, int maxAllowedChanges) {
        LocalDate now = LocalDate.now();

        if (user.getPictureChangeWindowStart() == null ||
                user.getPictureChangeWindowStart().isBefore(now.withDayOfMonth(1))) {
            // New month — reset window
            user.setPictureChangeWindowStart(now.withDayOfMonth(1));
            user.setPictureChangeCount(0);
        }

        return user.getPictureChangeCount() >= maxAllowedChanges;
    }
}
