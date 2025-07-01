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
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable=false, length = 20)
    private String username;
    @Column(unique = true, nullable=false, length = 20)
    private String firstName;
    @Column(unique = true, nullable=false, length = 20)
    private String lastName;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
