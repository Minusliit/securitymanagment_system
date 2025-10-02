package com.vims.security.security;

import com.vims.security.domain.LoginAttempt;
import com.vims.security.repository.LoginAttemptRepository;
import com.vims.security.service.BreachDetectionService;
import com.vims.security.service.SecurityLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LoginAttemptListener {
    private final LoginAttemptRepository loginAttemptRepository;
    private final SecurityLogService securityLogService;
    private final BreachDetectionService breachDetectionService;
    private final HttpServletRequest request;

    public LoginAttemptListener(LoginAttemptRepository loginAttemptRepository,
                                SecurityLogService securityLogService,
                                BreachDetectionService breachDetectionService,
                                HttpServletRequest request) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.securityLogService = securityLogService;
        this.breachDetectionService = breachDetectionService;
        this.request = request;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        handleSuccess(event.getAuthentication());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        handleFailure(event.getAuthentication());
    }

    private void handleSuccess(Authentication authentication) {
        String username = authentication.getName();
        saveAttempt(username, true);
        securityLogService.log("LOGIN_SUCCESS", "User %s logged in".formatted(username));
    }

    private void handleFailure(Authentication authentication) {
        String username = authentication.getName();
        saveAttempt(username, false);
        securityLogService.log("LOGIN_FAILURE", "Failed login for %s".formatted(username));
        if (breachDetectionService.isSuspicious(username)) {
            securityLogService.log("ALERT", "Suspicious activity detected for %s".formatted(username));
        }
    }

    private void saveAttempt(String username, boolean success) {
        String ip = request.getRemoteAddr();
        LoginAttempt attempt = LoginAttempt.builder()
                .username(username)
                .success(success)
                .ipAddress(ip)
                .timestamp(LocalDateTime.now())
                .build();
        loginAttemptRepository.save(attempt);
    }
}


