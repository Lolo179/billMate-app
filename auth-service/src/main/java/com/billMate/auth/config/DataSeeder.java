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
        if (userRepository.existsByEmail("admin@mail.com")) return;

        User user = User.builder()
                .username("admin")
                .email("admin@mail.com")
                .password(passwordEncoder.encode("admin123"))
                .roles(Set.of(Role.ADMIN))
                .build();

        userRepository.save(user);
    }

    @Override
    public void run(String... args) throws Exception {
        // no-op kept for compatibility
    }
}

