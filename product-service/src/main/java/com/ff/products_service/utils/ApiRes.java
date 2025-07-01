package com.ff.products_service.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Format de réponse standard pour toutes les API.")
public class ApiRes<T> {
@Schema(description = "Message descriptif de la réponse", example = "Products found")
    private String message;
    @Schema(description = "Code de statut HTTP de la réponse", example = "200")
    private int status;
    @Schema(description = "Horodatage de la réponse", example = "2023-10-27T10:30:00")
    private LocalDateTime timestamp;
    @Schema(description = "Données de la réponse (peut être null en cas d'erreur ou de succès sans données spécifiques)")
    private T data;
}