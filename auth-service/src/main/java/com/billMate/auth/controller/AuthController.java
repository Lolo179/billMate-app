package com.billMate.auth.controller;

import com.billMate.auth.dto.AuthResponse;
import com.billMate.auth.dto.LoginRequest;
import com.billMate.auth.dto.RegisterRequest;
import com.billMate.auth.model.User;
import com.billMate.auth.model.UserDTO;
import com.billMate.auth.service.AuthService;
import com.billMate.auth.service.JwtService;
import com.billMate.auth.service.impl.AuthServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        System.out.println("🟢 Entró al endpoint");
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(authService.getAllUsers(token));
    }




}
