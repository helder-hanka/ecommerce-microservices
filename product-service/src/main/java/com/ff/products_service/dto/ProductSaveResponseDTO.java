package com.ff.products_service.dto;

import com.ff.products_service.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Réponse standard pour la création d'un produit, incluant un message et les données du produit.") // Add description
public class ProductSaveResponseDTO {
@Schema(description = "Message d'information sur l'opération", example = "Product has been successfully save.")
    private String message;
    @Schema(description = "Les données du produit qui vient d'être créé ou modifié.")
    private ProductResponseDTO data;
}