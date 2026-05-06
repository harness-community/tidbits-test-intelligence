package com.example.ecommerce.service;

import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Widget")
                .price(new BigDecimal("19.99"))
                .stockQuantity(50)
                .category("Tools")
                .sku("SKU-WID-001")
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("createProduct() Tests")
    class CreateProductTests {
        @Test
        void createProduct_noSkuConflict_savesAndReturns() {
            when(productRepository.save(any())).thenReturn(sampleProduct);
            Product created = productService.createProduct(sampleProduct);
            assertThat(created).isEqualTo(sampleProduct);
            verify(productRepository).save(sampleProduct);
        }

        @Test
        void createProduct_withExistingSku_throwsException() {
            when(productRepository.findBySku("SKU-WID-001")).thenReturn(Optional.of(sampleProduct));
            assertThatThrownBy(() -> productService.createProduct(sampleProduct))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SKU");
        }

        @Test
        void createProduct_withNullSku_doesNotCheckSku() {
            sampleProduct.setSku(null);
            when(productRepository.save(any())).thenReturn(sampleProduct);
            productService.createProduct(sampleProduct);
            verify(productRepository, never()).findBySku(any());
        }
    }

    @Nested
    @DisplayName("getProductById() Tests")
    class GetProductByIdTests {
        @Test
        void getProductById_existingId_returnsProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            assertThat(productService.getProductById(1L)).isEqualTo(sampleProduct);
        }

        @Test
        void getProductById_nonExistentId_throwsNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.getProductById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllActiveProducts() Tests")
    class GetAllActiveProductsTests {
        @Test
        void getAllActiveProducts_returnsOnlyActive() {
            List<Product> activeProducts = List.of(sampleProduct);
            when(productRepository.findByActiveTrue()).thenReturn(activeProducts);
            assertThat(productService.getAllActiveProducts()).hasSize(1);
        }

        @Test
        void getAllActiveProducts_whenNone_returnsEmpty() {
            when(productRepository.findByActiveTrue()).thenReturn(List.of());
            assertThat(productService.getAllActiveProducts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchProducts() Tests")
    class SearchProductsTests {
        @Test
        void searchProducts_withKeyword_searchesByKeyword() {
            when(productRepository.searchByKeyword("widget")).thenReturn(List.of(sampleProduct));
            List<Product> results = productService.searchProducts("widget");
            assertThat(results).hasSize(1);
            verify(productRepository).searchByKeyword("widget");
        }

        @Test
        void searchProducts_withBlankKeyword_returnsAllActive() {
            when(productRepository.findByActiveTrue()).thenReturn(List.of(sampleProduct));
            productService.searchProducts("  ");
            verify(productRepository).findByActiveTrue();
        }

        @Test
        void searchProducts_withNullKeyword_returnsAllActive() {
            when(productRepository.findByActiveTrue()).thenReturn(List.of());
            productService.searchProducts(null);
            verify(productRepository).findByActiveTrue();
        }

        @Test
        void searchProducts_trimsKeyword() {
            when(productRepository.searchByKeyword("widget")).thenReturn(List.of());
            productService.searchProducts("  widget  ");
            verify(productRepository).searchByKeyword("widget");
        }
    }

    @Nested
    @DisplayName("getProductsByPriceRange() Tests")
    class PriceRangeTests {
        @Test
        void getProductsByPriceRange_validRange_returnsList() {
            when(productRepository.findByPriceBetween(any(), any())).thenReturn(List.of(sampleProduct));
            List<Product> products = productService.getProductsByPriceRange(
                    new BigDecimal("10"), new BigDecimal("50"));
            assertThat(products).hasSize(1);
        }

        @Test
        void getProductsByPriceRange_minGreaterThanMax_throwsException() {
            assertThatThrownBy(() -> productService.getProductsByPriceRange(
                    new BigDecimal("100"), new BigDecimal("10")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void getProductsByPriceRange_minEqualsMax_allowed() {
            when(productRepository.findByPriceBetween(any(), any())).thenReturn(List.of());
            assertThatNoException().isThrownBy(() ->
                    productService.getProductsByPriceRange(
                            new BigDecimal("10"), new BigDecimal("10")));
        }
    }

    @Nested
    @DisplayName("updateStock() Tests")
    class UpdateStockTests {
        @Test
        void updateStock_withValidQuantity_updatesProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);
            productService.updateStock(1L, 100);
            assertThat(sampleProduct.getStockQuantity()).isEqualTo(100);
        }

        @Test
        void updateStock_withNegativeQuantity_throwsException() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            assertThatThrownBy(() -> productService.updateStock(1L, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void updateStock_withZeroQuantity_setsToZero() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);
            productService.updateStock(1L, 0);
            assertThat(sampleProduct.getStockQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("deactivateProduct() Tests")
    class DeactivateProductTests {
        @Test
        void deactivateProduct_setsActiveFalse() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);
            productService.deactivateProduct(1L);
            assertThat(sampleProduct.isActive()).isFalse();
        }

        @Test
        void deactivateProduct_nonExistentId_throwsNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.deactivateProduct(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
