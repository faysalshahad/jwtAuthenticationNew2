package com.springsecurityjwt3.springSecurityJwt3.config;

import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;
import com.springsecurityjwt3.springSecurityJwt3.service.UserEntityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/auth/login") || path.equals("/auth/register");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        //Extracting Token from Header
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            jwt = authHeader.substring(7);
            try{
                username = jwtUtil.getuserNameFromToken(jwt);
            } catch (Exception e) {
                System.out.println("Unable to get JWT Token or JWT Token has expired");
            }
        }

        //Validating Token and Setting Security Context
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.userEntityService.loadUserByUsername(username);

            // Fetch the entity to check the logout timestamp
            UserEntity userEntity = userEntityRepository.findByUsername(username).orElse(null);

            if (userEntity != null && jwtUtil.validateToken(jwt)) {

                Date issuedAt = jwtUtil.getIssuedAtDateFromToken(jwt);
                LocalDateTime issuedAtLDT = issuedAt.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                // 3. Revocation Check
                if (userEntity.getLastLogoutDate() != null &&
                        issuedAtLDT.isBefore(userEntity.getLastLogoutDate())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked.");
                    return; // Stop here if revoked
                }

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
            filterChain.doFilter(request,response);
    }

}
