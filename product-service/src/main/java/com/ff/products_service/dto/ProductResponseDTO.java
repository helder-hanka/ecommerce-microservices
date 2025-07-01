package com.ff.products_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Représente les détails complets d'un produit dans une réponse.")
public class ProductResponseDTO {
@Schema(description = "ID unique du produit", example = "1")
    private Long id;
    @Schema(description = "Nom du produit", example = "Chaise Ergonomique")
    private String name;
    @Schema(description = "Description du produit", example = "Une chaise confortable pour le bureau.")
    private String description;
    @Schema(description = "Prix du produit", example = "250.00")
    private BigDecimal price;
    @Schema(description = "Quantité en stock", example = "100")
    private Integer stock;
    @Schema(description = "Liste des images associées au produit")
    private List<ImageResponseDTO> images;
}