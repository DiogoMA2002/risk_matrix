package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.EmailAlreadyExistsException;
import ipleiria.risk_matrix.exceptions.exception.UsernameAlreadyExistsException;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    // Criar um novo administrador
    public AdminUser createAdmin(AdminUser adminUser) {
        if (adminUserRepository.existsByUsername(adminUser.getUsername())) {
            throw new UsernameAlreadyExistsException("O nome de usuário já existe!");
        }
        if (adminUserRepository.existsByEmail(adminUser.getEmail())) {
            throw new EmailAlreadyExistsException("O email já existe!");
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
