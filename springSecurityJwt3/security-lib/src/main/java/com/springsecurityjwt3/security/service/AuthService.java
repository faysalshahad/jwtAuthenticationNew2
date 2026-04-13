package com.springsecurityjwt3.security.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.springsecurityjwt3.security.config.JwtUtil;
import com.springsecurityjwt3.core.dto.AuthResponse;
import com.springsecurityjwt3.core.dto.LoginRequest;
import com.springsecurityjwt3.core.dto.RegisterRequest;
import com.springsecurityjwt3.core.entity.UserEntity;
import com.springsecurityjwt3.core.repository.UserEntityRepository;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.security.lockout-duration-minutes}")
    private int lockoutDuration;

    @Value("${app.security.expiration-time-refresh-token}")
    private int refreshTokenDuration;

    public void registerUser(RegisterRequest registerRequest){
        if (userEntityRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Error: Username '" + registerRequest.getUsername() + "' already exists.");
        }

        UserEntity newUserEntity = new UserEntity();
        newUserEntity.setUsername(registerRequest.getUsername());
        newUserEntity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Logical check: If no role provided, default to USER. 
        // Or Just Save the role as provided by the client, but ensure it's in uppercase and has no spaces.
        String role = (registerRequest.getRole() == null || registerRequest.getRole().isBlank()) 
                      ? "USER" : registerRequest.getRole().toUpperCase().replace(" ", "_");

        newUserEntity.setRole(role);
        userEntityRepository.save(newUserEntity);

    }

//     public AuthResponse loginUser(LoginRequest loginRequest){
//         // Authentication logic is handled by Spring Security's filter chain, so we just need to generate the JWT if authentication is successful.
//         UserEntity userEntity = userEntityRepository.findByUsername(loginRequest.getUsername())
//                 .orElseThrow(() -> new RuntimeException("User Not Found: " + loginRequest.getUsername()));

//         // Check if account is locked before proceeding with authentication        
//         handleLockoutStatus(userEntity);

//         try {
//             authenticationManager.authenticate(
//                     new UsernamePasswordAuthenticationToken(
//                             loginRequest.getUsername(),
//                             loginRequest.getPassword()
//                     )
//             );

//             // Success: Reset failed attempts
//             userEntity.setFailedAttempt(0);
//             userEntity.setLockTime(null);

//             // Generate JWT Token
//             String accessToken = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole());
//             // Generate Refresh Token
//             String refreshToken = jwtUtil.generateRefreshToken();

//             userEntity.setRefreshToken(refreshToken);
//             userEntity.setRefreshTokenExpiry(LocalDateTime.now().plusDays(refreshTokenDuration));
//             userEntityRepository.save(userEntity);

            
//         return new AuthResponse("Login successful", accessToken, refreshToken);
        
//         } catch (AuthenticationException e) {
//             userEntityService.increaseFailedAttempts(userEntity);
//             throw new RuntimeException("Invalid credentials. Attempt " + userEntity.getFailedAttempt() + " of 5.");
//         }
        
//     }

//     public AuthResponse refreshAccessToken(String refreshTokenRequest) {
//     UserEntity user = userEntityRepository.findByRefreshToken(refreshTokenRequest)
//             .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));

//     if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
//         throw new RuntimeException("Refresh Token expired. Please login again.");
//     }

//     // Generate NEW Access Token
//     String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
    
//     return new AuthResponse("Token refreshed", newAccessToken, user.getRefreshToken());
// }

public AuthResponse loginUser(LoginRequest loginRequest) {
        UserEntity userEntity = userEntityRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User Not Found: " + loginRequest.getUsername()));

        handleLockoutStatus(userEntity);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            userEntity.setFailedAttempt(0);
            userEntity.setLockTime(null);

            String accessToken = jwtUtil.generateToken(userEntity.getUsername(), userEntity.getRole());
            String refreshToken = jwtUtil.generateRefreshToken();

            userEntity.setRefreshToken(refreshToken);
            userEntity.setRefreshTokenExpiry(LocalDateTime.now().plusDays(refreshTokenDuration));
            userEntityRepository.save(userEntity);

            // Return everything to the controller; the controller decides what to hide in cookies
            return new AuthResponse("Login successful", userEntity.getRole(), accessToken, refreshToken);
        
        } catch (AuthenticationException e) {
            userEntityService.increaseFailedAttempts(userEntity);
            throw new RuntimeException("Invalid credentials.");
        }
    }

    public AuthResponse refreshAccessToken(String refreshTokenRequest) {
        UserEntity user = userEntityRepository.findByRefreshToken(refreshTokenRequest)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));

        if (user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh Token expired. Please login again.");
        }

        String newAccessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
        // Return new access token and keep same refresh token
        return new AuthResponse("Token refreshed", user.getRole(), newAccessToken, user.getRefreshToken());
    }

    public void handleLockoutStatus(UserEntity userEntity) {
        if (!userEntity.isAccountNonLocked()) {
            if (userEntity.getLockTime() != null && Duration.between(userEntity.getLockTime(), LocalDateTime.now()).toMinutes() >= lockoutDuration) {
                
                // One minute has passed, unlock the account
                userEntity.setAccountNonLocked(true);
                userEntity.setFailedAttempt(0);
                userEntity.setLockTime(null);
                userEntityRepository.save(userEntity);
            } else {
                throw new RuntimeException("Your account is locked. Please try again in " + (lockoutDuration - Duration.between(userEntity.getLockTime(), LocalDateTime.now()).toMinutes()) + " minutes.");
            }
        }
    }   
}