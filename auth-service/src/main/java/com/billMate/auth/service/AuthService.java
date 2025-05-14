package com.billMate.auth.service;

import com.billMate.auth.dto.AuthResponse;
import com.billMate.auth.dto.LoginRequest;
import com.billMate.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
