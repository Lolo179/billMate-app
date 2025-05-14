package com.billMate.auth.service.impl;

import com.billMate.auth.dto.AuthResponse;
import com.billMate.auth.dto.LoginRequest;
import com.billMate.auth.dto.RegisterRequest;
import com.billMate.auth.model.Role;
import com.billMate.auth.model.User;
import com.billMate.auth.repository.UserRepository;
import com.billMate.auth.service.AuthService;
import com.billMate.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username  ya registrado");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        System.out.println("LOGIN --> email: " + request.getEmail());
        System.out.println("LOGIN --> raw password: " + request.getPassword());
        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}

