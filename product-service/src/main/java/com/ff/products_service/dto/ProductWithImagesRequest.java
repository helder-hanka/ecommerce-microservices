package com.ff.products_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Requête pour créer un nouveau produit avec ses images associées.")
public class ProductWithImagesRequest {

    @NotBlank(message = "Le Nom est obligatoire")
    @Schema(description = "Nom du produit", example = "Ordinateur Portable Ultra")
    private String name;

    @NotBlank(message = "La déscription est obligatoire")
    @Schema(description = "Description détaillée du produit", example = "Un ordinateur portable léger et puissant, idéal pour le travail et le divertissement.")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Schema(description = "Prix unitaire du produit", example = "1200.00")
    private BigDecimal price;

    @NotNull(message = "Le stock est obligatoire")
    @Schema(description = "Quantité en stock disponible", example = "50")
    private int stock;

    @Valid
    @Schema(description = "Liste des images associées à ce produit. Doit contenir exactement une image principale (main: true) non supprimée.")
    private List<ImageRequest> images;

    @Data
    @Schema(description = "Représente une image de produit à ajouter.")
    @EqualsAndHashCode(callSuper = true)
    public static class ImageRequest extends AbstractImageRequest {
        @NotBlank
        @Schema(description = "URL de l'image", example = "https://example.com/images/laptop-main.jpg")
        private String url;
        @NotBlank
        @Schema(description = "Titre de l'image", example = "Vue de face de l'ordinateur portable")
        private String title;
    }
}