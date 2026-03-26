package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.dto.AdminUserResponseDTO;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserService adminUserService;

    private AdminUser admin;

    @BeforeEach
    void setUp() {
        admin = new AdminUser();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
    }

    @Test
    void getAdminByUsername_found_returns200() throws Exception {
        when(adminUserService.getAdminByUsername("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/api/admin/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.email", is("admin@example.com")));
    }

    @Test
    void getAdminByUsername_missing_returns404() throws Exception {
        when(adminUserService.getAdminByUsername("ghost")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/ghost"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAdmins_returns200WithList() throws Exception {
        when(adminUserService.getAllAdmins()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("admin")));
    }
}
