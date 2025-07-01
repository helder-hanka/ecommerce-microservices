package com.ff.clients_service.dto;

import com.ff.clients_service.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "L'email ne peut pas être vide.")
    private String email;
    @NotBlank(message = "Le mot de passe ne peut pas être vide.")
    private String password;
    @NotNull(message = "Le rôle ne peut pas être vide.")
    private UserRole role;
}
