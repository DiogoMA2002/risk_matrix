package ipleiria.risk_matrix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ipleiria.risk_matrix.config.JwtUtil;
import ipleiria.risk_matrix.dto.*;
import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.service.AdminUserDetailsService;
import ipleiria.risk_matrix.service.AdminUserService;
import ipleiria.risk_matrix.service.TokenBlocklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private AuthRequestDTO authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequestDTO("admin", "Strong123!");
        when(jwtUtil.getAdminTokenExpirationMs()).thenReturn(86_400_000L);
        when(jwtUtil.getPublicTokenExpirationMs()).thenReturn(7_200_000L);
        when(jwtUtil.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
    }

    @Test
    void login_success_returns200AndRole() throws Exception {
        UserDetails userDetails = User.withUsername("admin").password("x").authorities("ROLE_ADMIN").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", "Strong123!", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(adminUserDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("admin")))
                .andExpect(jsonPath("$.expiresIn", is(86400000)));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void requestToken_returnsPublicRole() throws Exception {
        EmailTokenRequestDTO dto = new EmailTokenRequestDTO("public@example.com");
        when(jwtUtil.generatePublicToken("public@example.com")).thenReturn("pub-access");
        when(jwtUtil.generatePublicRefreshToken("public@example.com")).thenReturn("pub-refresh");

        mockMvc.perform(post("/api/auth/request-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("public")))
                .andExpect(jsonPath("$.email", is("public@example.com")));
    }

    @Test
    void refresh_withNoToken_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("No refresh token provided"));
    }

    @Test
    void refresh_adminFlow_returns200() throws Exception {
        RefreshTokenRequestDTO body = new RefreshTokenRequestDTO("r1");
        UserDetails userDetails = User.withUsername("admin").password("x").authorities("ROLE_ADMIN").build();

        when(jwtUtil.validateToken("r1")).thenReturn(true);
        when(jwtUtil.isRefreshToken("r1")).thenReturn(true);
        when(jwtUtil.extractJti("r1")).thenReturn("jti-1");
        when(tokenBlocklistService.isBlocked("jti-1")).thenReturn(false);
        when(jwtUtil.extractRole("r1")).thenReturn("admin");
        when(jwtUtil.extractUsername("r1")).thenReturn("admin");
        when(adminUserDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn("new-refresh");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("admin")));
    }

    @Test
    void register_conflict_returns409() throws Exception {
        AdminRegisterDTO dto = new AdminRegisterDTO();
        dto.setUsername("admin");
        dto.setEmail("admin@example.com");
        dto.setPassword("Strong123!");

        doThrow(new ConflictException("Username já existe")).when(adminUserService).register(any(AdminRegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void changePassword_validationError_returns400() throws Exception {
        ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO();
        dto.setOldPassword("OldPass1!");
        dto.setNewPassword("weak");
        dto.setConfirmNewPassword("weak");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
