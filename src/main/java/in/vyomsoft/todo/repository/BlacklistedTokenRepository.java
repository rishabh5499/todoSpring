package in.vyomsoft.todo.repository;

import in.vyomsoft.todo.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
    void deleteAllByExpiryDateBefore(LocalDateTime time);
}