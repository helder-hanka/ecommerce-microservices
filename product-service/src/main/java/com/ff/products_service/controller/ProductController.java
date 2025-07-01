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
@RequestMapping("/api/products")
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

    @GetMapping("/liste/{id}")
    @Operation(summary = "Récupérer un produit par ID", description = "Récupère les détails d'un produit spécifique en utilisant son identifiant unique.")
    public ResponseEntity<ApiRes<Product>> getProductById(@Parameter(name = "id", description = "ID unique du produit à récupérer", example = "1", required = true ) @PathVariable Long id) {

        Product product = productService.findById(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product not found");
        }
        return ResponseEntity.ok(ResponseBuilder.success("Product found", product));
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Ajouter un produit et ses images", description = "Crée un nouveau produit dans le système et lui associe des images.")
    public ResponseEntity<ApiRes<ProductResponseDTO>> createProductWithImageUrls(@Valid @RequestBody ProductWithImagesRequest request) {

        // 1. Valider qu’il y a exactement une image principale
        ImageValidationUtils.validateSingleMainImage(request.getImages());

        // 1. Créer le produit
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();

        product = productService.create(product); // save initial product

        // 2. Ajouter les images
        if (request.getImages() != null) {
            for (ProductWithImagesRequest.ImageRequest imageReq : request.getImages()) {
                Image image = Image.builder()
                        .url(imageReq.getUrl())
                        .title(imageReq.getTitle())
                        .main(imageReq.getMain())
                        .product(product)
                        .build();
                imageService.createImage(image);
            }
        }
        List<Image> images = imageService.getImagesByProductId(product.getId());
        product.setImages(images);

        ProductResponseDTO productDTO = productMapper.toProductResponseDTO(product);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseBuilder.created("Product has been successfully save.", productDTO));
    }

    @PutMapping("/modif/{id}")
    @Transactional
    @Operation(summary = "Modifier un produit par ID et ses images", description = "Met à jour les informations d'un produit existant et gère ses images (ajout, modification, suppression).")
    public ResponseEntity<ApiRes<Product>> updateProduct(@Parameter(description = "ID unique du produit à modifier", example = "1") @PathVariable Long id, @Valid @RequestBody UpdateProductWithImagesRequest request) {
    Product product = productService.findById(id);
    if (product == null) {
        throw new ResourceNotFoundException("Product not found with id " + id);
    }

        // 1. Valider qu’il y a exactement une image principale
        ImageValidationUtils.validateSingleMainImage(request.getImages());
        // 2. Vérifier qu’on ne supprime pas une image principale
        ImageValidationUtils.validateNoMainImageBeingDeleted(request.getImages());

    // Mise à jour des champs principaux
    product.setName(request.getName());
    product.setDescription(request.getDescription());
    product.setPrice(request.getPrice());
    product.setStock(request.getStock());
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

        List<Image> images = imageService.getImagesByProductId(id);

        if (images == null || images.isEmpty()) {
            throw new ResourceNotFoundException("Image not found with id " + id);
        }
        imageService.deleteImageAllByProductId(id);
        productService.delete(id);

        return ResponseEntity.ok(ResponseBuilder.success("Product has been successfully deleted",null ));
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
}
