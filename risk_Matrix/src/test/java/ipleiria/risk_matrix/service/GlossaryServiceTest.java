package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import ipleiria.risk_matrix.repository.GlossaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlossaryServiceTest {

    @Mock
    private GlossaryRepository glossaryRepository;

    @InjectMocks
    private GlossaryService glossaryService;

    private GlossaryEntry entry;

    @BeforeEach
    void setUp() {
        entry = new GlossaryEntry("Phishing", "Fraudulent attempt to obtain sensitive information");
        entry.setId(1L);
    }

    @Test
    void getAllEntries_returnsList() {
        when(glossaryRepository.findAll()).thenReturn(List.of(entry));

        List<GlossaryEntry> result = glossaryService.getAllEntries();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTerm()).isEqualTo("Phishing");
    }

    @Test
    void addEntry_savesAndReturns() {
        GlossaryEntry newEntry = new GlossaryEntry("Malware", "Malicious software");
        when(glossaryRepository.save(any(GlossaryEntry.class))).thenAnswer(inv -> {
            GlossaryEntry saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        GlossaryEntry result = glossaryService.addEntry(newEntry);

        assertThat(result.getTerm()).isEqualTo("Malware");
        assertThat(result.getId()).isEqualTo(2L);
        verify(glossaryRepository).save(newEntry);
    }

    @Test
    void updateEntry_setsIdAndSaves() {
        GlossaryEntry updated = new GlossaryEntry("Phishing", "Updated definition");
        when(glossaryRepository.save(any(GlossaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        GlossaryEntry result = glossaryService.updateEntry(1L, updated);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDefinition()).isEqualTo("Updated definition");
    }

    @Test
    void deleteEntry_callsRepository() {
        glossaryService.deleteEntry(1L);

        verify(glossaryRepository).deleteById(1L);
    }

    @Test
    void importEntries_newTerms_createsAll() {
        GlossaryEntry newEntry1 = new GlossaryEntry("Firewall", "Network security system");
        GlossaryEntry newEntry2 = new GlossaryEntry("VPN", "Virtual Private Network");

        when(glossaryRepository.findByTerm("Firewall")).thenReturn(Optional.empty());
        when(glossaryRepository.findByTerm("VPN")).thenReturn(Optional.empty());
        when(glossaryRepository.save(any(GlossaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        List<GlossaryEntry> result = glossaryService.importEntries(List.of(newEntry1, newEntry2));

        assertThat(result).hasSize(2);
        verify(glossaryRepository, times(2)).save(any(GlossaryEntry.class));
    }

    @Test
    void importEntries_existingTerm_updatesDefinition() {
        GlossaryEntry incoming = new GlossaryEntry("Phishing", "New definition");
        incoming.setId(99L);

        GlossaryEntry existing = new GlossaryEntry("Phishing", "Old definition");
        existing.setId(1L);

        when(glossaryRepository.findByTerm("Phishing")).thenReturn(Optional.of(existing));
        when(glossaryRepository.save(any(GlossaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        List<GlossaryEntry> result = glossaryService.importEntries(List.of(incoming));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDefinition()).isEqualTo("New definition");
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void importEntries_clearsIdOnIncoming() {
        GlossaryEntry incoming = new GlossaryEntry("NewTerm", "Some definition");
        incoming.setId(50L);

        when(glossaryRepository.findByTerm("NewTerm")).thenReturn(Optional.empty());
        when(glossaryRepository.save(any(GlossaryEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        glossaryService.importEntries(List.of(incoming));

        assertThat(incoming.getId()).isNull();
    }

    @Test
    void exportEntries_delegatesToFindAll() {
        when(glossaryRepository.findAll()).thenReturn(List.of(entry));

        List<GlossaryEntry> result = glossaryService.exportEntries();

        assertThat(result).hasSize(1);
        verify(glossaryRepository).findAll();
    }
}
