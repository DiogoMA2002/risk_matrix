package ipleiria.risk_matrix.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtFilter jwtFilter;
    private final PublicJwtFilter publicJwtFilter;
    private final String allowedOrigins;

    public SecurityConfig(
            JwtFilter jwtFilter,
            PublicJwtFilter publicJwtFilter,
            @Value("${app.cors.allowed-origins:http://localhost:8081,http://127.0.0.1:8081}") String allowedOrigins
    ) {
        this.jwtFilter = jwtFilter;
        this.publicJwtFilter = publicJwtFilter;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:;"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .preload(true))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                // Autorização por endpoint
                .authorizeHttpRequests(auth -> auth
                        // Static frontend assets (Vue build output)
                        .requestMatchers("/", "/index.html", "/favicon.ico",
                                "/js/**", "/css/**", "/img/**", "/assets/**").permitAll()

                        // Swagger / OpenAPI
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()

                        .requestMatchers("/api/auth/login", "/api/auth/logout",
                                "/api/auth/refresh", "/api/auth/request-token").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/change-password").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/glossary/**").hasAnyRole("ADMIN", "PUBLIC")
                        .requestMatchers(HttpMethod.POST, "/api/feedback/**").hasAnyRole("ADMIN", "PUBLIC")
                        .requestMatchers(HttpMethod.GET, "/api/feedback/**").hasRole("ADMIN")

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/glossary/**").hasRole("ADMIN")

                        .requestMatchers("/api/questions/**",
                                "/api/answers/**",
                                "/api/questionnaires/**",
                                "/api/categories/**"
                        ).hasAnyRole("ADMIN", "PUBLIC")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Admin filter runs first so it can claim the SecurityContext;
        // public filter runs after and skips if authentication is already set.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(publicJwtFilter, JwtFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> originPatterns = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::normalizeOriginPattern)
                .toList();

        logger.info("Effective CORS allowed origins: {}", originPatterns.isEmpty() ? "[default]" : originPatterns);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOriginPatterns(originPatterns.isEmpty()
                ? List.of("http://localhost:8081", "http://127.0.0.1:8081")
                : originPatterns);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * Spring's CORS origin-pattern syntax expects wildcard ports as :[*].
     * Accept legacy env values like http://localhost:* and normalize them.
     */
    private String normalizeOriginPattern(String pattern) {
        if (pattern.endsWith(":*")) {
            return pattern.substring(0, pattern.length() - 2) + ":[*]";
        }
        return pattern;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}