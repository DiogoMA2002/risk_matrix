package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.models.glossary.GlossaryEntry;
import ipleiria.risk_matrix.service.GlossaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlossaryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class GlossaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GlossaryService glossaryService;

    private GlossaryEntry entry;

    @BeforeEach
    void setUp() {
        entry = new GlossaryEntry("Phishing", "Fraudulent attempt to obtain sensitive information");
        entry.setId(1L);
    }

    @Test
    void getAllEntries_returnsList() throws Exception {
        when(glossaryService.getAllEntries()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/glossary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].term", is("Phishing")));
    }

    @Test
    void addEntry_validInput_returns201() throws Exception {
        when(glossaryService.addEntry(any(GlossaryEntry.class))).thenReturn(entry);

        mockMvc.perform(post("/api/glossary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.term", is("Phishing")));
    }

    @Test
    void updateEntry_validInput_returns200() throws Exception {
        GlossaryEntry updated = new GlossaryEntry("Phishing", "Updated definition");
        updated.setId(1L);
        when(glossaryService.updateEntry(eq(1L), any(GlossaryEntry.class))).thenReturn(updated);

        mockMvc.perform(put("/api/glossary/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definition", is("Updated definition")));
    }

    @Test
    void deleteEntry_returns204() throws Exception {
        doNothing().when(glossaryService).deleteEntry(1L);

        mockMvc.perform(delete("/api/glossary/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void importEntries_returnsList() throws Exception {
        GlossaryEntry e1 = new GlossaryEntry("Firewall", "Network security system");
        e1.setId(2L);
        when(glossaryService.importEntries(any())).thenReturn(List.of(e1));

        mockMvc.perform(post("/api/glossary/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(
                                new GlossaryEntry("Firewall", "Network security system")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].term", is("Firewall")));
    }

    @Test
    void exportEntries_returnsList() throws Exception {
        when(glossaryService.exportEntries()).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/glossary/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
