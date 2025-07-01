package com.ff.products_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Représente les détails d'une image de produit dans une réponse.")
public class ImageResponseDTO {

    @Schema(description = "ID unique de l'image", example = "1")
    private Long id;
    @Schema(description = "URL de l'image", example = "https://example.com/image_response.jpg")
    private String url;
    @Schema(description = "Titre descriptif de l'image", example = "Image du produit 1")
    private String title;
    @Schema(description = "Indique si c'est l'image principale du produit", example = "true")
    private boolean main;
}