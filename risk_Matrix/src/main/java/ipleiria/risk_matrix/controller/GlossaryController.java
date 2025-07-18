package ipleiria.risk_matrix.controller;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import ipleiria.risk_matrix.service.GlossaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glossary")
public class GlossaryController {
    @Autowired
    private GlossaryService glossaryService;

    @GetMapping
    public List<GlossaryEntry> getAllEntries() {
        return glossaryService.getAllEntries();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GlossaryEntry> addEntry(@RequestBody GlossaryEntry entry) {
        return new ResponseEntity<>(glossaryService.addEntry(entry), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GlossaryEntry> updateEntry(@PathVariable Long id, @RequestBody GlossaryEntry entry) {
        return ResponseEntity.ok(glossaryService.updateEntry(id, entry));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        glossaryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    public ResponseEntity<List<GlossaryEntry>> importEntries(@RequestBody List<GlossaryEntry> entries) {
        return ResponseEntity.ok(glossaryService.importEntries(entries));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<List<GlossaryEntry>> exportEntries() {
        return ResponseEntity.ok(glossaryService.exportEntries());
    }
} 