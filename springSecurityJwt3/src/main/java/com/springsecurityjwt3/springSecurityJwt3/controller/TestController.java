package com.springsecurityjwt3.springSecurityJwt3.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class TestController {


    @GetMapping(value = "/new")
    public ResponseEntity<?> getNew()
    {
        return ResponseEntity.ok("Success");
    }
}
