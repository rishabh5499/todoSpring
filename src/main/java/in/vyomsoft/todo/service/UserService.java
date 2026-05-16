package in.vyomsoft.todo.service;

import in.vyomsoft.todo.payload.*;

import java.nio.file.AccessDeniedException;

public interface UserService {
    UserDetailsDTO getUserDetails(String username, String ipAddress) throws AccessDeniedException;
    UserDetailsDTO updateUser(UserDetailsDTO user, String username) throws AccessDeniedException;
    String updatePassword(PassswordDTO user, String username) throws AccessDeniedException;
    PictureLimitDTO getPictureChangeLimit(String username);
    void delete(String username) throws AccessDeniedException;
    String generateOtp(String email);
    String resetPasswordWithOtp(ResetPasswordRequest request);
}
