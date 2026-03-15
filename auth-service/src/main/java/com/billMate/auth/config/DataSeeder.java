package com.billMate.auth.config;

import com.billMate.auth.model.Role;
import com.billMate.auth.model.User;
import com.billMate.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void seedAfterStartup() {
        seedUser("admin", "admin@mail.com", "admin123", Role.ADMIN);
        seedUser("testuser", "testuser@billmate.com", "Test1234!", Role.USER);
    }

    private void seedUser(String username, String email, String rawPassword, Role role) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .build());

        user.setUsername(username);
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    @Override
    public void run(String... args) throws Exception {
        // no-op kept for compatibility
    }
}

