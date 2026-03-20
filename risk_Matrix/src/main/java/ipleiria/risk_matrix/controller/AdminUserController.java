package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.dto.AdminUserResponseDTO;
import ipleiria.risk_matrix.exceptions.exception.NotFoundException;
import ipleiria.risk_matrix.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponseDTO getAdminByUsername(@PathVariable String username) {
        return adminUserService.getAdminByUsername(username)
                .map(AdminUserResponseDTO::new)
                .orElseThrow(() -> new NotFoundException("Admin not found: " + username));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponseDTO> getAllAdmins() {
        return adminUserService.getAllAdmins().stream()
                .map(AdminUserResponseDTO::new)
                .toList();
    }
}
