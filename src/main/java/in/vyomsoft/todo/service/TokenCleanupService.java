package in.vyomsoft.todo.service;

import in.vyomsoft.todo.repository.BlacklistedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {

    private final BlacklistedTokenRepository tokenRepository;

    public TokenCleanupService(BlacklistedTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanBlacklist() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteAllByExpiryDateBefore(now);
    }
}