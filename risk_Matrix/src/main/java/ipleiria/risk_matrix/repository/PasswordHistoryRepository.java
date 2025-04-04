package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.models.users.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    List<PasswordHistory> findTop3ByAdminOrderByChangedAtDesc(AdminUser admin);
    List<PasswordHistory> findByAdminOrderByChangedAtDesc(AdminUser admin);

}
