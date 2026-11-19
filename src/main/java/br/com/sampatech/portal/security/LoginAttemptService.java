package br.com.sampatech.portal.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private record Attempt(int count, Instant firstFailure) {}

    public void loginFailed(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        attempts.merge(normalize(email), new Attempt(1, Instant.now()),
                (current, first) -> isExpired(current) ? first : new Attempt(current.count() + 1, current.firstFailure()));
    }

    public void loginSucceeded(String email) {
        if (email != null) {
            attempts.remove(normalize(email));
        }
    }

    public boolean isBlocked(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String key = normalize(email);
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return false;
        }
        if (isExpired(attempt)) {
            attempts.remove(key);
            return false;
        }
        return attempt.count() >= MAX_ATTEMPTS;
    }

    private boolean isExpired(Attempt attempt) {
        return attempt.firstFailure().plus(LOCK_TIME).isBefore(Instant.now());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
