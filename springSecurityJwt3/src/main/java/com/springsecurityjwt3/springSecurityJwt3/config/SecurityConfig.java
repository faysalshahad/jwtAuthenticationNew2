package com.springsecurityjwt3.springSecurityJwt3.config;

import com.springsecurityjwt3.springSecurityJwt3.service.UserEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private JwtFilter jwtFilter;

    //@Value("${app.security.pepper}")
//    private final String PEPPERSECRETKEY = "4AnRtF;gZQ9wNDxC";

    @Value("${app.security.pepper}")
    private String PEPPERSECRETKEY;

    // Modern Approach

//    private final UserEntityService userEntityService;
//    private final JwtFilter jwtFilter;
//    private final String pepperSecretKey;
//
//    @Autowired
//    public SecurityConfig(
//            UserEntityService userEntityService,
//            JwtFilter jwtFilter,
//            @Value("${app.security.pepper}") String pepperSecretKey
//    ) {
//        this.userEntityService = userEntityService;
//        this.jwtFilter = jwtFilter;
//        this.pepperSecretKey = pepperSecretKey;
//    }

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
//                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                // Publicly accessible
                                .requestMatchers("/auth/login", "/auth/register").permitAll()
                                // Requires a token
                                .requestMatchers("/auth/logout").authenticated()
                                .anyRequest().authenticated()
                        )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws  Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        //DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider()
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userEntityService);
        //authenticationProvider.setUserDetailsService(userEntityService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        // How long the browser should cache preflight response (in seconds)
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
