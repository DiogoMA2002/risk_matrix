package ipleiria.risk_matrix.controller;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<AdminUser> getAdminByUsername(@PathVariable String username) {
        return adminUserService.getAdminByUsername(username);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUser> getAllAdmins() {
        return adminUserService.getAllAdmins();
    }
}
