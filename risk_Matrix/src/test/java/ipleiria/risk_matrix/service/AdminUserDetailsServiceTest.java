package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    @Mock
    private AdminUserRepository adminRepo;

    @InjectMocks
    private AdminUserDetailsService adminUserDetailsService;

    @Test
    void loadUserByUsername_withUsername_looksUpByUsername() {
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPassword("encoded");

        when(adminRepo.findByUsername("admin")).thenReturn(Optional.of(admin));

        UserDetails details = adminUserDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");
        verify(adminRepo).findByUsername("admin");
    }

    @Test
    void loadUserByUsername_withEmail_looksUpByEmail() {
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPassword("encoded");
        admin.setEmail("admin@example.com");

        when(adminRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));

        UserDetails details = adminUserDetailsService.loadUserByUsername("admin@example.com");

        assertThat(details.getUsername()).isEqualTo("admin");
        verify(adminRepo).findByEmail("admin@example.com");
    }

    @Test
    void loadUserByUsername_missing_throwsUsernameNotFoundException() {
        when(adminRepo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with identifier: ghost");
    }
}
