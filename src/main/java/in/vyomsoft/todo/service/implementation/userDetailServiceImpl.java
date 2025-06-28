package in.vyomsoft.todo.service.implementation;

import in.vyomsoft.todo.entity.Todo;
import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.ResourceNotFoundException;
import in.vyomsoft.todo.payload.RegisterDto;
import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.payload.UserDetailsDTO;
import in.vyomsoft.todo.repository.TodoRepository;
import in.vyomsoft.todo.repository.UserRepository;
import in.vyomsoft.todo.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
public class userDetailServiceImpl implements UserService {
    private UserRepository repository;
    private PasswordEncoder passwordEncoder;
    private ModelMapper modelMapper;

    public userDetailServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDetailsDTO getUserDetails(String username) throws AccessDeniedException {
        User user = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        return modelMapper.map(user, UserDetailsDTO.class);
    }

    @Override
    public UserDetailsDTO updateUser(UserDetailsDTO user, String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        if (selectedUser.getUsername() == null) {
            throw new ResourceNotFoundException("User", "User Name", selectedUser.getId());
        }

        if (!selectedUser.getEmail().equals(username) && !selectedUser.getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this Todo");
        }

        selectedUser.setName(user.getName());
        selectedUser.setUsername(user.getUsername());
        selectedUser.setPassword(passwordEncoder.encode(user.getPassword()));
        selectedUser.setDpUrl(user.getDpUrl());

        User updatedTodo = repository.save(selectedUser);
        return modelMapper.map(updatedTodo, UserDetailsDTO.class);
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
}
