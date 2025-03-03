package ipleiria.risk_matrix.service;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    // Criar um novo administrador
    public AdminUser createAdmin(AdminUser adminUser) {
        if (adminUserRepository.existsByUsername(adminUser.getUsername())) {
            throw new RuntimeException("Username já existe!");
        }
        if (adminUserRepository.existsByEmail(adminUser.getEmail())) {
            throw new RuntimeException("Email já existe!");
        }
        return adminUserRepository.save(adminUser);
    }

    // Buscar admin pelo username
    public Optional<AdminUser> getAdminByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    // Listar todos os admins
    public List<AdminUser> getAllAdmins() {
        return adminUserRepository.findAll();
    }
}
