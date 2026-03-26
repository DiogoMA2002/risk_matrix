package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.config.SecurityConfig;
import ipleiria.risk_matrix.dto.EmailTokenRequestDTO;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.AdminUserService;
import ipleiria.risk_matrix.service.CategoryService;
import ipleiria.risk_matrix.service.TokenBlocklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, CategoryController.class},
        properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc(addFilters = true)
@Import({WebMvcTestConfig.class, SecurityConfig.class})
class SecurityAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private TokenBlocklistService tokenBlocklistService;

    @BeforeEach
    void setUp() {
        when(jwtUtil.getPublicTokenExpirationMs()).thenReturn(7_200_000L);
        when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
        when(jwtUtil.generatePublicToken("public@example.com")).thenReturn("pub-token");
        when(jwtUtil.generatePublicRefreshToken("public@example.com")).thenReturn("pub-refresh");
        when(categoryService.getAllCategories()).thenReturn(List.of());
    }

    @Test
    void permitAllEndpoint_requestToken_allowsUnauthenticated() throws Exception {
        EmailTokenRequestDTO dto = new EmailTokenRequestDTO("public@example.com");

        mockMvc.perform(post("/api/auth/request-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_isForbidden() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withValidPublicToken_isAllowed() throws Exception {
        when(jwtUtil.validateToken("pub-token")).thenReturn(true);
        when(jwtUtil.extractRole("pub-token")).thenReturn("public");
        when(jwtUtil.extractJti("pub-token")).thenReturn("jti-1");
        when(tokenBlocklistService.isBlocked("jti-1")).thenReturn(false);
        when(jwtUtil.extractEmail("pub-token")).thenReturn("public@example.com");

        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("public_access_token", "pub-token")))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withBlockedToken_isForbidden() throws Exception {
        when(jwtUtil.validateToken("pub-token")).thenReturn(true);
        when(jwtUtil.extractRole("pub-token")).thenReturn("public");
        when(jwtUtil.extractJti("pub-token")).thenReturn("jti-1");
        when(tokenBlocklistService.isBlocked("jti-1")).thenReturn(true);

        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("public_access_token", "pub-token")))
                .andExpect(status().isForbidden());
    }
}
