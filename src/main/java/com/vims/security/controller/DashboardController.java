package com.vims.security.controller;

import com.vims.security.repository.FileUploadRepository;
import com.vims.security.repository.LoginAttemptRepository;
import com.vims.security.repository.SecurityLogRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@PreAuthorize("hasAnyRole('SECURITY_MANAGER','ADMIN')")
public class DashboardController {
    private final LoginAttemptRepository loginAttemptRepository;
    private final SecurityLogRepository securityLogRepository;
    private final FileUploadRepository fileUploadRepository;

    public DashboardController(LoginAttemptRepository loginAttemptRepository, SecurityLogRepository securityLogRepository, FileUploadRepository fileUploadRepository) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.securityLogRepository = securityLogRepository;
        this.fileUploadRepository = fileUploadRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        var loginAttempts = loginAttemptRepository.findAll();
        var securityLogs = securityLogRepository.findAll();
        var uploads = fileUploadRepository.findAll();
        
        System.out.println("Dashboard - Login attempts: " + loginAttempts.size());
        System.out.println("Dashboard - Security logs: " + securityLogs.size());
        System.out.println("Dashboard - File uploads: " + uploads.size());
        
        // Debug: Print all file uploads
        for (var upload : uploads) {
            System.out.println("File upload: " + upload);
        }
        
        model.addAttribute("loginAttempts", loginAttempts);
        model.addAttribute("securityLogs", securityLogs);
        model.addAttribute("uploads", uploads);
        return "dashboard";
    }

    @PostMapping("/security/login-attempts/{id}/delete")
    @Transactional
    public String deleteLoginAttempt(@PathVariable Long id) {
        try {
            loginAttemptRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) { }
        return "redirect:/dashboard";
    }
    @GetMapping("/security/login-attempts/{id}/delete")
    public String deleteLoginAttemptGet(@PathVariable Long id) { return deleteLoginAttempt(id); }

    @PostMapping("/security/security-logs/{id}/delete")
    @Transactional
    public String deleteSecurityLog(@PathVariable Long id) {
        try {
            securityLogRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) { }
        return "redirect:/dashboard";
    }
    @GetMapping("/security/security-logs/{id}/delete")
    public String deleteSecurityLogGet(@PathVariable Long id) { return deleteSecurityLog(id); }

    @PostMapping("/security/uploads/{id}/delete")
    @Transactional
    public String deleteUpload(@PathVariable Long id) {
        try {
            fileUploadRepository.deleteById(id);
        } catch (EmptyResultDataAccessException ignored) { }
        return "redirect:/dashboard";
    }
    @GetMapping("/security/uploads/{id}/delete")
    public String deleteUploadGet(@PathVariable Long id) { return deleteUpload(id); }
}


