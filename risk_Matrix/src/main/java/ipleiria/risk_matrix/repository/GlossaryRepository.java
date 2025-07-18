package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlossaryRepository extends JpaRepository<GlossaryEntry, Long> {
}