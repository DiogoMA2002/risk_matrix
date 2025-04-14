package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.exceptions.exception.BadRequestException;
import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder

    @Autowired // Use constructor injection
    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // Buscar admin pelo username
    public Optional<AdminUser> getAdminByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    // Listar todos os admins
    public List<AdminUser> getAllAdmins() {
        return adminUserRepository.findAll();
    }

    // --- Self-update methods ---

    @Transactional // Ensure atomicity
    public AdminUser updateOwnUsername(String currentUsernamePrincipal, String newUsername, String currentPassword) {
        // 1. Fetch current user
        AdminUser currentUser = adminUserRepository.findByUsername(currentUsernamePrincipal)
                .orElseThrow(() -> new NotFoundException("Utilizador logado não encontrado."));

        // 2. Verify current password
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new BadRequestException("Senha atual incorreta.");
        }

        // 3. Validate new username
        if (newUsername == null || newUsername.trim().isEmpty()) {
             throw new BadRequestException("O novo nome de utilizador não pode ser vazio.");
        }
        String trimmedNewUsername = newUsername.trim();
        if (trimmedNewUsername.equals(currentUsernamePrincipal)) {
            throw new BadRequestException("O novo nome de utilizador não pode ser igual ao atual.");
        }
        if (adminUserRepository.existsByUsername(trimmedNewUsername)) {
            throw new ConflictException("Este nome de utilizador já está em uso.");
        }

        // 4. Update and Save
        currentUser.setUsername(trimmedNewUsername);
        return adminUserRepository.save(currentUser);
    }

    @Transactional
    public AdminUser updateOwnEmail(String currentUsernamePrincipal, String newEmail, String currentPassword) {
        // 1. Fetch current user
        AdminUser currentUser = adminUserRepository.findByUsername(currentUsernamePrincipal)
                .orElseThrow(() -> new NotFoundException("Utilizador logado não encontrado."));

        // 2. Verify current password
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new BadRequestException("Senha atual incorreta.");
        }

        // 3. Validate new email
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new BadRequestException("O novo email não pode ser vazio.");
        }
        // Basic format check (more robust validation can be added)
        if (!newEmail.contains("@")) {
            throw new BadRequestException("Formato de email inválido.");
        }
        String trimmedNewEmail = newEmail.trim();
        if (trimmedNewEmail.equalsIgnoreCase(currentUser.getEmail())) {
            throw new BadRequestException("O novo email não pode ser igual ao atual.");
        }
        if (adminUserRepository.existsByEmail(trimmedNewEmail)) {
            throw new ConflictException("Este email já está em uso.");
        }

        // 4. Update and Save
        currentUser.setEmail(trimmedNewEmail);
        return adminUserRepository.save(currentUser);
    }
}
