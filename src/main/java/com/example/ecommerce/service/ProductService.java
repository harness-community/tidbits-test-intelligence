package com.example.ecommerce.service;

import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        if (product.getSku() != null && productRepository.findBySku(product.getSku()).isPresent()) {
            throw new IllegalArgumentException("Product with SKU already exists: " + product.getSku());
        }
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category);
    }

    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllActiveProducts();
        }
        return productRepository.searchByKeyword(keyword.trim());
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }
        return productRepository.findByPriceBetween(min, max);
    }

    public Product updateProduct(Long id, Product updated) {
        Product existing = getProductById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());
        return productRepository.save(existing);
    }

    public Product updateStock(Long id, int quantity) {
        Product product = getProductById(id);
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        product.setStockQuantity(quantity);
        return productRepository.save(product);
    }

    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThan(threshold);
    }
}
