package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.users.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    // Busca um admin pelo username
    Optional<AdminUser> findByUsername(String username);

    // Busca um admin pelo email
    Optional<AdminUser> findByEmail(String email);

}
