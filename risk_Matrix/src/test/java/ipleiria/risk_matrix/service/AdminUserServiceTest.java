package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.dto.AdminRegisterDTO;
import ipleiria.risk_matrix.dto.ChangePasswordRequestDTO;
import ipleiria.risk_matrix.exceptions.exception.ConflictException;
import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.models.users.PasswordHistory;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import ipleiria.risk_matrix.repository.PasswordHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private AdminUser adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new AdminUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
    }

    @Test
    void getAdminByUsername_existing_returnsOptionalWithUser() {
        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        Optional<AdminUser> result = adminUserService.getAdminByUsername("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    void getAdminByUsername_nonExisting_returnsEmpty() {
        when(adminUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<AdminUser> result = adminUserService.getAdminByUsername("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void getAllAdmins_returnsList() {
        when(adminUserRepository.findAll()).thenReturn(List.of(adminUser));

        List<AdminUser> result = adminUserService.getAllAdmins();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("admin");
    }

    @Test
    void register_validInput_savesAdminAndPasswordHistory() {
        AdminRegisterDTO dto = new AdminRegisterDTO();
        dto.setUsername("newadmin");
        dto.setEmail("newadmin@example.com");
        dto.setPassword("StrongPass123!");

        when(adminUserRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(adminUserRepository.findByEmail("newadmin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass123!")).thenReturn("encodedNewPass");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(inv -> inv.getArgument(0));

        adminUserService.register(dto);

        verify(adminUserRepository).save(any(AdminUser.class));
        verify(passwordHistoryRepository).save(any(PasswordHistory.class));
    }

    @Test
    void register_duplicateUsername_throwsConflictException() {
        AdminRegisterDTO dto = new AdminRegisterDTO();
        dto.setUsername("admin");
        dto.setEmail("other@example.com");
        dto.setPassword("Pass123!");

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminUserService.register(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void register_duplicateEmail_throwsConflictException() {
        AdminRegisterDTO dto = new AdminRegisterDTO();
        dto.setUsername("newadmin");
        dto.setEmail("admin@example.com");
        dto.setPassword("Pass123!");

        when(adminUserRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(adminUserRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> adminUserService.register(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void changePassword_validInput_updatesPassword() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123!");

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordHistoryRepository.findTop5ByAdminOrderByChangedAtDesc(adminUser)).thenReturn(List.of());
        when(passwordEncoder.encode("newPass123!")).thenReturn("encodedNewPass");
        when(adminUserRepository.save(any(AdminUser.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordHistoryRepository.findByAdminOrderByChangedAtDesc(adminUser)).thenReturn(new ArrayList<>());

        adminUserService.changePassword("admin", request);

        verify(adminUserRepository).save(adminUser);
        verify(passwordHistoryRepository).save(any(PasswordHistory.class));
    }

    @Test
    void changePassword_wrongOldPassword_throwsIllegalArgument() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("wrongPass");
        request.setNewPassword("newPass123!");

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> adminUserService.changePassword("admin", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incorreta");
    }

    @Test
    void changePassword_recentlyUsedPassword_throwsIllegalArgument() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("oldPass");
        request.setNewPassword("reusedPass");

        PasswordHistory pastEntry = new PasswordHistory();
        pastEntry.setPasswordHash("encodedReusedPass");
        pastEntry.setChangedAt(LocalDateTime.now());

        when(adminUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordHistoryRepository.findTop5ByAdminOrderByChangedAtDesc(adminUser))
                .thenReturn(List.of(pastEntry));
        when(passwordEncoder.matches("reusedPass", "encodedReusedPass")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.changePassword("admin", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("recentemente utilizada");
    }

    @Test
    void changePassword_nonExistingUser_throwsUsernameNotFound() {
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass!");

        when(adminUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.changePassword("ghost", request))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
