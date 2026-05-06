package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByActiveTrue();
    List<Product> findByCategoryAndActiveTrue(String category);
    Optional<Product> findBySku(String sku);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByStockQuantityLessThan(int threshold);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
}
