package in.vyomsoft.todo.controller;

import in.vyomsoft.todo.payload.*;
import in.vyomsoft.todo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    public UserDetailsDTO getUser(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
    ) throws AccessDeniedException {
        // Get IP from request header (handling proxies like Nginx)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();

        return service.getUserDetails(userDetails.getUsername(), ip);
    }

    @GetMapping("/picture-limit")
    public PictureLimitDTO getPictureChangeLimit(@AuthenticationPrincipal UserDetails userDetails) {
        return service.getPictureChangeLimit(userDetails.getUsername());
    }

    @PutMapping
    public UserDetailsDTO updateUserDetails(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDetailsDTO user) throws AccessDeniedException {
        return service.updateUser(user, userDetails.getUsername());
    }

    @PutMapping("/password")
    public String updatePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PassswordDTO user) throws AccessDeniedException {
        return service.updatePassword(user, userDetails.getUsername());
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        return service.generateOtp(email);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        return service.resetPasswordWithOtp(request);
    }

    @DeleteMapping
    public void deleteEntry(@AuthenticationPrincipal UserDetails userDetails) throws AccessDeniedException {
        service.delete(userDetails.getUsername());
    }
}
