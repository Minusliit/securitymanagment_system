package com.vims.security.service;

import com.vims.security.domain.Role;
import com.vims.security.domain.User;
import com.vims.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PasswordPolicyService passwordPolicyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;
    }

    @Transactional
    public User register(String username, String email, String rawPassword, Set<Role> roles) {
        passwordPolicyService.validatePassword(rawPassword);
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .twoFactorEnabled(false)
                .build();
        return userRepository.save(user);
    }
}


