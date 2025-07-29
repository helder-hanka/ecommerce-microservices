package com.ff.commandes_service.service;

import com.ff.commandes_service.dto.ApiResponse;
import com.ff.commandes_service.dto.ProductStockResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class ProductServiceClient {
    private final WebClient webClient;


    public ProductServiceClient(@Value("${products-service.url}") String productServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(productServiceUrl)
                .build();
        log.info("ProductServiceClient initialized with base URL: {}", productServiceUrl);
    }
    /**
     * Vérifie le stock d'un produit spécifique.
     * @param productId L'ID du produit.
     * @return Le stock disponible du produit.
     * @throws RuntimeException si le produit n'est pas trouvé ou s'il y a une erreur de communication.
     */
    public Integer checkProductStock(Long productId) {
        log.info("1: Checking stock for product ID: {}", productId);
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        try {
            /*Integer stock = webClient.get()
                    .uri("/api/public/products/{id}/stock", productId)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .block();*/
             ApiResponse<ProductStockResponse> response = webClient.get()
                    .uri("/api/public/products/{id}/stock", productId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ProductStockResponse>>() {})
                    .block();
            log.info("2: Stock for product ID {}: {}", productId, response);
            if (response == null || response.getData() == null) {
                log.warn("3: Product with ID {} not found", productId);
                throw new RuntimeException("Product not found");
            }
            log.info("4: Stock check successful for product ID: {}", productId);
            return response.getData().getStock();
        } catch (Exception e) {
            log.error("4: Error checking stock for product ID {}: {}", productId, e.getMessage());
            throw new RuntimeException("Error checking product stock", e);
        }
    }
    /**
     * Décrémente le stock d'un produit après une commande réussie.
     * @param productId L'ID du produit.
     * @param quantity La quantité à décrémenter.
     * @throws RuntimeException en cas d'erreur de communication ou de mise à jour.
     */
    public void decrementProductStock(Long productId, int quantity) {
        log.info("Decrementing stock for product ID: {}, quantity: {}", productId, quantity);
        if (productId == null || quantity <= 0) {
            throw new IllegalArgumentException("Product ID cannot be null and quantity must be greater than zero");
        }
        try {
            webClient.put()
                    .uri("/api/public/products/{id}/stock", productId)
                    .bodyValue(quantity)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            log.info("Stock decremented successfully for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error decrementing stock for product ID {}: {}", productId, e.getMessage());
            throw new RuntimeException("Error decrementing product stock", e);
        }// Je suis à la méthode pour mettre à jour le stock d'un produit
    }
}
