package com.ff.products_service.service;

import com.ff.products_service.entity.Product;
import com.ff.products_service.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> findAll() {
        return productRepo.findAll();
    }

    public Product findById(Long id) {
        return productRepo.findById(id).orElse(null);
    }

    public Product create(Product product) {
        return productRepo.save(product);
    }

    public Product update(Long id, Product product) {
        Product oldProduct = productRepo.findById(id).orElse(null);
        if (oldProduct == null) return null;
        oldProduct.setName(product.getName());
        oldProduct.setDescription(product.getDescription());
        oldProduct.setPrice(product.getPrice());
        oldProduct.setStock(product.getStock());
        return productRepo.save(oldProduct);
    }

    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    public int getStockByProductId(Long productId) {
        Product product = productRepo.findById(productId).orElse(null);
        return product != null ? product.getStock() : -1;
    }
}