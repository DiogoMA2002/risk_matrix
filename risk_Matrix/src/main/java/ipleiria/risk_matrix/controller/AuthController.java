package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.dto.AdminRegisterDTO;
import ipleiria.risk_matrix.dto.AuthRequestDTO;
import ipleiria.risk_matrix.dto.AuthResponseDTO;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import ipleiria.risk_matrix.dto.UpdateEmailRequestDTO;
import ipleiria.risk_matrix.dto.UpdateUsernameRequestDTO;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.models.users.PasswordHistory;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.repository.PasswordHistoryRepository;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService userDetailsService;
    private final AdminUserRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepo;
    private final AdminUserService adminUserService;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, AdminUserDetailsService userDetailsService,
                          AdminUserRepository adminRepo, PasswordEncoder passwordEncoder, PasswordHistoryRepository passwordHistoryRepo,
                          AdminUserService adminUserService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.passwordHistoryRepo = passwordHistoryRepo;
        this.adminUserService = adminUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponseDTO(jwt));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais Inválidas");
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@Valid @RequestBody AdminRegisterDTO dto) {
        if (adminRepo.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username já existe");
        }

        if (adminRepo.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já existe");
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(dto.getUsername());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));

        adminRepo.save(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body("Admin criado com sucesso");
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {

        String username = authentication.getName();
        AdminUser admin = adminRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado"));

        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password antiga é incorreta");
        }

        // Check if new password was used before
        List<PasswordHistory> history = passwordHistoryRepo.findTop3ByAdminOrderByChangedAtDesc(admin);
        for (PasswordHistory past : history) {
            if (passwordEncoder.matches(request.getNewPassword(), past.getPasswordHash())) {
                return ResponseEntity.badRequest().body("A nova password foi recentemente utilizada, escolha outra nova password");
            }
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepo.save(admin);

        // Save to history
        PasswordHistory entry = new PasswordHistory();
        entry.setAdmin(admin);
        entry.setPasswordHash(admin.getPassword());
        entry.setChangedAt(LocalDateTime.now());
        passwordHistoryRepo.save(entry);

        // ⚠️ Clean up history: keep only last 5
        List<PasswordHistory> allEntries = passwordHistoryRepo.findByAdminOrderByChangedAtDesc(admin);
        if (allEntries.size() > 5) {
            List<PasswordHistory> toDelete = allEntries.subList(5, allEntries.size());
            passwordHistoryRepo.deleteAll(toDelete);
        }

        return ResponseEntity.ok("Password alterada com sucesso!");
    }

    @PutMapping("/update-username")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUsername(
            @Valid @RequestBody UpdateUsernameRequestDTO request,
            @AuthenticationPrincipal UserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado.");
        }
        String currentUsername = principal.getUsername();

        try {
            // Call the service method
            AdminUser updatedUser = adminUserService.updateOwnUsername(
                    currentUsername,
                    request.getNewUsername(),
                    request.getCurrentPassword()
            );

            // Generate new UserDetails and JWT with the updated username
            UserDetails newUserDetails = userDetailsService.loadUserByUsername(updatedUser.getUsername());
            String newJwt = jwtUtil.generateToken(newUserDetails);

            // Return the new JWT
            return ResponseEntity.ok(new AuthResponseDTO(newJwt));

        } catch (Exception e) {
            // Catch potential exceptions from the service (NotFound, BadRequest, Conflict)
            // You might want more specific exception handling here
            if (e instanceof NotFoundException || e instanceof BadRequestException || e instanceof ConflictException) {
                 return ResponseEntity.badRequest().body(e.getMessage());
            } else {
                // Log unexpected errors
                // logger.error("Unexpected error updating username for {}: {}", currentUsername, e.getMessage(), e);
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar nome de utilizador.");
            }
        }
    }

    @PutMapping("/update-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateEmail(
            @Valid @RequestBody UpdateEmailRequestDTO request,
            @AuthenticationPrincipal UserDetails principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Não autenticado.");
        }
        String currentUsername = principal.getUsername();

        try {
            // Call the service method
            adminUserService.updateOwnEmail(
                    currentUsername,
                    request.getNewEmail(),
                    request.getCurrentPassword()
            );

            // Email change doesn't invalidate JWT subject, just return success
            return ResponseEntity.ok("Email atualizado com sucesso!");

        } catch (Exception e) {
             if (e instanceof NotFoundException || e instanceof BadRequestException || e instanceof ConflictException) {
                 return ResponseEntity.badRequest().body(e.getMessage());
            } else {
                // logger.error("Unexpected error updating email for {}: {}", currentUsername, e.getMessage(), e);
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar email.");
            }
        }
    }

}
