package ipleiria.risk_matrix;

import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class RiskMatrixApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskMatrixApplication.class, args);
    }

    @Bean
    public CommandLineRunner createDefaultAdmin(AdminUserRepository adminRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            String defaultUsername = "admin";
            String defaultEmail = "admin@example.com";
            String defaultPassword = "admin123";

            if (adminRepo.findByUsername(defaultUsername).isEmpty()) {
                AdminUser admin = new AdminUser();
                admin.setUsername(defaultUsername);
                admin.setEmail(defaultEmail);
                admin.setPassword(passwordEncoder.encode(defaultPassword)); // encrypt password
                adminRepo.save(admin);

                System.out.println("Default admin created: " + defaultUsername + " / " + defaultPassword);
            } else {
                System.out.println("Default admin already exists.");
            }
        };
    }

}
