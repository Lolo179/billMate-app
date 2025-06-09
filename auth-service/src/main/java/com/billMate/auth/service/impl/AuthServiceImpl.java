package com.billMate.auth.service.impl;

import com.billMate.auth.dto.AuthResponse;
import com.billMate.auth.dto.LoginRequest;
import com.billMate.auth.dto.RegisterRequest;
import com.billMate.auth.model.Role;
import com.billMate.auth.model.User;
import com.billMate.auth.model.UserDTO;
import com.billMate.auth.repository.UserRepository;
import com.billMate.auth.service.AuthService;
import com.billMate.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Role selectedRole;
        try {
            selectedRole = Role.valueOf(request.getRole().toUpperCase()); // puede ser USER o ADMIN
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rol inválido: " + request.getRole());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(selectedRole))
                .build();

        userRepository.save(user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::name).toList());

        String token = jwtService.generateToken(claims, user.getEmail());

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

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles().stream().map(Role::name).toList());
        String token = jwtService.generateToken(claims,user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public List<UserDTO> getAllUsers(String token) {
        List<String> roles = jwtService.extractRoles(token);
        if (!roles.contains("ADMIN")) {
            System.out.println("🚫 Acceso denegado: No es ADMIN");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado");
        }

        return userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream().map(Enum::name).toList()
                ))
                .toList();
    }


}

