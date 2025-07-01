package com.ff.clients_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable=false)
    private String email;
    @Column(unique = true, nullable=false)
    private String password;
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String refreshToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}