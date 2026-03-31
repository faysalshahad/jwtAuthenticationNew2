package com.springsecurityjwt3.springSecurityJwt3.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springsecurityjwt3.springSecurityJwt3.dto.AuthResponse;
import com.springsecurityjwt3.springSecurityJwt3.dto.LoginRequest;
import com.springsecurityjwt3.springSecurityJwt3.dto.RegisterRequest;
import com.springsecurityjwt3.springSecurityJwt3.dto.UserSummaryDTO;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;
import com.springsecurityjwt3.springSecurityJwt3.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;

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
        // try {
        //     authService.registerUser(request);
        //     return ResponseEntity.ok(new AuthResponse("User registered successfully", null));
        // } catch (Exception e) {
        //     return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage(), null));
        // }
        try {
        // Perform the registration logic
        authService.registerUser(request);
        
        // Return a clean response. 
        // We pass 'null' for the role here because the Admin doesn't need 
        // the NEW user's role to change their own UI.
        return ResponseEntity.ok(new AuthResponse("User " + request.getUsername() + " registered successfully", null));
        
    } catch (RuntimeException e) {
        // Return a 400 Bad Request if the user already exists or validation fails
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new AuthResponse(e.getMessage(), null));
    } catch (Exception e) {
        // Generic fallback for server errors
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new AuthResponse("An unexpected error occurred", null));
    }
    }

    // @PostMapping("/login")
    // public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
    //     try {
    //         // String token = authService.loginUser(request);
    //         AuthResponse authResponse = authService.loginUser(loginRequest);
    //         return ResponseEntity.ok(authResponse);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //                              .body(new AuthResponse(e.getMessage(), null, null));
    //     }
    // }

    @PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    AuthResponse authResponse = authService.loginUser(request);

    // Create the Access Token Cookie
    ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", authResponse.getAccessToken())
            .httpOnly(true)
            .secure(false) // Set to 'true' in production with HTTPS
            .path("/")
            .maxAge(1 * 60) // 1 minutes
            .sameSite("Strict")
            .build();

    // Create the Refresh Token Cookie
    ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
            .httpOnly(true)
            .secure(false) // Set to 'true' in production 
            .path("/")
            .maxAge(1 * 24 * 60 * 60) // 1 days
            .sameSite("Strict")
            .build();

     // Return only message and role in the body
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new AuthResponse(authResponse.getMessage(), authResponse.getRole()));
    }


    // @PostMapping("/logout")
    // public ResponseEntity<AuthResponse> logout() {
    //     var auth = SecurityContextHolder.getContext().getAuthentication();
        
    //     if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
    //                              .body(new AuthResponse("No active session found", null, null));
    //     }

    //     UserEntity user = userEntityRepository.findByUsername(auth.getName())
    //             .orElseThrow(() -> new RuntimeException("User not found"));

    //     // Revoke tokens on logout
    //     user.setLastLogoutDate(LocalDateTime.now());
    //     user.setRefreshToken(null);
    //     user.setRefreshTokenExpiry(null);
    //     userEntityRepository.save(user);
    //     SecurityContextHolder.clearContext();

    //     return ResponseEntity.ok(new AuthResponse("Successfully logged out", null, null));
    // }


    @PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest request) {
    // 1. Create a "Clear" cookie for Access Token
    ResponseCookie clearAccessToken = ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(false) // Set to true in production (HTTPS)
            .path("/")
            .maxAge(0)    // Tells browser to delete it immediately
            .sameSite("Strict") // CSRF protection
            .build();

    // 2. Create a "Clear" cookie for Refresh Token
    ResponseCookie clearRefreshToken = ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)    // Tells browser to delete it immediately
            .sameSite("Strict") // CSRF protection
            .build();

    // Optional: Update the database to log the logout time (for revocation check)
    // String username = jwtUtil.getuserNameFromToken(extractTokenFromCookie(request));
    // authService.processLogout(username);

    return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, clearAccessToken.toString())
            .header(HttpHeaders.SET_COOKIE, clearRefreshToken.toString())
            .body(Map.of("message", "Logged out successfully"));
}

    // @PostMapping("/refresh")
    // public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        
    //     try {
    //         AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
    //         return ResponseEntity.ok(response);
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    //         .body(new AuthResponse(e.getMessage(), null, null));
    //     }
    // }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        try {
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : request.getCookies()) {
                    if ("refreshToken".equals(c.getName())) refreshToken = c.getValue();
                }
            }

            if (refreshToken == null) throw new RuntimeException("No refresh token");

            AuthResponse response = authService.refreshAccessToken(refreshToken);

            ResponseCookie newAccessCookie = ResponseCookie.from("accessToken", response.getAccessToken())
                    .httpOnly(true)
                    .path("/")
                    .maxAge(15 * 60)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newAccessCookie.toString())
                    .body(new AuthResponse("Token refreshed", response.getRole()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(e.getMessage(), null));
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