package in.vyomsoft.todo.controller;

import in.vyomsoft.todo.payload.RegisterDto;
import in.vyomsoft.todo.payload.TodoDto;
import in.vyomsoft.todo.payload.UserDetailsDTO;
import in.vyomsoft.todo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/userDetails")
public class UserDetailsController {
    private UserService service;

    public UserDetailsController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public UserDetailsDTO getUserDetails(@AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        return service.getUserDetails(userDetails.getUsername());
    }

    @PutMapping
    public UserDetailsDTO updateUserDetails(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDetailsDTO user) throws AccessDeniedException {
        return service.updateUser(user, userDetails.getUsername());
    }

    @DeleteMapping
    public void deleteEntry(@AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        service.delete(userDetails.getUsername());
    }
}
