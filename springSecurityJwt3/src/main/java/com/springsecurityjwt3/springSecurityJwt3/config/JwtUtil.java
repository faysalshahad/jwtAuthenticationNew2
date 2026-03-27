package com.springsecurityjwt3.springSecurityJwt3.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String jwtSecretKey;

    @Value("${app.security.expiration-time-access-token}")
    private long expirationTime;

    //helper method
    private Key getSigningKey(){
        byte[] keyBytes = jwtSecretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username,  String role) {
        String finalRole = (role.startsWith("ROLE_")) ? role : "ROLE_" + role;
        return Jwts
                .builder()
                .subject(username)
                .claim("roles", finalRole) // Consistency check
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(){
        return java.util.UUID.randomUUID().toString();
    }

    public String getuserNameFromToken(String token){
        return Jwts
                .parser()
                .verifyWith((SecretKey)getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token){
        try{
            Jwts
                    .parser()
                    .verifyWith((SecretKey)getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception){
            System.out.println("Jwt Exception Error");
            return false;
        }
    }

    public Date getIssuedAtDateFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getIssuedAt();
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {

        String role = Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("roles", String.class);

        return List.of(new SimpleGrantedAuthority(role));
    }




}
