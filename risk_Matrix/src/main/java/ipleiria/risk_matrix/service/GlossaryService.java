package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import ipleiria.risk_matrix.repository.GlossaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class GlossaryService {
    private final GlossaryRepository glossaryRepository;

    public GlossaryService(GlossaryRepository glossaryRepository) {
        this.glossaryRepository = glossaryRepository;
    }

    public List<GlossaryEntry> getAllEntries() {
        return glossaryRepository.findAll();
    }

    public GlossaryEntry addEntry(GlossaryEntry entry) {
        return glossaryRepository.save(entry);
    }

    public GlossaryEntry updateEntry(Long id, GlossaryEntry entry) {
        entry.setId(id);
        return glossaryRepository.save(entry);
    }

    public void deleteEntry(Long id) {
        glossaryRepository.deleteById(id);
    }

    public List<GlossaryEntry> importEntries(List<GlossaryEntry> entries) {
        List<GlossaryEntry> importedEntries = new ArrayList<>();
        
        for (GlossaryEntry entry : entries) {
            // Clear the ID to allow new ID generation
            entry.setId(null);
            
            // Check if term already exists
            Optional<GlossaryEntry> existingEntry = glossaryRepository.findByTerm(entry.getTerm());
            
            if (existingEntry.isPresent()) {
                // Update existing entry with new definition
                GlossaryEntry existing = existingEntry.get();
                existing.setDefinition(entry.getDefinition());
                importedEntries.add(glossaryRepository.save(existing));
            } else {
                // Create new entry
                importedEntries.add(glossaryRepository.save(entry));
            }
        }
        
        return importedEntries;
    }

    public List<GlossaryEntry> exportEntries() {
        return glossaryRepository.findAll();
    }
} 