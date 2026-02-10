package com.springsecurityjwt3.springSecurityJwt3.config;

import com.springsecurityjwt3.springSecurityJwt3.service.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.swing.plaf.SeparatorUI;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private JwtFilter jwtFilter;

    //@Value("${app.security.pepper}")
    private final String PEPPERSECRETKEY = "4AnRtF;gZQ9wNDxC";

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(){
            @Override
            public String encode(CharSequence rawPassword){
                return super.encode(rawPassword + PEPPERSECRETKEY);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword){
                return super.matches(rawPassword + PEPPERSECRETKEY, encodedPassword);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth
                                // Publicly accessible
                                .requestMatchers("/auth/login", "/auth/register").permitAll()
                                // Requires a token
                                .requestMatchers("/auth/logout").authenticated()
                                .anyRequest().authenticated()
                        );
        httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws  Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userEntityService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}
