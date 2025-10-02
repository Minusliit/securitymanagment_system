package com.vims.security.service;

import com.vims.security.domain.LoginAttempt;
import com.vims.security.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BreachDetectionService {
    private final LoginAttemptRepository loginAttemptRepository;

    @Value("${security.breach.failed-login-threshold:5}")
    private int failedLoginThreshold;

    public BreachDetectionService(LoginAttemptRepository loginAttemptRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
    }

    public boolean isSuspicious(String username) {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<LoginAttempt> attempts = loginAttemptRepository.findByUsernameAndTimestampAfter(username, since);
        long failed = attempts.stream().filter(a -> !a.isSuccess()).count();
        return failed >= failedLoginThreshold;
    }
}


