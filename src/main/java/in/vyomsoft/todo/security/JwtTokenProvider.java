package in.vyomsoft.todo.security;

import in.vyomsoft.todo.entity.User;
import in.vyomsoft.todo.exception.TodoAPIException;
import in.vyomsoft.todo.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;
    @Autowired
    private UserRepository userRepository;

    public String generateToken(Authentication authentication) {
        String usernameOrEmail = authentication.getName();

        // Fetch user from DB
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Long userId = user.getId();
        String username = user.getUsername();
        String email = user.getEmail();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserId(String token) {
        return Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(token).getPayload();

        return claims.get("username", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build().parse(token);
            return true;
        } catch (MalformedJwtException exception) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Invalid JWT token");
        } catch (ExpiredJwtException exception) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Expired JWT token");
        } catch (UnsupportedJwtException exception) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Unsupported JWT token");
        } catch (IllegalArgumentException exception) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "JWT claims string is null or empty");
        }
    }
}
