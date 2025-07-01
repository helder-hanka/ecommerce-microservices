package com.ff.clients_service.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String username;
    private String firstName;
    private String lastName;
}
