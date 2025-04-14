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
        // Try finding by username first
        Optional<AdminUser> userByUsername = adminRepo.findByUsername(usernameOrEmail);

        if (userByUsername.isPresent()) {
            AdminUser user = userByUsername.get();
            return new User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        // If not found by username, try finding by email
        Optional<AdminUser> userByEmail = adminRepo.findByEmail(usernameOrEmail);
        if (userByEmail.isPresent()) {
            AdminUser user = userByEmail.get();
            // IMPORTANT: Return UserDetails with the *actual username*, not the email used for lookup
            return new User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        // If not found by either, throw the exception
        throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);

        // AdminUser user = adminRepo.findByUsername(username)
        //         .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // return new User(user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
