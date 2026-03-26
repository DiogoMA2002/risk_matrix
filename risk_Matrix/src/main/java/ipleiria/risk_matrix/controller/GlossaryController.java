package ipleiria.risk_matrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import ipleiria.risk_matrix.service.GlossaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/glossary")
@Tag(name = "Glossary", description = "CRUD and import/export operations for glossary terms")
public class GlossaryController {
    private final GlossaryService glossaryService;

    public GlossaryController(GlossaryService glossaryService) {
        this.glossaryService = glossaryService;
    }

    @GetMapping
    @Operation(summary = "List all glossary entries")
    public List<GlossaryEntry> getAllEntries() {
        return glossaryService.getAllEntries();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Add a glossary entry", description = "Creates a new glossary term. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Entry created")
    public ResponseEntity<GlossaryEntry> addEntry(@RequestBody GlossaryEntry entry) {
        return new ResponseEntity<>(glossaryService.addEntry(entry), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a glossary entry", description = "Updates an existing glossary term. Requires ADMIN role.")
    public ResponseEntity<GlossaryEntry> updateEntry(
            @Parameter(description = "Entry ID") @PathVariable Long id,
            @RequestBody GlossaryEntry entry) {
        return ResponseEntity.ok(glossaryService.updateEntry(id, entry));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a glossary entry", description = "Deletes a glossary entry by ID. Requires ADMIN role.")
    @ApiResponse(responseCode = "204", description = "Entry deleted")
    public ResponseEntity<Void> deleteEntry(@Parameter(description = "Entry ID") @PathVariable Long id) {
        glossaryService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/import")
    @Operation(summary = "Import glossary entries", description = "Bulk-imports glossary entries. Existing terms are updated, new terms are created. Requires ADMIN role.")
    public ResponseEntity<List<GlossaryEntry>> importEntries(@RequestBody List<GlossaryEntry> entries) {
        return ResponseEntity.ok(glossaryService.importEntries(entries));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    @Operation(summary = "Export all glossary entries", description = "Returns all glossary entries for export. Requires ADMIN role.")
    public ResponseEntity<List<GlossaryEntry>> exportEntries() {
        return ResponseEntity.ok(glossaryService.exportEntries());
    }
} 