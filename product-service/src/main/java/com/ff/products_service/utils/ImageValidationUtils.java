package com.ff.products_service.utils;

import com.ff.products_service.dto.AbstractImageRequest;

import java.util.List;

public class ImageValidationUtils {

    public static void validateSingleMainImage(List<? extends AbstractImageRequest> images) {
        long mainImageCount = images.stream()
                .filter(img -> !Boolean.TRUE.equals(img.getToDelete()))
                .filter(img -> Boolean.TRUE.equals(img.getMain()))
                .count();
        if (mainImageCount == 0) {
            throw new IllegalArgumentException("Il doit y avoir une image principale.");
        }

        if (mainImageCount > 1) {
            throw new IllegalArgumentException("Une seule image peut être marquée comme principale.");
        }
    }

    public static void validateNoMainImageBeingDeleted(List<? extends AbstractImageRequest> images) {
        boolean isTryingToDeleteMainImage = images.stream()
                .anyMatch(img -> Boolean.TRUE.equals(img.getToDelete()) && Boolean.TRUE.equals(img.getMain()));

        if (isTryingToDeleteMainImage) {
            throw new IllegalArgumentException("Une image principale ne peut pas être supprimée directement. Veuillez d’abord en définir une autre comme principale.");
        }
    }
}
