package ipleiria.risk_matrix.models.users;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private AdminUser admin;

    private LocalDateTime changedAt;

}