package com.santandertecnologia.capabilitiestesting.application.service;

import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.PRODUCT_ID;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.PRODUCT_ID_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.port.out.CacheService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ProductRepository;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para ProductService. Demuestra el uso de Mockito, AssertJ, principios FIRST,
 * patrón AAA, nested tests y tests parametrizados. Usa MockUtils y TestConstants para datos de
 * prueba consistentes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

  private final Product testProduct = MockUtils.mockProduct(PRODUCT_ID);
  @Mock private ProductRepository productRepository;
  @Mock private CacheService cacheService;
  @InjectMocks private ProductService productService;

  @Nested
  @DisplayName("Product Creation Tests")
  class ProductCreationTests {

    @Test
    @DisplayName("Should create product successfully with valid data")
    void shouldCreateProductSuccessfullyWithValidData() {
      // Arrange
      when(productRepository.existsBySku(any())).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(testProduct);

      // Act
      final Product result = productService.createProduct(testProduct);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(PRODUCT_ID);
      assertThat(result.isActive()).isTrue();
      assertThat(result.getStock()).isEqualTo(10);

      verify(productRepository).existsBySku(any());
      verify(productRepository).save(any(Product.class));
      verify(cacheService).put(any(String.class), any(Product.class), anyLong());
    }

    @Test
    @DisplayName("Should throw exception when SKU already exists")
    void shouldThrowExceptionWhenSkuAlreadyExists() {
      // Arrange
      when(productRepository.existsBySku(any())).thenReturn(true);

      // Act & Assert
      assertThatThrownBy(() -> productService.createProduct(testProduct))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("SKU already exists");

      verify(productRepository).existsBySku(any());
      verify(productRepository, never()).save(any(Product.class));
      verify(cacheService, never()).put(any(), any(), anyLong());
    }

    @ParameterizedTest
    @EnumSource(Product.Category.class)
    @DisplayName("Should create product with all valid categories")
    void shouldCreateProductWithAllValidCategories(final Product.Category category) {
      // Arrange
      final Product productWithCategory = MockUtils.mockProduct(category);
      when(productRepository.existsBySku(any())).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(productWithCategory);

      // Act
      final Product result = productService.createProduct(productWithCategory);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getCategory()).isEqualTo(category);

      verify(productRepository).save(any(Product.class));
    }
  }

  @Nested
  @DisplayName("Product Retrieval Tests")
  class ProductRetrievalTests {

    @Test
    @DisplayName("Should get product by ID from repository when not in cache")
    void shouldGetProductByIdFromRepositoryWhenNotInCache() {
      // Arrange
      when(cacheService.get(any(String.class), eq(Product.class))).thenReturn(Optional.empty());
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

      // Act
      final Optional<Product> result = productService.getProductById(PRODUCT_ID);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(PRODUCT_ID);

      verify(cacheService).get(any(String.class), eq(Product.class));
      verify(productRepository).findById(PRODUCT_ID);
      verify(cacheService).put(any(String.class), any(Product.class), anyLong());
    }

    @Test
    @DisplayName("Should get product by ID from cache when available")
    void shouldGetProductByIdFromCacheWhenAvailable() {
      // Arrange
      when(cacheService.get(any(String.class), eq(Product.class)))
          .thenReturn(Optional.of(testProduct));

      // Act
      final Optional<Product> result = productService.getProductById(PRODUCT_ID);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(PRODUCT_ID);

      verify(cacheService).get(any(String.class), eq(Product.class));
      verify(productRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should return empty when product not found")
    void shouldReturnEmptyWhenProductNotFound() {
      // Arrange
      when(cacheService.get(any(String.class), eq(Product.class))).thenReturn(Optional.empty());
      when(productRepository.findById(PRODUCT_ID_2)).thenReturn(Optional.empty());

      // Act
      final Optional<Product> result = productService.getProductById(PRODUCT_ID_2);

      // Assert
      assertThat(result).isEmpty();

      verify(cacheService).get(any(String.class), eq(Product.class));
      verify(productRepository).findById(PRODUCT_ID_2);
    }

    @Test
    @DisplayName("Should get products by category")
    void shouldGetProductsByCategory() {
      // Arrange
      final Product electronics1 = MockUtils.mockProduct(Product.Category.ELECTRONICS);
      final Product electronics2 = MockUtils.mockProduct(Product.Category.ELECTRONICS);
      final List<Product> expectedProducts = Arrays.asList(electronics1, electronics2);

      when(productRepository.findByCategory(Product.Category.ELECTRONICS))
          .thenReturn(expectedProducts);

      // Act
      final List<Product> result =
          productService.getProductsByCategory(Product.Category.ELECTRONICS);

      // Assert
      assertThat(result).hasSize(2).allMatch(p -> p.getCategory() == Product.Category.ELECTRONICS);

      verify(productRepository).findByCategory(Product.Category.ELECTRONICS);
    }
  }

  @Nested
  @DisplayName("Product Stock Management Tests")
  class ProductStockManagementTests {

    @Test
    @DisplayName("Should update product stock successfully")
    void shouldUpdateProductStockSuccessfully() {
      // Arrange
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
      when(productRepository.save(any(Product.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      final Optional<Product> result = productService.updateProductStock(PRODUCT_ID, 50);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getStock()).isEqualTo(50);
      assertThat(result.get().isActive()).isTrue();

      verify(productRepository).findById(PRODUCT_ID);
      verify(productRepository).save(any(Product.class));
      verify(cacheService).put(any(String.class), any(Product.class), anyLong());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 5, 10, 100})
    @DisplayName("Should update stock with different values")
    void shouldUpdateStockWithDifferentValues(final int newStock) {
      // Arrange
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
      when(productRepository.save(any(Product.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      final Optional<Product> result = productService.updateProductStock(PRODUCT_ID, newStock);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getStock()).isEqualTo(newStock);
      assertThat(result.get().isActive()).isEqualTo(newStock > 0);
    }

    @Test
    @DisplayName("Should mark product as inactive when stock is zero")
    void shouldMarkProductAsInactiveWhenStockIsZero() {
      // Arrange
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
      when(productRepository.save(any(Product.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // Act
      final Optional<Product> result = productService.updateProductStock(PRODUCT_ID, 0);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getStock()).isZero();
      assertThat(result.get().isActive()).isFalse();

      verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return empty when updating stock of non-existent product")
    void shouldReturnEmptyWhenUpdatingStockOfNonExistentProduct() {
      // Arrange
      when(productRepository.findById(PRODUCT_ID_2)).thenReturn(Optional.empty());

      // Act
      final Optional<Product> result = productService.updateProductStock(PRODUCT_ID_2, 50);

      // Assert
      assertThat(result).isEmpty();

      verify(productRepository).findById(PRODUCT_ID_2);
      verify(productRepository, never()).save(any(Product.class));
      verify(cacheService, never()).put(any(), any(), anyLong());
    }
  }

  @Nested
  @DisplayName("Product Deletion Tests")
  class ProductDeletionTests {

    @Test
    @DisplayName("Should delete product and evict from cache")
    void shouldDeleteProductAndEvictFromCache() {
      // Arrange
      when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

      // Act
      productService.deleteProduct(PRODUCT_ID);

      // Assert
      verify(productRepository).deleteById(PRODUCT_ID);
      verify(cacheService).evict("product:" + PRODUCT_ID);
    }
  }

  @Nested
  @DisplayName("Product Search Tests")
  class ProductSearchTests {

    @Test
    @DisplayName("Should get all active products")
    void shouldGetAllActiveProducts() {
      // Arrange
      final Product activeProduct1 = MockUtils.mockProduct(true);
      final Product activeProduct2 = MockUtils.mockProduct(true);
      final List<Product> expectedProducts = Arrays.asList(activeProduct1, activeProduct2);

      when(productRepository.findActiveProducts()).thenReturn(expectedProducts);

      // Act
      final List<Product> result = productService.getActiveProducts();

      // Assert
      assertThat(result).hasSize(2);
      assertThat(result).allMatch(Product::isActive);

      verify(productRepository).findActiveProducts();
    }

    @Test
    @DisplayName("Should return empty list when no active products")
    void shouldReturnEmptyListWhenNoActiveProducts() {
      // Arrange
      when(productRepository.findActiveProducts()).thenReturn(List.of());

      // Act
      final List<Product> result = productService.getActiveProducts();

      // Assert
      assertThat(result).isEmpty();

      verify(productRepository).findActiveProducts();
    }
  }

  @Nested
  @DisplayName("Product Price Tests")
  class ProductPriceTests {

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 9.99, 99.99, 999.99, 9999.99})
    @DisplayName("Should create products with different prices")
    void shouldCreateProductsWithDifferentPrices(final double price) {
      // Arrange
      final Product productWithPrice =
          MockUtils.mockProduct(
              "Test Product",
              "Test Description",
              BigDecimal.valueOf(price),
              Product.Category.ELECTRONICS,
              10,
              true);

      when(productRepository.existsBySku(any())).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(productWithPrice);

      // Act
      final Product result = productService.createProduct(productWithPrice);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(price));
    }
  }
}
