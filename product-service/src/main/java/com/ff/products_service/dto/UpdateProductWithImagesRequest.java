package com.ff.products_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "Requête pour modifier un produit existant et ses images.")
public class UpdateProductWithImagesRequest {
    @NotNull(message = "Le Id Produit est obligatoire")
    @Schema(description = "ID unique du produit à modifier", example = "123")
    private Long id;

    @NotBlank(message = "Le Nom est obligatoire")
    @Schema(description = "Nouveau nom du produit", example = "Ordinateur Portable Pro Max")
    private String name;

    @NotBlank(message = "La déscription est obligatoire")
    @Schema(description = "Nouvelle description détaillée du produit", example = "Une version améliorée avec plus de mémoire et un processeur plus rapide.")
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @Schema(description = "Nouveau prix unitaire du produit", example = "1500.00")
    private BigDecimal price;

    @NotNull(message = "Le stock est obligatoire")
    @Schema(description = "Nouvelle quantité en stock", example = "75")
    private int stock;

    @Valid
    @NotEmpty(message = "La liste d'images ne peut être vide")
    @Schema(description = "Liste des images associées au produit. Permet d'ajouter (id=null), modifier (id existant) ou supprimer (toDelete=true) des images. Doit toujours contenir une image principale non supprimée.")
    private List<UpdateProductWithImagesRequest.ImageRequest> images;

    @Data
    @Schema(description = "Représente une image de produit à modifier ou supprimer.")
    @EqualsAndHashCode(callSuper = true)
    public static class ImageRequest extends AbstractImageRequest {
        @Schema(description = "ID de l'image. Null pour une nouvelle image, requis pour modifier ou supprimer une image existante.", example = "456")
        private Long id;

        @NotBlank(message = "L'URL est obligatoire")
        @Schema(description = "URL de l'image", example = "https://example.com/images/laptop-new-main.jpg")
        private String url;

        @NotBlank(message = "Le Titre est obligatoire")
        @Schema(description = "Titre descriptif de l'image", example = "Vue latérale de l'ordinateur portable")
        private String title;

        @NotNull(message = "Le Main est obligatoire")
        @Schema(description = "Indique si cette image est l'image principale du produit. Requise pour toutes les images.", example = "true")
        private Boolean main;

        @NotNull(message = "Le boolean delete est obligatoire")
        @Schema(description = "Définit si l'image doit être supprimée. Si 'true', l'URL et le titre ne sont pas pertinents. Une image principale ne peut pas être marquée pour suppression directe.", example = "false")
        private Boolean toDelete;
    }
}