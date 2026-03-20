package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.utils.RoleConstants;
import ipleiria.risk_matrix.dto.AdminRegisterDTO;
import ipleiria.risk_matrix.dto.AuthRequestDTO;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import ipleiria.risk_matrix.dto.EmailTokenRequestDTO;
import ipleiria.risk_matrix.dto.RefreshTokenRequestDTO;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.models.users.PasswordHistory;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.repository.PasswordHistoryRepository;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.TokenBlocklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService userDetailsService;
    private final AdminUserRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepo;
    private final TokenBlocklistService tokenBlocklistService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil,
                          AdminUserDetailsService userDetailsService,
                          AdminUserRepository adminRepo, PasswordEncoder passwordEncoder,
                          PasswordHistoryRepository passwordHistoryRepo,
                          TokenBlocklistService tokenBlocklistService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.passwordHistoryRepo = passwordHistoryRepo;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request, HttpServletResponse response) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            setAdminCookies(response, accessToken, refreshToken);

            return ResponseEntity.ok(Map.of(
                    "role", RoleConstants.ADMIN,
                    "expiresIn", jwtUtil.getAdminTokenExpirationMs()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/request-token")
    public ResponseEntity<?> requestToken(@Valid @RequestBody EmailTokenRequestDTO request, HttpServletResponse response) {
        String accessToken = jwtUtil.generatePublicToken(request.getEmail());
        String refreshToken = jwtUtil.generatePublicRefreshToken(request.getEmail());
        long expiresAt = System.currentTimeMillis() + jwtUtil.getPublicTokenExpirationMs();
        long refreshExpiresAt = System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs();

        setPublicCookies(response, accessToken, refreshToken);

        return ResponseEntity.ok(Map.of(
                "role", RoleConstants.PUBLIC,
                "email", request.getEmail(),
                "expiresAt", expiresAt,
                "refreshExpiresAt", refreshExpiresAt
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response,
                                          @RequestBody(required = false) RefreshTokenRequestDTO body) {
        String refreshToken = resolveRefreshToken(request, body);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token provided");
        }

        try {
            if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }

            // Reject revoked refresh tokens
            String jti = safeExtractJti(refreshToken);
            if (jti != null && tokenBlocklistService.isBlocked(jti)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token has been revoked");
            }

            String role = jwtUtil.extractRole(refreshToken);

            if (RoleConstants.ADMIN.equals(role)) {
                String username = jwtUtil.extractUsername(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                String newAccess = jwtUtil.generateToken(userDetails);
                String newRefresh = jwtUtil.generateRefreshToken(userDetails);

                // Revoke old refresh token
                if (jti != null) {
                    tokenBlocklistService.block(jti, System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs());
                }

                setAdminCookies(response, newAccess, newRefresh);
                return ResponseEntity.ok(Map.of("role", RoleConstants.ADMIN, "expiresIn", jwtUtil.getAdminTokenExpirationMs()));

            } else if (RoleConstants.PUBLIC.equals(role)) {
                String email = jwtUtil.extractEmail(refreshToken);
                String newAccess = jwtUtil.generatePublicToken(email);
                String newRefresh = jwtUtil.generatePublicRefreshToken(email);
                long expiresAt = System.currentTimeMillis() + jwtUtil.getPublicTokenExpirationMs();
                long refreshExpiresAt = System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs();

                if (jti != null) {
                    tokenBlocklistService.block(jti, System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs());
                }

                setPublicCookies(response, newAccess, newRefresh);
                return ResponseEntity.ok(Map.of(
                        "role", RoleConstants.PUBLIC,
                        "email", email,
                        "expiresAt", expiresAt,
                        "refreshExpiresAt", refreshExpiresAt
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token role");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token refresh failed");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Revoke any refresh tokens found in cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String name = cookie.getName();
                if ("admin_refresh_token".equals(name) || "public_refresh_token".equals(name)) {
                    try {
                        String jti = jwtUtil.extractJti(cookie.getValue());
                        if (jti != null) {
                            tokenBlocklistService.block(jti, System.currentTimeMillis() + jwtUtil.getRefreshTokenExpirationMs());
                        }
                    } catch (Exception ignored) { }
                }
            }
        }

        clearCookies(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
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
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request,
                                            Authentication authentication) {
        String username = authentication.getName();
        AdminUser admin = adminRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin não encontrado"));

        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password antiga é incorreta");
        }

        // Check against last 5 passwords
        List<PasswordHistory> history = passwordHistoryRepo.findTop5ByAdminOrderByChangedAtDesc(admin);
        for (PasswordHistory past : history) {
            if (passwordEncoder.matches(request.getNewPassword(), past.getPasswordHash())) {
                return ResponseEntity.badRequest().body("A nova password foi recentemente utilizada, escolha outra nova password");
            }
        }

        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepo.save(admin);

        PasswordHistory entry = new PasswordHistory();
        entry.setAdmin(admin);
        entry.setPasswordHash(admin.getPassword());
        entry.setChangedAt(LocalDateTime.now());
        passwordHistoryRepo.save(entry);

        // Keep only last 5 history entries
        List<PasswordHistory> allEntries = passwordHistoryRepo.findByAdminOrderByChangedAtDesc(admin);
        if (allEntries.size() > 5) {
            passwordHistoryRepo.deleteAll(allEntries.subList(5, allEntries.size()));
        }

        return ResponseEntity.ok("Password alterada com sucesso!");
    }

    // --- Cookie helpers ---

    private void setAdminCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie("admin_access_token", accessToken,
                (int) (jwtUtil.getAdminTokenExpirationMs() / 1000)));
        response.addHeader("Set-Cookie", buildCookie("admin_refresh_token", refreshToken,
                (int) (jwtUtil.getRefreshTokenExpirationMs() / 1000)));
    }

    private void setPublicCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie("public_access_token", accessToken,
                (int) (jwtUtil.getPublicTokenExpirationMs() / 1000)));
        response.addHeader("Set-Cookie", buildCookie("public_refresh_token", refreshToken,
                (int) (jwtUtil.getRefreshTokenExpirationMs() / 1000)));
    }

    private void clearCookies(HttpServletResponse response) {
        for (String name : List.of("admin_access_token", "admin_refresh_token",
                                   "public_access_token", "public_refresh_token")) {
            response.addHeader("Set-Cookie", buildCookie(name, "", 0));
        }
    }

    private String buildCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString();
    }

    private String resolveRefreshToken(HttpServletRequest request, RefreshTokenRequestDTO body) {
        // Prefer cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("admin_refresh_token".equals(cookie.getName()) ||
                    "public_refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Fall back to request body
        if (body != null) {
            return body.getRefreshToken();
        }
        return null;
    }

    private String safeExtractJti(String token) {
        try {
            return jwtUtil.extractJti(token);
        } catch (Exception e) {
            return null;
        }
    }
}
