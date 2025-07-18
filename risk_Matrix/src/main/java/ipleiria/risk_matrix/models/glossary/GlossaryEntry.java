package ipleiria.risk_matrix.models.glossary;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "glossary")
public class GlossaryEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String term;

    @Column(nullable = false)
    private String definition;

    public GlossaryEntry() {}

    public GlossaryEntry(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }
} 