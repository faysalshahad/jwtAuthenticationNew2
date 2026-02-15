package com.springsecurityjwt3.springSecurityJwt3.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
