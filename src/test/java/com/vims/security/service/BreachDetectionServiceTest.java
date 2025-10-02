package com.vims.security.service;

import com.vims.security.domain.LoginAttempt;
import com.vims.security.repository.LoginAttemptRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class BreachDetectionServiceTest {
    @Test
    void flags_user_after_threshold_failures() {
        LocalDateTime now = LocalDateTime.now();
        List<LoginAttempt> attempts = List.of(
                LoginAttempt.builder().username("a").success(false).timestamp(now.minusMinutes(5)).build(),
                LoginAttempt.builder().username("a").success(false).timestamp(now.minusMinutes(4)).build(),
                LoginAttempt.builder().username("a").success(false).timestamp(now.minusMinutes(3)).build(),
                LoginAttempt.builder().username("a").success(false).timestamp(now.minusMinutes(2)).build(),
                LoginAttempt.builder().username("a").success(false).timestamp(now.minusMinutes(1)).build()
        );

        LoginAttemptRepository repo = Mockito.mock(LoginAttemptRepository.class);
        when(repo.findByUsernameAndTimestampAfter(eq("a"), any(LocalDateTime.class))).thenReturn(attempts);
        when(repo.findByUsernameAndTimestampAfter(eq("b"), any(LocalDateTime.class))).thenReturn(List.of());

        BreachDetectionService svc = new BreachDetectionService(repo);
        assertTrue(svc.isSuspicious("a"));
        assertFalse(svc.isSuspicious("b"));
    }
}


