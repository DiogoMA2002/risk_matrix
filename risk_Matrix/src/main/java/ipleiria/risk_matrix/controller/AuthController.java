package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.utils.RoleConstants;
import ipleiria.risk_matrix.dto.AdminRegisterDTO;
import ipleiria.risk_matrix.dto.AuthRequestDTO;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import ipleiria.risk_matrix.dto.EmailTokenRequestDTO;
import ipleiria.risk_matrix.dto.RefreshTokenRequestDTO;
import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.AdminUserService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, logout, token management, registration, and password changes")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AdminUserDetailsService userDetailsService;
    private final AdminUserService adminUserService;
    private final TokenBlocklistService tokenBlocklistService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil,
                          AdminUserDetailsService userDetailsService,
                          AdminUserService adminUserService,
                          TokenBlocklistService tokenBlocklistService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.adminUserService = adminUserService;
        this.tokenBlocklistService = tokenBlocklistService;
    }

    @PostMapping("/login")
    @Operation(summary = "Admin login", description = "Authenticates an admin user and sets JWT cookies (access + refresh)")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request, HttpServletResponse response) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            setAdminCookies(response, accessToken, refreshToken);
            clearPublicCookies(response);

            return ResponseEntity.ok(Map.of(
                    "role", RoleConstants.ADMIN,
                    "expiresIn", jwtUtil.getAdminTokenExpirationMs()
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/request-token")
    @Operation(summary = "Request public token", description = "Issues a public JWT for anonymous users identified by email")
    @ApiResponse(responseCode = "200", description = "Token issued")
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
    @Operation(summary = "Refresh token", description = "Rotates JWT access and refresh tokens. Old refresh token is revoked.")
    @ApiResponse(responseCode = "200", description = "Tokens refreshed")
    @ApiResponse(responseCode = "401", description = "Invalid or missing refresh token")
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
    @Operation(summary = "Logout", description = "Revokes all JWT cookies and blocklists the current tokens")
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Blocklist both access and refresh tokens found in cookies.
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                String name = cookie.getName();
                boolean isAccess  = "admin_access_token".equals(name)  || "public_access_token".equals(name);
                boolean isRefresh = "admin_refresh_token".equals(name) || "public_refresh_token".equals(name);
                if (isAccess || isRefresh) {
                    try {
                        String jti = jwtUtil.extractJti(cookie.getValue());
                        if (jti != null) {
                            // Access tokens expire sooner; refresh tokens after 7 days.
                            long ttlMs = isRefresh
                                    ? jwtUtil.getRefreshTokenExpirationMs()
                                    : jwtUtil.getAdminTokenExpirationMs();
                            tokenBlocklistService.block(jti, System.currentTimeMillis() + ttlMs);
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
    @Operation(summary = "Register a new admin", description = "Creates a new admin user. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Admin created")
    @ApiResponse(responseCode = "409", description = "Username or email already exists")
    public ResponseEntity<?> register(@Valid @RequestBody AdminRegisterDTO dto) {
        try {
            adminUserService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Admin criado com sucesso");
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Change password", description = "Changes the authenticated admin's password. Revokes current session tokens. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "New password recently used")
    @ApiResponse(responseCode = "401", description = "Old password incorrect")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request,
                                            Authentication authentication,
                                            HttpServletRequest httpRequest,
                                            HttpServletResponse httpResponse) {
        try {
            adminUserService.changePassword(authentication.getName(), request);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("incorreta")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        }

        revokeAdminCookies(httpRequest);
        clearCookies(httpResponse);

        return ResponseEntity.ok("Password alterada com sucesso!");
    }

    private void revokeAdminCookies(HttpServletRequest httpRequest) {
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                String name = cookie.getName();
                boolean isAccess  = "admin_access_token".equals(name);
                boolean isRefresh = "admin_refresh_token".equals(name);
                if (isAccess || isRefresh) {
                    try {
                        String jti = jwtUtil.extractJti(cookie.getValue());
                        if (jti != null) {
                            long ttlMs = isRefresh
                                    ? jwtUtil.getRefreshTokenExpirationMs()
                                    : jwtUtil.getAdminTokenExpirationMs();
                            tokenBlocklistService.block(jti, System.currentTimeMillis() + ttlMs);
                        }
                    } catch (Exception ignored) { }
                }
            }
        }
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

    private void clearPublicCookies(HttpServletResponse response) {
        for (String name : List.of("public_access_token", "public_refresh_token")) {
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
        // Read both named cookies independently so we never accidentally use the wrong role's token.
        String adminRefresh = null;
        String publicRefresh = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("admin_refresh_token".equals(cookie.getName()))  adminRefresh  = cookie.getValue();
                if ("public_refresh_token".equals(cookie.getName())) publicRefresh = cookie.getValue();
            }
        }

        // Prefer the admin cookie when present and its embedded role claim confirms it is admin.
        // Fall back to the public cookie, then to the request body.
        if (adminRefresh != null && RoleConstants.ADMIN.equals(safeExtractRole(adminRefresh))) {
            return adminRefresh;
        }
        if (publicRefresh != null && RoleConstants.PUBLIC.equals(safeExtractRole(publicRefresh))) {
            return publicRefresh;
        }
        if (body != null) {
            return body.getRefreshToken();
        }
        return null;
    }

    private String safeExtractRole(String token) {
        try {
            return jwtUtil.extractRole(token);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeExtractJti(String token) {
        try {
            return jwtUtil.extractJti(token);
        } catch (Exception e) {
            return null;
        }
    }
}
