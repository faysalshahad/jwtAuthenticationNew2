package com.springsecurityjwt3.security.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class ServerMetadata {

    private LocalDateTime serverStartTime;

    @PostConstruct
    public void init(){
        // Truncated to seconds to avoid millisecond mismatch with JWT precision
        this.serverStartTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        System.out.println("SERVER STARTUP PROTECTOR: System started at " + serverStartTime);
    }

    public LocalDateTime getServerStartTime(){
        return serverStartTime;
    }
}
