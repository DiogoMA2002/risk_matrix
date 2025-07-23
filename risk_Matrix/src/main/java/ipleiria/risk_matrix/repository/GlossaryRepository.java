package ipleiria.risk_matrix.repository;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlossaryRepository extends JpaRepository<GlossaryEntry, Long> {
    Optional<GlossaryEntry> findByTerm(String term);
}