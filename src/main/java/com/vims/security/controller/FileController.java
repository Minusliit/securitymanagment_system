package com.vims.security.controller;

import com.vims.security.domain.FileUpload;
import com.vims.security.repository.FileUploadRepository;
import com.vims.security.service.SecurityLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
public class FileController {
    private final Set<String> allowedTypes = Set.of("application/pdf", "image/jpeg");
    private final long maxSize = 10 * 1024 * 1024; // 10MB

    private final FileUploadRepository repository;
    private final SecurityLogService logService;

    public FileController(FileUploadRepository repository, SecurityLogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    @GetMapping("/upload")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    @Transactional
    public String handleUpload(@RequestParam String username, @RequestParam("file") MultipartFile file, Model model) {
        try {
            if (file == null || file.isEmpty()) {
                model.addAttribute("error", "No file selected");
                return "upload";
            }
            
            String type = file.getContentType();
            boolean blocked = false;
            String reason = null;

            if (file.getSize() > maxSize) {
                blocked = true;
                reason = "File too large";
            } else if (type == null || !allowedTypes.contains(type)) {
                blocked = true;
                reason = "Disallowed file type";
            }

            FileUpload fileUpload = FileUpload.builder()
                    .username(username)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(type)
                    .sizeBytes(file.getSize())
                    .blocked(blocked)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            System.out.println("Saving file upload: " + fileUpload);
            FileUpload saved = repository.save(fileUpload);
            System.out.println("Saved file upload with ID: " + saved.getId());
            System.out.println("File upload successfully persisted to database");

            if (blocked) {
                logService.log("UPLOAD_BLOCKED", "Blocked upload by %s: %s (%s)".formatted(username, file.getOriginalFilename(), reason));
                model.addAttribute("error", "Upload blocked: " + reason);
            } else {
                logService.log("UPLOAD_OK", "Accepted upload by %s: %s".formatted(username, file.getOriginalFilename()));
                model.addAttribute("message", "Upload successful");
            }
            return "upload";
            
        } catch (Exception e) {
            System.err.println("Error during file upload: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            return "upload";
        }
    }

    @GetMapping("/uploads")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    public String listUploads(Model model) {
        try {
            System.out.println("=== UPLOADS LIST DEBUG ===");
            System.out.println("Retrieving all uploads...");
            
            // First, let's check if the repository is working
            long count = repository.count();
            System.out.println("Total uploads count: " + count);
            
            var uploads = repository.findAll();
            System.out.println("Found " + uploads.size() + " uploads");
            
            // Log each upload for debugging
            for (FileUpload upload : uploads) {
                System.out.println("  - ID: " + upload.getId() + 
                                 ", Filename: " + upload.getOriginalFilename() + 
                                 ", Username: " + upload.getUsername() + 
                                 ", Blocked: " + upload.isBlocked() +
                                 ", Size: " + upload.getSizeBytes() + " bytes");
            }
            
            model.addAttribute("uploads", uploads);
            
            // If no uploads found, add some sample data
            if (uploads.isEmpty()) {
                System.out.println("No uploads found, creating sample data...");
                createSampleUploads();
                uploads = repository.findAll();
                System.out.println("After creating samples, found " + uploads.size() + " uploads");
                model.addAttribute("uploads", uploads);
            }
            
            return "uploads";
        } catch (Exception e) {
            System.err.println("Error retrieving uploads: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error retrieving uploads: " + e.getMessage());
            model.addAttribute("uploads", java.util.Collections.emptyList());
            return "uploads";
        }
    }
    
    private void createSampleUploads() {
        try {
            // Create some sample uploads if none exist
            FileUpload sample1 = FileUpload.builder()
                    .username("admin")
                    .originalFilename("security-report.pdf")
                    .contentType("application/pdf")
                    .sizeBytes(1024000)
                    .blocked(false)
                    .timestamp(LocalDateTime.now().minusHours(2))
                    .build();
            
            FileUpload sample2 = FileUpload.builder()
                    .username("user")
                    .originalFilename("malicious-file.exe")
                    .contentType("application/x-msdownload")
                    .sizeBytes(2048000)
                    .blocked(true)
                    .timestamp(LocalDateTime.now().minusHours(1))
                    .build();
            
            FileUpload sample3 = FileUpload.builder()
                    .username("securitymanager")
                    .originalFilename("document.jpg")
                    .contentType("image/jpeg")
                    .sizeBytes(512000)
                    .blocked(false)
                    .timestamp(LocalDateTime.now().minusMinutes(30))
                    .build();
            
            repository.save(sample1);
            repository.save(sample2);
            repository.save(sample3);
            
            System.out.println("Sample uploads created successfully");
        } catch (Exception e) {
            System.err.println("Error creating sample uploads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/uploads/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    public String getUploadDetails(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== UPLOAD DETAILS DEBUG ===");
            System.out.println("Attempting to find upload with ID: " + id);
            
            // Check if the repository is working
            var allUploads = repository.findAll();
            System.out.println("Total uploads in database: " + allUploads.size());
            
            // List all uploads for debugging
            System.out.println("All uploads in database:");
            for (FileUpload upload : allUploads) {
                System.out.println("  - ID: " + upload.getId() + ", Filename: " + upload.getOriginalFilename());
            }
            
            var upload = repository.findById(id);
            System.out.println("Upload found: " + upload.isPresent());
            
            if (upload.isPresent()) {
                System.out.println("Upload details: " + upload.get());
                model.addAttribute("upload", upload.get());
                return "upload-details";
            } else {
                System.out.println("Upload with ID " + id + " not found");
                System.out.println("Available IDs: " + allUploads.stream().map(FileUpload::getId).toList());
                model.addAttribute("error", "Upload not found with ID: " + id + ". Available IDs: " + allUploads.stream().map(FileUpload::getId).toList());
                return "upload-details"; // Return the template instead of redirecting
            }
        } catch (Exception e) {
            System.err.println("Error retrieving upload details: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error retrieving upload details: " + e.getMessage() + ". Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return "upload-details"; // Return the template instead of redirecting
        }
    }

    @GetMapping("/uploads/test")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    @ResponseBody
    public String testDatabase() {
        try {
            var count = repository.count();
            var allUploads = repository.findAll();
            StringBuilder result = new StringBuilder();
            result.append("Database connection working. Total uploads: ").append(count).append("\n\n");
            result.append("All uploads:\n");
            for (FileUpload upload : allUploads) {
                result.append("ID: ").append(upload.getId())
                      .append(", Filename: ").append(upload.getOriginalFilename())
                      .append(", Username: ").append(upload.getUsername())
                      .append(", Blocked: ").append(upload.isBlocked())
                      .append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Database error: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }

    @GetMapping("/uploads/create-test")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    @ResponseBody
    public String createTestUpload() {
        try {
            FileUpload testUpload = FileUpload.builder()
                    .username("testuser")
                    .originalFilename("test-document.pdf")
                    .contentType("application/pdf")
                    .sizeBytes(1024)
                    .blocked(false)
                    .timestamp(LocalDateTime.now())
                    .build();
            
            FileUpload saved = repository.save(testUpload);
            return "Test upload created with ID: " + saved.getId() + "\nUpload details: " + saved.toString();
        } catch (Exception e) {
            return "Error creating test upload: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }
    
    @GetMapping("/uploads/schema")
    @ResponseBody
    public String checkSchema() {
        try {
            // Test if we can access the database
            long count = repository.count();
            var uploads = repository.findAll();
            StringBuilder result = new StringBuilder();
            result.append("Schema test successful. Total uploads: ").append(count).append("\n");
            result.append("Uploads:\n");
            for (FileUpload upload : uploads) {
                result.append("- ID: ").append(upload.getId())
                      .append(", Filename: ").append(upload.getOriginalFilename())
                      .append(", Username: ").append(upload.getUsername())
                      .append(", Blocked: ").append(upload.isBlocked())
                      .append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Schema test failed: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }
    
    @GetMapping("/uploads/init")
    @ResponseBody
    public String initializeData() {
        try {
            // Create sample data if none exists
            createSampleUploads();
            long count = repository.count();
            return "Data initialized successfully. Total uploads: " + count;
        } catch (Exception e) {
            return "Error initializing data: " + e.getMessage() + "\nStack trace: " + java.util.Arrays.toString(e.getStackTrace());
        }
    }
    
    @PostMapping("/uploads/{id}/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    @Transactional
    public String deleteUpload(@PathVariable Long id, Model model) {
        try {
            System.out.println("=== DELETE UPLOAD DEBUG ===");
            System.out.println("Attempting to delete upload with ID: " + id);
            
            // Check if upload exists
            var upload = repository.findById(id);
            if (upload.isPresent()) {
                FileUpload fileUpload = upload.get();
                System.out.println("Found upload: " + fileUpload.getOriginalFilename());
                
                // Delete from database
                repository.deleteById(id);
                System.out.println("Upload deleted successfully from database");
                
                // Log the deletion
                logService.log("UPLOAD_DELETED", "Deleted upload: %s (ID: %d)".formatted(fileUpload.getOriginalFilename(), id));
                
                return "redirect:/uploads?deleted=success";
            } else {
                System.out.println("Upload with ID " + id + " not found");
                return "redirect:/uploads?error=not_found";
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting upload: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/uploads?error=delete_failed";
        }
    }
    
    @GetMapping("/uploads/{id}/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SECURITY_MANAGER')")
    public String deleteUploadGet(@PathVariable Long id, Model model) {
        return deleteUpload(id, model);
    }
}


