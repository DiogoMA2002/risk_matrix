package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.users.AdminUser;
import lombok.Getter;

@Getter
public class AdminUserResponseDTO {

    private final Long id;
    private final String username;
    private final String email;

    public AdminUserResponseDTO(AdminUser admin) {
        this.id = admin.getId();
        this.username = admin.getUsername();
        this.email = admin.getEmail();
    }
}
