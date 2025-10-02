package com.vims.security.controller;

import com.vims.security.domain.Role;
import com.vims.security.domain.User;
import com.vims.security.repository.UserRepository;
import com.vims.security.service.QrCodeService;
import com.vims.security.service.TotpService;
import com.vims.security.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Set;

@Controller
@Validated
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final QrCodeService qrCodeService;

    public AuthController(UserService userService, UserRepository userRepository, TotpService totpService, QrCodeService qrCodeService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam @NotBlank String username,
                           @RequestParam @Email String email,
                           @RequestParam @NotBlank String password,
                           Model model) {
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }
        userService.register(username, email, password, Set.of(Role.USER));
        return "redirect:/login?registered";
    }

    @GetMapping("/2fa")
    public String twoFactorPage(Model model, HttpSession session, @RequestParam(required = false) String error) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        model.addAttribute("username", username);
        
        if (error != null) {
            if ("user_not_found".equals(error)) {
                model.addAttribute("error", "User not found");
            }
        }
        
        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("enabled", user != null && user.isTwoFactorEnabled());
            model.addAttribute("secret", user != null ? user.getTwoFactorSecret() : null);
            if (user != null && user.getTwoFactorSecret() != null) {
                String otpauth = "otpauth://totp/VIMS:" + username + "?secret=" + user.getTwoFactorSecret() + "&issuer=VIMS&algorithm=SHA1&digits=6&period=30";
                model.addAttribute("otpauth", otpauth);
            }
        }
        Object verified = session.getAttribute("2faVerified");
        model.addAttribute("verified", verified instanceof Boolean && (Boolean) verified);
        return "twofactor";
    }

    @PostMapping("/2fa/enable")
    public String enable2fa(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/2fa?error=user_not_found";
        }
        
        String secret = totpService.generateSecret();
        user.setTwoFactorEnabled(true);
        user.setTwoFactorSecret(secret);
        userRepository.save(user);
        
        session.setAttribute("2faVerified", false);
        
        return "redirect:/2fa";
    }

    @PostMapping("/2fa/disable")
    public String disable2fa(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return "redirect:/2fa?error=user_not_found";
        }
        
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        
        session.setAttribute("2faVerified", false);
        
        return "redirect:/2fa";
    }

    @PostMapping("/2fa/verify")
    public String verify2fa(@RequestParam int code, HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        if (user == null || !user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
            model.addAttribute("error", "2FA not enabled for this user");
            return "twofactor";
        }
        boolean ok = totpService.verifyCode(user.getTwoFactorSecret(), code);
        if (ok) {
            session.setAttribute("2faVerified", true);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", "Invalid code");
        model.addAttribute("username", username);
        model.addAttribute("enabled", true);
        return "twofactor";
    }

    @PostMapping("/2fa/regenerate")
    public String regenerate2fa(Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "twofactor";
        }
        
        // Generate new secret
        String newSecret = totpService.generateSecret();
        user.setTwoFactorSecret(newSecret);
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        
        // Reset verification status
        session.setAttribute("2faVerified", false);
        
        // Redirect back to 2FA page with new secret
        return "redirect:/2fa";
    }

    @GetMapping("/2fa/qr")
    @ResponseBody
    public ResponseEntity<byte[]> getQrCode(HttpSession session) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            System.out.println("QR Code Request - Username: " + username);
            
            User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;
            System.out.println("QR Code Request - User found: " + (user != null));
            
            if (user == null || user.getTwoFactorSecret() == null) {
                System.out.println("QR Code: User not found or no secret. User: " + user + ", Secret: " + (user != null ? user.getTwoFactorSecret() : "null"));
                return ResponseEntity.notFound().build();
            }
            
            String otpauth = "otpauth://totp/VIMS:" + username + "?secret=" + user.getTwoFactorSecret() + "&issuer=VIMS&algorithm=SHA1&digits=6&period=30";
            System.out.println("QR Code: Generating for " + otpauth);
            byte[] qrCode = qrCodeService.generateQrCode(otpauth);
            System.out.println("QR Code: Generated " + qrCode.length + " bytes");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.setContentLength(qrCode.length);
            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("QR Code Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/2fa/test-qr")
    @ResponseBody
    public ResponseEntity<byte[]> testQrCode() {
        try {
            String testUrl = "otpauth://totp/test?secret=TEST123&issuer=VIMS";
            System.out.println("Test QR Code: Generating for " + testUrl);
            byte[] qrCode = qrCodeService.generateQrCode(testUrl);
            System.out.println("Test QR Code: Generated " + qrCode.length + " bytes");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            headers.setContentLength(qrCode.length);
            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Test QR Code Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


