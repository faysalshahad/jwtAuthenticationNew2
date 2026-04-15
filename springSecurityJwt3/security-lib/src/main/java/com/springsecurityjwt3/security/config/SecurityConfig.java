package com.springsecurityjwt3.security.config;

import java.util.List;

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

import com.springsecurityjwt3.security.filter.JwtFilter;
import com.springsecurityjwt3.security.service.UserEntityService;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private JwtFilter jwtFilter;

    // @Value("${app.security.pepper}")
    // private final String PEPPERSECRETKEY = "4AnRtF;gZQ9wNDxC";

    @Value("${app.security.pepper}")
    private String PEPPERSECRETKEY;

    // Define Swagger/OpenAPI endpoints that should be public
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/swagger-resources/**",
            "/swagger-resources",
            "/webjars/**",
            "/configuration/ui",
        "/configuration/security"
    };

    // Modern Approach

    // private final UserEntityService userEntityService;
    // private final JwtFilter jwtFilter;
    // private final String pepperSecretKey;
    //
    // @Autowired
    // public SecurityConfig(
    // UserEntityService userEntityService,
    // JwtFilter jwtFilter,
    // @Value("${app.security.pepper}") String pepperSecretKey
    // ) {
    // this.userEntityService = userEntityService;
    // this.jwtFilter = jwtFilter;
    // this.pepperSecretKey = pepperSecretKey;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return super.encode(rawPassword + PEPPERSECRETKEY);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return super.matches(rawPassword + PEPPERSECRETKEY, encodedPassword);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                // .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible Swagger UI endpoints
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()
                        // Publicly accessible Login Endpoint
                        .requestMatchers("/auth/login", "/auth/refresh").permitAll()
                        // // Publicly accessible Login Endpoint
                        // .requestMatchers("/auth/refresh").permitAll()
                        // Requires a token
                        // .requestMatchers("/auth/register", "/auth/**",
                        // "/auth/api/**").authenticated()
                        // Requires a token
                        .requestMatchers("/auth/logout").authenticated() // Ensure this is authenticated
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                // --- LOGOUT SECTION ---
                .logout(logout -> logout
                        .logoutUrl("/auth/logout") // The endpoint to trigger logout
                        .addLogoutHandler((request, response, authentication) -> {
                            // Dynamically detect the domain (localhost or IP)
                            String domain = request.getServerName();
                            // Forcefully overwrite cookies with immediate expiration
                            for (String cookieName : new String[] { "accessToken", "refreshToken" }) {
                                jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, null);
                                cookie.setPath("/"); // CRITICAL: This must match the path used during login
                                cookie.setDomain(domain); // Using the detected domain
                                cookie.setHttpOnly(true);
                                cookie.setMaxAge(0); // Expires immediately
                                response.addCookie(cookie);
                            }
                        })
                        // .deleteCookies("accessToken", "refreshToken")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\": \"Logged out successfully.\"}");
                            response.getWriter().flush();
                        }));

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        // DaoAuthenticationProvider authenticationProvider = new
        // DaoAuthenticationProvider()
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userEntityService);
        // authenticationProvider.setUserDetailsService(userEntityService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Combine both origins in a single list
        configuration.setAllowedOrigins(List.of(
                "http://localhost:8000", // Vite React app
                // Refereing to PC's local IP address for access from other devices in the same
                // network
                "http://172.20.1.225:8000", // Refereing to PC's local IP address for access from other devices in the
                                            // same network
                "http://192.168.144.1:8000", // Refereing to PC's local IP address for access from other devices in the
                                             // same network
                "http://172.29.128.1:8000", // Refereing to PC's local IP address for access from other devices in the
                                            // same network
                // Swagger UI origins
                "http://localhost:8080", // Swagger UI origin
                "http://172.20.1.225:8080", // Swagger UI origin
                "http://192.168.144.1:8080", // Swagger UI origin
                "http://172.29.128.1:8080" // Swagger UI origin

        ));
        // Allow all common HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Allow the headers React sends (JWT and JSON)
        // configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // Allow all headers that Swagger UI and your API might need
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true); // MANDATORY for cookies
        // Add "Set-Cookie" to your allowed headers just in case
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));
        // How long the browser should cache preflight response (in seconds)
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
