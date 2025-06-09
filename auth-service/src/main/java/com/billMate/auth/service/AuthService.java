package com.billMate.auth.service;

import com.billMate.auth.dto.AuthResponse;
import com.billMate.auth.dto.LoginRequest;
import com.billMate.auth.dto.RegisterRequest;
import com.billMate.auth.model.User;
import com.billMate.auth.model.UserDTO;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    List<UserDTO> getAllUsers(String token);
}
