package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.dto.AdminRegisterDTO;
import ipleiria.risk_matrix.dto.AuthRequestDTO;
import ipleiria.risk_matrix.dto.AuthResponseDTO;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import ipleiria.risk_matrix.dto.EmailTokenRequestDTO;
import ipleiria.risk_matrix.dto.EmailTokenResponseDTO;
import ipleiria.risk_matrix.dto.RefreshTokenRequestDTO;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.models.users.PasswordHistory;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.repository.PasswordHistoryRepository;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService userDetailsService;
    private final AdminUserRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepo;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, AdminUserDetailsService userDetailsService,
                          AdminUserRepository adminRepo, PasswordEncoder passwordEncoder, PasswordHistoryRepository passwordHistoryRepo) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.passwordHistoryRepo = passwordHistoryRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);
            
            return ResponseEntity.ok(new AuthResponseDTO(jwt, refreshToken, jwtUtil.getPublicTokenExpirationMs()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais Inválidas");
        }
    }

    @PostMapping("/request-token")
    public ResponseEntity<?> requestToken(@Valid @RequestBody EmailTokenRequestDTO request) {
        try {
            String token = jwtUtil.generatePublicToken(request.getEmail());
            String refreshToken = jwtUtil.generatePublicRefreshToken(request.getEmail());
            long expiresAt = System.currentTimeMillis() + jwtUtil.getPublicTokenExpirationMs();
            long refreshExpiresAt = System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs();

            EmailTokenResponseDTO response = new EmailTokenResponseDTO(
                    token,
                    refreshToken,
                    request.getEmail(),
                    "public",
                    expiresAt,
                    refreshExpiresAt
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar token: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            // Validate refresh token
            if (!jwtUtil.validateToken(request.getRefreshToken()) || !jwtUtil.isRefreshToken(request.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            String role = jwtUtil.extractRole(request.getRefreshToken());
            
            if ("admin".equals(role)) {
                // Admin refresh
                String username = jwtUtil.extractUsername(request.getRefreshToken());
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                String newToken = jwtUtil.generateToken(userDetails);
                String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
                
                return ResponseEntity.ok(new AuthResponseDTO(newToken, newRefreshToken, jwtUtil.getPublicTokenExpirationMs()));
            } else if ("public".equals(role)) {
                // Public refresh
                String email = jwtUtil.extractEmail(request.getRefreshToken());
                String newToken = jwtUtil.generatePublicToken(email);
                String newRefreshToken = jwtUtil.generatePublicRefreshToken(email);
                long expiresAt = System.currentTimeMillis() + jwtUtil.getPublicTokenExpirationMs();
                long refreshExpiresAt = System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs();
                
                EmailTokenResponseDTO response = new EmailTokenResponseDTO(
                        newToken,
                        newRefreshToken,
                        email,
                        "public",
                        expiresAt,
                        refreshExpiresAt
                );
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token role");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao renovar token: " + e.getMessage());
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
}