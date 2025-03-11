package ipleiria.risk_matrix.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Correct way to disable CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**", "/questions/**", "/suggestions/**", "/answers/**", "/questionnaires/**").permitAll() // Allow these endpoints
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // ✅ Correct way to enable Basic Authentication

        return http.build();
    }
}
