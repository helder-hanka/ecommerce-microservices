package com.ff.commandes_service.exception;

import lombok.*;

import java.time.LocalDateTime;



@Getter
public class ErrorResponse {
    // Ajoutez les getters pour tous les champs
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String timestamp;

    // Constructeur(s), getters et setters
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now().toString(); // Pour un format simple
    }

}
