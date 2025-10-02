package com.vims.security.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "file_upload")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = true)
    private String username;

    @Column(name = "original_filename", length = 500)
    private String originalFilename;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "blocked")
    private boolean blocked;

    @Column(name = "uploaded_at")
    private LocalDateTime timestamp;
    
    // Add a constructor for debugging
    @Override
    public String toString() {
        return "FileUpload{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", blocked=" + blocked +
                ", timestamp=" + timestamp +
                '}';
    }
}


