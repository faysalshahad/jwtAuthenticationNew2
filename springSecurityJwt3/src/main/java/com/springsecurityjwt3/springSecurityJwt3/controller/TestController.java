package com.springsecurityjwt3.springSecurityJwt3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class TestController {


    @GetMapping(value = "/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER ADMIN')")
    public ResponseEntity<?> getNew()
    {
        return ResponseEntity.ok("Success. Public Content: Visible to any logged-in user.");
    }

    @GetMapping(value = "/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER ADMIN')")
    public ResponseEntity<?> getAdmin()
    {
        return ResponseEntity.ok("Admin Board: Only Admins and Super Admins can see this.");
    }

    @GetMapping(value = "/super-admin")
    @PreAuthorize("hasAnyRole('SUPER ADMIN')")
    public ResponseEntity<?> getSuperAdmin()
    {
        return ResponseEntity.ok("Super Admin Secret: Higher level access granted.");
    }

    @GetMapping(value = "/user")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> getUser()
    {
        return ResponseEntity.ok("User only page.");
    }

    @GetMapping(value = "/userInfo")
    public ResponseEntity<?> getUserInfo()
    {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("You are logged in as: " + auth.getName() + " with roles: " + auth.getAuthorities());
    }


}
