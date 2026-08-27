package com.resourcebooking.controller;

import com.resourcebooking.dto.auth.LoginRequest;
import com.resourcebooking.dto.auth.LoginResponse;
import com.resourcebooking.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}