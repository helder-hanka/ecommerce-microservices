package com.ff.products_service.dto;

import com.ff.products_service.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Réponse contenant une liste de produits")
public class ProductListResponse {
    @Schema(description = "Message de la réponse", example = "Produits trouvés")
    private String message;

    @Schema(description = "Code HTTP", example = "200")
    private int statusCode;

    @Schema(description = "Horodatage", example = "2025-06-11T17:34:00")
    private LocalDateTime timestamp;

    @Schema(description = "Liste des produits")
    private List<Product> data;
}