package com.ff.clients_service.dto;

import com.ff.clients_service.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private UserRole role;
}
