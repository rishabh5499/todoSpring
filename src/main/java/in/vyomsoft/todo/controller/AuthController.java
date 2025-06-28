package in.vyomsoft.todo.controller;

import in.vyomsoft.todo.entity.BlacklistedToken;
import in.vyomsoft.todo.payload.JwtAuthResponse;
import in.vyomsoft.todo.payload.LoginDto;
import in.vyomsoft.todo.payload.RegisterDto;
import in.vyomsoft.todo.repository.BlacklistedTokenRepository;
import in.vyomsoft.todo.security.JwtTokenProvider;
import in.vyomsoft.todo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private AuthService authService;
    private final BlacklistedTokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, BlacklistedTokenRepository tokenRepository, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.tokenRepository = tokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping(value = {"/login", "/signin"})
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        String token = authService.login(loginDto);
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        return ResponseEntity.ok(jwtAuthResponse);
    }

    @PostMapping(value = {"/register", "/signup"})
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        return new ResponseEntity<>(authService.register(registerDto), HttpStatus.CREATED);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            Date expiry = jwtTokenProvider.getExpiryDate(token);

            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setToken(token);
            blacklistedToken.setExpiryDate(expiry.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            tokenRepository.save(blacklistedToken);
        }

        return ResponseEntity.ok("Logged out successfully.");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
