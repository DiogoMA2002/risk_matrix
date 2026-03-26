package ipleiria.risk_matrix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import ipleiria.risk_matrix.dto.GlossaryEntryDTO;
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
    public List<GlossaryEntryDTO> getAllEntries() {
        return glossaryService.getAllEntries().stream().map(GlossaryEntryDTO::new).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Add a glossary entry", description = "Creates a new glossary term. Requires ADMIN role.")
    @ApiResponse(responseCode = "201", description = "Entry created")
    public ResponseEntity<GlossaryEntryDTO> addEntry(@RequestBody GlossaryEntryDTO entryDto) {
        return new ResponseEntity<>(new GlossaryEntryDTO(glossaryService.addEntry(entryDto.toEntity())), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a glossary entry", description = "Updates an existing glossary term. Requires ADMIN role.")
    public ResponseEntity<GlossaryEntryDTO> updateEntry(
            @Parameter(description = "Entry ID") @PathVariable Long id,
            @RequestBody GlossaryEntryDTO entryDto) {
        return ResponseEntity.ok(new GlossaryEntryDTO(glossaryService.updateEntry(id, entryDto.toEntity())));
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
    public ResponseEntity<List<GlossaryEntryDTO>> importEntries(@RequestBody List<GlossaryEntryDTO> entries) {
        List<GlossaryEntry> imported = glossaryService.importEntries(entries.stream().map(GlossaryEntryDTO::toEntity).toList());
        return ResponseEntity.ok(imported.stream().map(GlossaryEntryDTO::new).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    @Operation(summary = "Export all glossary entries", description = "Returns all glossary entries for export. Requires ADMIN role.")
    public ResponseEntity<List<GlossaryEntryDTO>> exportEntries() {
        return ResponseEntity.ok(glossaryService.exportEntries().stream().map(GlossaryEntryDTO::new).toList());
    }
} 