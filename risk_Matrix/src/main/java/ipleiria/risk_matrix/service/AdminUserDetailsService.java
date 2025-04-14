package ipleiria.risk_matrix.service;

import ipleiria.risk_matrix.models.users.AdminUser;
import ipleiria.risk_matrix.repository.AdminUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminRepo;


    public AdminUserDetailsService(AdminUserRepository adminRepo) {
        this.adminRepo = adminRepo;

    }
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Check if the input looks like an email
        Optional<AdminUser> userOptional;
        if (usernameOrEmail != null && usernameOrEmail.contains("@")) {
            // Attempt to find by email
            userOptional = adminRepo.findByEmail(usernameOrEmail);
        } else {
            // Attempt to find by username
            userOptional = adminRepo.findByUsername(usernameOrEmail);
        }

        AdminUser user = userOptional.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with identifier: " + usernameOrEmail)
        );
        
        return new User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
