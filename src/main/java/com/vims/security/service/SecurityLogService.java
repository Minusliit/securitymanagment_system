package com.vims.security.service;

import com.vims.security.domain.SecurityLog;
import com.vims.security.repository.SecurityLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SecurityLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityLogService.class);
    private final SecurityLogRepository repository;

    public SecurityLogService(SecurityLogRepository repository) {
        this.repository = repository;
    }

    public void log(String eventType, String message) {
        LOGGER.info("[SECURITY] {} - {}", eventType, message);
        SecurityLog log = SecurityLog.builder()
                .eventType(eventType)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        repository.save(log);
    }
}


