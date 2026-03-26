package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.config.RateLimitConfig;
import ipleiria.risk_matrix.config.RateLimitInterceptor;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.TokenBlocklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

/**
 * Shared mock bean definitions for @WebMvcTest controller tests.
 * Provides the security and infrastructure beans that the web slice
 * (filters, interceptors, security config) requires but that are
 * not auto-detected by @WebMvcTest.
 */
@TestConfiguration
public class WebMvcTestConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return mock(JwtUtil.class);
    }

    @Bean
    public AdminUserDetailsService adminUserDetailsService() {
        return mock(AdminUserDetailsService.class);
    }

    @Bean
    public TokenBlocklistService tokenBlocklistService() {
        return mock(TokenBlocklistService.class);
    }

    @Bean
    public RateLimitConfig rateLimitConfig() {
        return mock(RateLimitConfig.class);
    }

    @Bean
    @Primary
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor(rateLimitConfig(), jwtUtil(), "127.0.0.1") {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                return true;
            }
        };
    }

    @Bean
    public AdminUserRepository adminUserRepository() {
        return mock(AdminUserRepository.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return mock(PasswordEncoder.class);
    }
}
