package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // Criar um novo administrador
    @PostMapping("/create")
    public AdminUser createAdmin(@RequestBody AdminUser adminUser) {
        return adminUserService.createAdmin(adminUser);
    }

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
