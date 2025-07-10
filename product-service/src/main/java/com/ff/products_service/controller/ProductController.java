package com.ff.products_service.controller;

import com.ff.products_service.dto.*;
import com.ff.products_service.entity.Image;
import com.ff.products_service.entity.Product;
import com.ff.products_service.service.ImageService;
import com.ff.products_service.service.ProductService;
import com.ff.products_service.utils.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
@Tag(name="Produits", description = "Opérations sur les produits")
public class ProductController {

    private final ProductService productService;
    private final ImageService imageService;
    private final ProductMapper productMapper;

    @GetMapping
    @Operation(summary = "Lister tous les produits", description = "Récupère une liste de tous les produits disponibles dans le système.")
    public ResponseEntity<ApiRes<List<Product>>> getAllProducts() {
        List<Product> products = productService.findAll();
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        return ResponseEntity.ok(ResponseBuilder.success("Products found", products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID", description = "Récupère les détails d'un produit spécifique en utilisant son identifiant unique.")
    public ResponseEntity<ApiRes<Product>> getProductById(@Parameter(name = "id", description = "ID unique du produit à récupérer", example = "1", required = true ) @PathVariable Long id) {

        Product product = productService.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found");
        }
        return ResponseEntity.ok(ResponseBuilder.success("Product found", product));
    }

    @GetMapping("/{id}/stock")
    @Operation(summary = "Récupérer le nombre de stock du produit par ID", description = "Récupère la quantité de stock disponible pour un produit spécifique.")
    public ResponseEntity<?> getProductStock(@Parameter(description = "ID unique du produit dont on veut récupérer le stock", example = "1") @PathVariable Long id) {

       Product product = productService.findById(id);
       if (product == null) {
           throw new ResourceNotFoundException("Product not found with id " + id);
       }
       return ResponseEntity.ok(ResponseBuilder.success("Product stock", product.getStock()));
    }

    // Update stock by product id
    @PutMapping("/{id}/stock")
    @Operation(summary = "Mettre à jour le stock d'un produit", description = "Met à jour la quantité de stock disponible pour un produit spécifique.")
    public ResponseEntity<ApiRes<Product>> updateProductStock(
            @Parameter(description = "ID unique du produit dont on veut mettre à jour le stock", example = "1") @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest stockUpdateRequest) {

        Product product = productService.updateStock(id, stockUpdateRequest.getQuantity());
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
        return ResponseEntity.ok(ResponseBuilder.success("Product stock updated", product));
    }
}
