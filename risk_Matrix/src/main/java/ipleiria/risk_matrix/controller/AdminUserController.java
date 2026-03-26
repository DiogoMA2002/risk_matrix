package ipleiria.risk_matrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.dto.AdminUserResponseDTO;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Users", description = "Admin user management endpoints")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin by username", description = "Returns admin user details by username. Requires ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Admin found")
    @ApiResponse(responseCode = "404", description = "Admin not found")
    public AdminUserResponseDTO getAdminByUsername(
            @Parameter(description = "Admin username") @PathVariable String username) {
        return adminUserService.getAdminByUsername(username)
                .map(AdminUserResponseDTO::new)
                .orElseThrow(() -> new NotFoundException("Admin not found: " + username));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all admins", description = "Returns all registered admin users. Requires ADMIN role.")
    public List<AdminUserResponseDTO> getAllAdmins() {
        return adminUserService.getAllAdmins().stream()
                .map(AdminUserResponseDTO::new)
                .toList();
    }
}
