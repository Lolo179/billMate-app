package com.billMate.auth.controller;

import com.billMate.auth.dto.AuthResponse;
import com.billMate.auth.dto.LoginRequest;
import com.billMate.auth.dto.RegisterRequest;
import com.billMate.auth.model.UserDTO;
import com.billMate.auth.service.AuthService;
import com.billMate.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;


@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest){
        log.info(">> POST /auth/register", kv("email", registerRequest.getEmail()));
        AuthResponse response = authService.register(registerRequest);
        log.info("<< POST /auth/register - registered", kv("email", registerRequest.getEmail()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){
        log.info(">> POST /auth/login", kv("email", loginRequest.getEmail()));
        AuthResponse response = authService.login(loginRequest);
        log.info("<< POST /auth/login - success", kv("email", loginRequest.getEmail()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        log.info(">> GET /auth/users");
        String token = authHeader.replace("Bearer ", "");
        List<UserDTO> users = authService.getAllUsers(token);
        log.info("<< GET /auth/users", kv("count", users.size()));
        return ResponseEntity.ok(users);
    }




}
