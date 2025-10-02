package com.vims.security.security;

import com.vims.security.domain.User;
import com.vims.security.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class TwoFactorFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    private static final Set<String> EXCLUDE_PATHS = Set.of(
            "/login", "/logout", "/2fa", "/register", "/h2-console"
    );

    public TwoFactorFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (EXCLUDE_PATHS.stream().anyMatch(path::startsWith) || path.startsWith("/css") || path.startsWith("/js")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.isTwoFactorEnabled()) {
                Object verified = request.getSession().getAttribute("2faVerified");
                if (!(verified instanceof Boolean) || !((Boolean) verified)) {
                    response.sendRedirect("/2fa");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}


