package ipleiria.risk_matrix;

import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class RiskMatrixApplication {

    private static final Logger logger = LoggerFactory.getLogger(RiskMatrixApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RiskMatrixApplication.class, args);
    }

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner createDefaultAdmin(AdminUserRepository adminRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (adminRepo.findByUsername(adminUsername).isEmpty()) {
                AdminUser admin = new AdminUser();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                adminRepo.save(admin);

                logger.warn("Default admin account created (username: {}). Change this password immediately!", adminUsername);
            } else {
                logger.debug("Default admin account already exists.");
            }
        };
    }

}
