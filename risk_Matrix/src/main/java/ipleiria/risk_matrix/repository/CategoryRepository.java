package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    boolean existsByNameIgnoreCase(String newName);
}
