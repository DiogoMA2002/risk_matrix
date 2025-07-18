package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import ipleiria.risk_matrix.repository.GlossaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return glossaryRepository.saveAll(entries);
    }

    public List<GlossaryEntry> exportEntries() {
        return glossaryRepository.findAll();
    }
} 