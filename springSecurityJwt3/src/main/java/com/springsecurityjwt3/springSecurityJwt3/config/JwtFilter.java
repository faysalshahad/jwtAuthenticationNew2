package com.springsecurityjwt3.springSecurityJwt3.config;

import com.springsecurityjwt3.springSecurityJwt3.entity.UserEntity;
import com.springsecurityjwt3.springSecurityJwt3.repository.UserEntityRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

//    @Autowired
//    private UserEntityService userEntityService;

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private ServerMetadata serverMetadata;

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
            //UserDetails userDetails = this.userEntityService.loadUserByUsername(username);

            // Fetch the entity to check the logout timestamp
            UserEntity userEntity = userEntityRepository.findByUsername(username).orElse(null);

            if (userEntity != null && jwtUtil.validateToken(jwt)) {

                Date issuedAt = jwtUtil.getIssuedAtDateFromToken(jwt);
                LocalDateTime issuedAtLDT = issuedAt.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                System.out.println("Token Issued At: " + issuedAtLDT);
                System.out.println("Server Start At: " + serverMetadata.getServerStartTime());

                // --- SERVER RESTART CHECK ---
                // If the token was issued before the current server instance started, reject it.
                if(issuedAtLDT.isBefore(serverMetadata.getServerStartTime())){
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired due to server restart. Please login again.");
                    return;
                }

                // Revocation Check
                if (userEntity.getLastLogoutDate() != null &&
                        issuedAtLDT.isBefore(userEntity.getLastLogoutDate())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been revoked.");
                    return; // Stop here if revoked
                }

//                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());

                // 3️⃣ Extract roles FROM JWT
                var authorities = jwtUtil.getAuthoritiesFromToken(jwt);

                // 4️⃣ Create Authentication
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                authorities
                        );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
            filterChain.doFilter(request,response);
    }

}
