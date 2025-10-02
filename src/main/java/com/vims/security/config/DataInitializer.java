package com.vims.security.config;

import com.vims.security.domain.FileUpload;
import com.vims.security.domain.Role;
import com.vims.security.domain.User;
import com.vims.security.repository.FileUploadRepository;
import com.vims.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder, FileUploadRepository fileUploads) {
        return args -> {
            if (users.findByUsername("securitymanager").isEmpty()) {
                users.save(User.builder()
                        .username("securitymanager")
                        .email("securitymanager@example.com")
                        .passwordHash(encoder.encode("1234"))
                        .roles(Set.of(Role.SECURITY_MANAGER))
                        .build());

            }
            if (users.findByUsername("admin").isEmpty()) {
                users.save(User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .passwordHash(encoder.encode("Password1"))
                        .roles(Set.of(Role.ADMIN))
                        .build());
            }
            if (users.findByUsername("user").isEmpty()) {
                users.save(User.builder()
                        .username("user")
                        .email("user@example.com")
                        .passwordHash(encoder.encode("Password1"))
                        .roles(Set.of(Role.USER))
                        .build());
            }
            
            // Add some sample file uploads for testing
            if (fileUploads.count() == 0) {
                fileUploads.save(FileUpload.builder()
                        .username("securitymanager")
                        .originalFilename("sample-document.pdf")
                        .contentType("application/pdf")
                        .sizeBytes(1024000)
                        .blocked(false)
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .build());
                        
                fileUploads.save(FileUpload.builder()
                        .username("user")
                        .originalFilename("malicious.exe")
                        .contentType("application/x-msdownload")
                        .sizeBytes(2048000)
                        .blocked(true)
                        .timestamp(LocalDateTime.now().minusHours(1))
                        .build());
            }
        };
    }
}


