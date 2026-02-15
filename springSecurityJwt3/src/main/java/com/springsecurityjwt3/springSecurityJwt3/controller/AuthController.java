package com.springsecurityjwt3.springSecurityJwt3.controller;

import com.springsecurityjwt3.springSecurityJwt3.config.JwtUtil;
import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;
import com.springsecurityjwt3.springSecurityJwt3.service.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestBody UserEntity userEntity){
        if (userEntityRepository.findByUsername(userEntity.getUsername()).isPresent()){
            return "Error! User name has already been taken";
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(userEntity.getUsername());
        newUser.setPassword(passwordEncoder.encode(userEntity.getPassword()));

        newUser.setAccountNonLocked(true);
        newUser.setFailedAttempt(0);
        newUser.setLockTime(null);
        if(userEntity.getRole() == null ){
            newUser.setRole("USER");
        }else {
            newUser.setRole(userEntity.getRole().toUpperCase());
        }
        userEntityRepository.save(newUser);
        return "User Registered Successfully";
    }

    @PostMapping("/login")
    public String loginController(@RequestBody UserEntity userEntity){
        System.out.println(" userEntity : "+userEntity);
        UserEntity user = userEntityRepository.findByUsername(userEntity.getUsername())
                .orElseThrow(()-> new RuntimeException("User not found " + userEntity.getUsername()));

        if (!user.isAccountNonLocked()) {
            if(user.getLockTime() != null &&
            Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes() >= 1){
                user.setAccountNonLocked(true);
                user.setFailedAttempt(0);
                user.setLockTime(null);
                userEntityRepository.save(user);
            } else {
                return "Account is locked. Please try again later!";
            }
        }
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userEntity.getUsername(), userEntity.getPassword())
            );
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userEntityRepository.save(user);
            return jwtUtil.generateToken(user.getUsername());
        } catch (AuthenticationException e) {
           userEntityService.increaseFailedAttempts(user);
            if (user.getFailedAttempt() >= 5) {
                return "Account has been locked due to 5 failed attempts.";
            }
            return "Invalid Password. Attempt " + user.getFailedAttempt() + " of 5";
        }

    }

    @PostMapping("/logout")
    public String logout() {
        // 1. Get Authentication from context
        var auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Check if the user is actually logged in
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "Error: No active session found to logout.";
        }

        // 3. Get username safely
        String username = auth.getName();

        // 4. Update the database
        UserEntity user = userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        user.setLastLogoutDate(LocalDateTime.now());
        userEntityRepository.save(user);

        // 5. Clear the Security Context for this specific request
        SecurityContextHolder.clearContext();

        return "Successfully logged out. Your token is now invalidated.";
    }

}
