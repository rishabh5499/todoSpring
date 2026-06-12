package in.vyomsoft.todo.service.implementation;

import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.ResourceNotFoundException;
import in.vyomsoft.todo.payload.*;
import in.vyomsoft.todo.repository.UserRepository;
import in.vyomsoft.todo.service.S3Service;
import in.vyomsoft.todo.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
public class userDetailServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final S3Service s3Service;

    @Value("${weather.api.key}")
    private String weatherApiKey;

    @Autowired
    private JavaMailSender mailSender;

    public userDetailServiceImpl(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            ModelMapper modelMapper,
            S3Service s3Service) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.s3Service = s3Service;
    }

    @Override
    public UserDetailsDTO getUserDetails(String username, String ipAddress) {
        User user = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetailsDTO dto = modelMapper.map(user, UserDetailsDTO.class);

        try {
            String url = String.format(
                    "https://api.weatherapi.com/v1/current.json?key=%s&q=%s",
                    weatherApiKey,
                    ipAddress
            );
            RestTemplate restTemplate = new RestTemplate();
            WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

            if (response != null && response.getCurrent() != null) {
                WeatherResponse.Condition condition = response.getCurrent().getCondition();

                WeatherDTO weatherDto = new WeatherDTO();
                weatherDto.setText(condition.getText());
                weatherDto.setIcon("https:" + condition.getIcon());

                dto.setWeather(weatherDto);
            }
        } catch (Exception e) {
            WeatherDTO fallback = new WeatherDTO();
            fallback.setText("");
            dto.setWeather(fallback);
        }

        return dto;
    }

    @Override
    @Transactional
    public UserDetailsDTO updateUser(UserDetailsDTO userDto, String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isImageChanging = userDto.getDpUrl() != null &&
                !userDto.getDpUrl().equals(selectedUser.getDpUrl());

        if (isImageChanging) {
            String oldS3Url = selectedUser.getDpUrl();

            if (oldS3Url != null && !oldS3Url.isEmpty()) {
                s3Service.deleteImageByUrl(oldS3Url);
                System.out.println("Cleaned up old image: " + oldS3Url);
            }
            selectedUser.setPictureChangeCount(selectedUser.getPictureChangeCount() + 1);
        }

        selectedUser.setName(userDto.getName());
        selectedUser.setUsername(userDto.getUsername());
        selectedUser.setDpUrl(userDto.getDpUrl());

        User updatedUser = repository.save(selectedUser);
        return modelMapper.map(updatedUser, UserDetailsDTO.class);
    }

    @Override
    public String updatePassword(PassswordDTO user, String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        if (!selectedUser.getEmail().equals(username) && !selectedUser.getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to update this password");
        }

        selectedUser.setPassword(passwordEncoder.encode(user.getNewPassword()));
        repository.save(selectedUser);
        return "Password Updated Successfully";
    }

    @Override
    public void delete(String username) throws AccessDeniedException {
        User selectedUser = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        if (!selectedUser.getEmail().equals(username) && !selectedUser.getUsername().equals(username)) {
            throw new AccessDeniedException("You are not authorized to delete this account");
        }

        // Cleanup profile picture from S3 before deleting user
        if (selectedUser.getDpUrl() != null) {
            s3Service.deleteImageByUrl(selectedUser.getDpUrl());
        }

        repository.delete(selectedUser);
    }

    @Override
    public PictureLimitDTO getPictureChangeLimit(String username) {
        User user = repository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDate now = LocalDate.now();

        if (user.getPictureChangeWindowStart() == null ||
                user.getPictureChangeWindowStart().plusMonths(3).isBefore(now)) {
            user.setPictureChangeWindowStart(now);
            user.setPictureChangeCount(0);
            repository.save(user);
        }

        int maxAllowedChanges = 3;
        int used = user.getPictureChangeCount();
        int remaining = Math.max(0, maxAllowedChanges - used);

        return new PictureLimitDTO(maxAllowedChanges, used, remaining);
    }

    @Override
    public String generateOtp(String identifier) {
        User user = repository.findByEmail(identifier)
                .or(() -> repository.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with input: " + identifier));
        String targetEmail = user.getEmail();

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Set OTP and Expiry (5 minutes)
        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        repository.save(user);

        // Trigger Email to the found user's email address
        sendOtpEmail(targetEmail, otp);

        return "OTP sent successfully to " + (targetEmail);
    }

    @Override
    @Transactional
    public String resetPasswordWithOtp(ResetPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .or(() -> repository.findByUsername(request.getEmail()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Validate OTP and Expiry
        if (user.getResetOtp() != null &&
                user.getResetOtp().equals(request.getOtp()) &&
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {

            // Update password and clear OTP fields
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setResetOtp(null);
            user.setOtpExpiry(null);
            repository.save(user);

            return "Password reset successfully!";
        } else {
            throw new RuntimeException("Invalid or expired OTP");
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("qavyomsoft@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your Noti Password Reset OTP");
        message.setText("Hello,\n\nYou requested a password reset for Noti. Your OTP is: " + otp +
                "\n\nIt will expire in 5 minutes. If you didn't request this, ignore this email.");
        mailSender.send(message);
    }
}