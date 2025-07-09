package com.ff.products_service.controller;

import com.ff.products_service.dto.ProductWithImagesRequest;
import com.ff.products_service.dto.UpdateProductWithImagesRequest;
import com.ff.products_service.entity.Image;
import com.ff.products_service.entity.Product;
import com.ff.products_service.security.JwtService;
import com.ff.products_service.service.ImageService;
import com.ff.products_service.service.ProductService;
import com.ff.products_service.utils.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products/admin")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;
    private final ImageService imageService;
    private final ProductMapper productMapper;
    private final JwtService jwtService;

    @PostMapping
    @Transactional
    @Operation(summary = "Ajouter un produit et ses images", description = "Crée un nouveau produit dans le système et lui associe des images.")
    public ResponseEntity<ApiRes<Product>>createProductWithImageUrls(@Valid @RequestBody ProductWithImagesRequest request, HttpServletRequest servletRequest) {
        Long adminId = getAdminId(servletRequest);

        // 1. Valider qu’il y a exactement une image principale
        ImageValidationUtils.validateSingleMainImage(request.getImages());

        // 1. Créer le produit
        Product product = Product.builder()
                .adminId(adminId)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .createdAt(LocalDateTime.now())
                .build();

        product = productService.create(product);
        // 2. Ajouter les images
        if (request.getImages() == null) {
            throw new IllegalArgumentException("Images cannot be null");
        }
            for (ProductWithImagesRequest.ImageRequest imageReq : request.getImages()) {
                Image image = Image.builder()
                        .url(imageReq.getUrl())
                        .title(imageReq.getTitle())
                        .main(imageReq.getMain())
                        .product(product)
                        .build();
                imageService.createImage(image);
            }
        List<Image> images = imageService.getImagesByProductId(product.getId());
        product.setImages(images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseBuilder.success("Product has been successfully saved", product));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Modifier un produit par ID et ses images", description = "Met à jour les informations d'un produit existant et gère ses images (ajout, modification, suppression).")
    public ResponseEntity<ApiRes<Product>> updateProduct(@Parameter(description = "ID unique du produit à modifier", example = "1") @PathVariable Long id, @Valid @RequestBody UpdateProductWithImagesRequest request, HttpServletRequest servletRequest) {
        Product product = productService.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }

        // Vérifier si le produit correspond à l'adminId
        isProductAdmin(id, servletRequest, product);

        // 1. Valider qu’il y a exactement une image principale
        ImageValidationUtils.validateSingleMainImage(request.getImages());
        // 2. Vérifier qu’on ne supprime pas une image principale
        ImageValidationUtils.validateNoMainImageBeingDeleted(request.getImages());

        // Mise à jour des champs principaux
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setUpdatedAt(LocalDateTime.now());
        productService.create(product);

        // Gestion des images
        for (UpdateProductWithImagesRequest.ImageRequest imageReq : request.getImages()) {
            Long imageId = imageReq.getId();

            if (imageReq.getToDelete()) {            if (imageId != null) {
                Image existingImage = imageService.findImageById(imageId);
                if (existingImage == null) {
                    throw new ResourceNotFoundException("Image not found with id " + imageId);
                }
                imageService.deleteImageById(imageId);
            }
                continue;
            }

            if (imageId == null) {
                Image newImage = Image.builder()
                        .url(imageReq.getUrl())
                        .title(imageReq.getTitle())
                        .main(imageReq.getMain())
                        .product(product)
                        .build();
                imageService.createImage(newImage);
            } else {
                Image existingImage = imageService.findImageById(imageId);
                if (existingImage == null) {
                    throw new ResourceNotFoundException("Image not found with id " + imageId);
                }
                existingImage.setUrl(imageReq.getUrl());
                existingImage.setTitle(imageReq.getTitle());
                existingImage.setMain(imageReq.getMain());
                imageService.createImage(existingImage);
            }
        }

        Product updatedProduct = productService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(ResponseBuilder.success("Product has been successfully modified", updatedProduct));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Supprimer un produit par ID et ses images", description = "Supprime un produit et toutes les images associées de manière permanente.")
    public ResponseEntity<ApiRes<String>> deleteProduct(@Parameter(description = "ID unique du produit à supprimer", example = "1") @PathVariable Long id) {
        Product product = productService.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id " + id);
        }
        // Vérifier si le produit correspond à l'adminId
        Long adminId = product.getAdminId();
        if (adminId == null) {
            throw new ResourceNotFoundException("Admin ID not found for product with id " + id);
        }

        // Vérifier si le produit a des images associées
        // Si oui, les supprimer
        // Si non, continuer la suppression du produit
        // Récupérer les images associées au produit
        if (product.getImages() == null || product.getImages().isEmpty()) {
            throw new ResourceNotFoundException("No images found for product with id " + id);
        }
        // Supprimer toutes les images associées au produit
        // Si aucune image n'est trouvée, lancer une exception

        List<Image> images = imageService.getImagesByProductId(id);

        if (images == null || images.isEmpty()) {
            throw new ResourceNotFoundException("Image not found with id " + id);
        }
        imageService.deleteImageAllByProductId(id);
        productService.delete(id);

        return ResponseEntity.ok(ResponseBuilder.success("Product has been successfully deleted",null ));
    }

    private Long getAdminId(HttpServletRequest servletRequest) {
        // Vérifier le token dans l'en-tête Authorization
        if (servletRequest == null) {
            throw new IllegalArgumentException("ServletRequest cannot be null");
        }
        // Extraire le token de l'en-tête Authorization
        String authHeader = servletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResourceNotFoundException("Token manquant ou invalide");
        }
        String token = authHeader.substring(7);
        Long adminId = jwtService.extractUserId(token);
        if (adminId == null) {
            throw new ResourceNotFoundException("Admin ID not found in token");
        }
        return adminId;
    }

    private void isProductAdmin(Long id, HttpServletRequest servletRequest, Product product) {
        Long adminIdProduct = product.getAdminId();

        // Vérifier si l'adminId du produit correspond à l'adminId du token
        Long tokenAdminId = getAdminId(servletRequest);
        if (!adminIdProduct.equals(tokenAdminId)) {
            throw new ResourceNotFoundException("Admin ID does not match for product with id " + id);
        }
    }
    @GetMapping
    @Operation(summary = "Lister tous les produits par admin ID", description = "Récupère une liste de tous les produits créés par l'admin connecté.")
    public ResponseEntity<ApiRes<List<Product>>> getProductsByAdminId(HttpServletRequest servletRequest) {
        Long adminId = getAdminId(servletRequest);
        List<Product> products = productService.findByAdminId(adminId);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("No products found for admin with id " + adminId);
        }
        return ResponseEntity.ok(ResponseBuilder.success("Products found", products));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un produit par ID et admin ID", description = "Récupère les détails d'un produit spécifique créé par l'admin connecté en utilisant son identifiant unique.")
    public ResponseEntity<ApiRes<Product>> getProductById(@Parameter(name = "id", description = "ID unique du produit à récupérer", example = "1", required = true ) @PathVariable Long id, HttpServletRequest servletRequest) {
        Long adminId = getAdminId(servletRequest);
        Product product = productService.findByAdminIdAndProductId(adminId, id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found with id " + id + " for admin with id " + adminId);
        }
        return ResponseEntity.ok(ResponseBuilder.success("Product found", product));
    }

}
