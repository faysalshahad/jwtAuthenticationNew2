package com.springsecurityjwt3.springSecurityJwt3.controller;

import com.springsecurityjwt3.dto.AuthResponse;
import com.springsecurityjwt3.dto.LoginRequest;
import com.springsecurityjwt3.dto.RegisterRequest;
import com.springsecurityjwt3.dto.TokenRefreshRequest;
import com.springsecurityjwt3.dto.UserSummaryDTO;
import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;
import com.springsecurityjwt3.springSecurityJwt3.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserEntityRepository userEntityRepository;


    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')") // Only Super Admin and Admin can hit this endpoint
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.ok(new AuthResponse("User registered successfully", null, null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // String token = authService.loginUser(request);
            AuthResponse authResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new AuthResponse(e.getMessage(), null, null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(new AuthResponse("No active session found", null, null));
        }

        UserEntity user = userEntityRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Revoke tokens on logout
        user.setLastLogoutDate(LocalDateTime.now());
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userEntityRepository.save(user);
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(new AuthResponse("Successfully logged out", null, null));
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        
        try {
            AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new AuthResponse(e.getMessage(), null, null));
        }
    }

    // Inside AuthController.java

@GetMapping("/users")
// @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'USER')") // Everyone logged in should see this to place orders
public ResponseEntity<List<UserSummaryDTO>> getAllUsers() {
    List<UserSummaryDTO> users = userEntityRepository.findAll().stream()
            .map(user -> new UserSummaryDTO(user.getId(), user.getUsername()))
            .toList();
    return ResponseEntity.ok(users);
}
}