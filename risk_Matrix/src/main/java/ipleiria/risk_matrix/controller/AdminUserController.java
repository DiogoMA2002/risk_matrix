package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;


    // Buscar um administrador por username
    @GetMapping("/{username}")
    public Optional<AdminUser> getAdminByUsername(@PathVariable String username) {
        return adminUserService.getAdminByUsername(username);
    }

    // Listar todos os administradores
    @GetMapping("/all")
    public List<AdminUser> getAllAdmins() {
        return adminUserService.getAllAdmins();
    }
}
