package in.vyomsoft.todo.service;

import in.vyomsoft.todo.payload.RegisterDto;
import in.vyomsoft.todo.payload.UserDetailsDTO;

import java.nio.file.AccessDeniedException;

public interface UserService {
    UserDetailsDTO getUserDetails(String username) throws AccessDeniedException;
    UserDetailsDTO updateUser(UserDetailsDTO user, String username) throws AccessDeniedException;
    void delete(String username) throws AccessDeniedException;
}
