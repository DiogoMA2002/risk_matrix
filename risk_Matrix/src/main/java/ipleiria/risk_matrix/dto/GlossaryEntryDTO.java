package ipleiria.risk_matrix.dto;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlossaryEntryDTO {
    private Long id;
    private String term;
    private String definition;

    public GlossaryEntryDTO() {
    }

    public GlossaryEntryDTO(GlossaryEntry entry) {
        this.id = entry.getId();
        this.term = entry.getTerm();
        this.definition = entry.getDefinition();
    }

    public GlossaryEntry toEntity() {
        GlossaryEntry entry = new GlossaryEntry();
        entry.setId(this.id);
        entry.setTerm(this.term);
        entry.setDefinition(this.definition);
        return entry;
    }
}
