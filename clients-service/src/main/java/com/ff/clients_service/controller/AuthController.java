package com.ff.clients_service.controller;

import com.ff.clients_service.dto.AuthRequest;
import com.ff.clients_service.dto.AuthResponse;
import com.ff.clients_service.dto.RegisterRequest;
import com.ff.clients_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) throws Exception {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request){
        return authService.authenticate(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam String refreshToken){
        return authService.refreshToken(refreshToken);
    }
}
