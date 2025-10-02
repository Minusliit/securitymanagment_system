package com.vims.security.service;

import com.vims.security.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
    private final AppProperties properties;

    public PasswordPolicyService(AppProperties properties) {
        this.properties = properties;
    }

    public void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < properties.getMinLength()) {
            throw new IllegalArgumentException("Password must be at least " + properties.getMinLength() + " characters long");
        }
        if (properties.isRequireUppercase() && rawPassword.chars().noneMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("Password must contain an uppercase letter");
        }
        if (properties.isRequireNumber() && rawPassword.chars().noneMatch(Character::isDigit)) {
            throw new IllegalArgumentException("Password must contain a number");
        }
    }
}


