package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AdminRegisterDTO;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.models.users.PasswordHistory;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.repository.PasswordHistoryRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AdminUserService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminUserRepository adminUserRepository,
                            PasswordHistoryRepository passwordHistoryRepository,
                            PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordHistoryRepository = passwordHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AdminUser> getAdminByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    public List<AdminUser> getAllAdmins() {
        return adminUserRepository.findAll();
    }

    @Transactional
    public void register(AdminRegisterDTO dto) {
        if (adminUserRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ConflictException("Username já existe");
        }
        if (adminUserRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("Email já existe");
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(dto.getUsername());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        adminUserRepository.save(admin);

        PasswordHistory initial = new PasswordHistory();
        initial.setAdmin(admin);
        initial.setPasswordHash(admin.getPassword());
        initial.setChangedAt(LocalDateTime.now());
        passwordHistoryRepository.save(initial);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequestDTO request) {
        AdminUser admin = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado"));

        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("Password antiga é incorreta");
        }

        List<PasswordHistory> history = passwordHistoryRepository.findTop5ByAdminOrderByChangedAtDesc(admin);
        for (PasswordHistory past : history) {
            if (passwordEncoder.matches(request.getNewPassword(), past.getPasswordHash())) {
                throw new IllegalArgumentException(
                        "A nova password foi recentemente utilizada, escolha outra nova password");
            }
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(admin);

        PasswordHistory entry = new PasswordHistory();
        entry.setAdmin(admin);
        entry.setPasswordHash(admin.getPassword());
        entry.setChangedAt(LocalDateTime.now());
        passwordHistoryRepository.save(entry);

        // Keep only the last 5 history entries
        List<PasswordHistory> allEntries = passwordHistoryRepository.findByAdminOrderByChangedAtDesc(admin);
        if (allEntries.size() > 5) {
            passwordHistoryRepository.deleteAll(allEntries.subList(5, allEntries.size()));
        }
    }
}
