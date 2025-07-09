package com.ff.clients_service.service;

import com.ff.clients_service.dto.*;
import com.ff.clients_service.entity.User;
import com.ff.clients_service.repository.UserRepository;
import com.ff.clients_service.security.JwtService;
import com.ff.clients_service.utils.MessageResponse;
import com.ff.clients_service.utils.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public MessageResponse register(RegisterRequest request) {
        var userEmail = userRepository.findByEmail(request.getEmail()).isPresent();
     if (userEmail){
         throw new ResourceNotFoundException("Email is already taken!");
     }
        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        return new MessageResponse("User registered successfully!");
    }

    public AuthResponse authenticate(AuthRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateToken(user);

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);
        var user = userRepository.findByEmail(email)
                .orElseThrow();

        if (!refreshToken.equals(user.getRefreshToken()) || !jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        var newAccessToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .userId(user.getId())
                .role(user.getRole())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
