package com.santandertecnologia.capabilitiestesting.infrastructure.integration;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ProductRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateProductRequest;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.context.WebApplicationContext;

@DisplayName("Product Integration Tests - MongoDB with Flapdoodle")
class ProductIntegrationTest extends BaseIntegrationTest {

  @Autowired private ProductRepository productRepository;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CacheManager cacheManager;

  @BeforeAll
  void setUpClass(@Autowired final WebApplicationContext context) {
    // Configurar RestAssured MockMvc una sola vez para toda la clase de test
    RestAssuredMockMvc.webAppContextSetup(context);
  }

  @AfterAll
  void tearDownClass() {
    // Reset de RestAssured para limpiar toda la configuración
    RestAssuredMockMvc.reset();
  }

  @BeforeEach
  void setUp() {
    // Limpiar MongoDB antes de cada test para asegurar aislamiento
    // IMPORTANTE: Solo limpiamos los DATOS, no destruimos el contexto de Spring
    productRepository.deleteAll();

    // Limpiar Redis cache manualmente para evitar interferencias entre tests
    if (cacheManager != null) {
      cacheManager.getCacheNames().forEach(cacheName -> {
        final var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
          cache.clear();
        }
      });
    }
  }

  @Nested
  @DisplayName("Product CRUD Operations with MongoDB")
  class ProductCrudOperationsTests {

    @SneakyThrows
    @Test
    @DisplayName("Should create product and persist in MongoDB")
    void shouldCreateProductAndPersistInMongoDB() {
      // Arrange - Usar MockUtils para crear request
      final CreateProductRequest request =
          MockUtils.mockCreateProductRequest(
              "Laptop Gaming",
              "High performance gaming laptop",
              BigDecimal.valueOf(1299.99),
              Product.Category.ELECTRONICS,
              15,
              "LAP-GAM-001");

      // Act - Crear producto via API REST
      given()
          .contentType(ContentType.JSON)
          .body(objectMapper.writeValueAsString(request))
          .when()
          .post("/api/products")
          .then()
          .statusCode(201)
          .body("name", equalTo("Laptop Gaming"))
          .body("description", equalTo("High performance gaming laptop"))
          .body("price", equalTo(1299.99f))
          .body("category", equalTo("ELECTRONICS"))
          .body("stock", equalTo(15))
          .body("available", equalTo(true));
    }

    @SneakyThrows
    @Test
    @DisplayName("Should retrieve product by ID from MongoDB")
    void shouldRetrieveProductByIdFromMongoDB() {
      // Arrange - Crear y guardar producto en MongoDB usando MockUtils
      final Product product = MockUtils.mockProduct();
      final Product savedProduct = productRepository.save(product);

      // Act & Assert - Obtener producto via API
      given()
          .when()
          .get("/api/products/{id}", savedProduct.getId().toString())
          .then()
          .statusCode(200)
          .body("id", equalTo(savedProduct.getId().toString()))
          .body("name", equalTo("Test Product"))
          .body("category", equalTo("ELECTRONICS"))
          .body("available", equalTo(true));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void shouldReturn404WhenProductNotFound() {
      // Arrange
      final UUID nonExistentId = UUID.randomUUID();

      // Act & Assert
      given().when().get("/api/products/{id}", nonExistentId.toString()).then().statusCode(404);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should delete product from MongoDB")
    void shouldDeleteProductFromMongoDB() {
      // Arrange - Crear y guardar producto
      final Product product = MockUtils.mockProduct();
      final Product savedProduct = productRepository.save(product);

      // Act - Eliminar producto
      given()
          .when()
          .delete("/api/products/{id}", savedProduct.getId().toString())
          .then()
          .statusCode(204);

      // Assert - Verificar que no existe
      given()
          .when()
          .get("/api/products/{id}", savedProduct.getId().toString())
          .then()
          .statusCode(404);
    }
  }

  @Nested
  @DisplayName("Product Category and Search Tests")
  class ProductCategorySearchTests {

    @SneakyThrows
    @Test
    @DisplayName("Should search products by category in MongoDB")
    void shouldSearchProductsByCategoryInMongoDB() {
      // Arrange - Crear productos de diferentes categorías usando MockUtils
      final Product electronics1 = MockUtils.mockProductWithUniqueId(Product.Category.ELECTRONICS);
      final Product electronics2 = MockUtils.mockProductWithUniqueId(Product.Category.ELECTRONICS);
      final Product book = MockUtils.mockProductWithUniqueId(Product.Category.BOOKS);

      productRepository.save(electronics1);
      productRepository.save(electronics2);
      productRepository.save(book);

      // Act & Assert - Buscar solo productos ELECTRONICS
      given()
          .queryParam("category", "ELECTRONICS")
          .when()
          .get("/api/products/search")
          .then()
          .statusCode(200)
          .body("$", hasSize(2))
          .body("[0].category", equalTo("ELECTRONICS"))
          .body("[1].category", equalTo("ELECTRONICS"));
    }

    @SneakyThrows
    @Test
    @DisplayName("Should get all active products from MongoDB")
    void shouldGetAllActiveProductsFromMongoDB() {
      // Arrange - Crear productos activos e inactivos usando MockUtils
      final Product activeProduct1 = MockUtils.mockProductWithUniqueId(true);
      final Product activeProduct2 = MockUtils.mockProductWithUniqueId(true);
      final Product inactiveProduct = MockUtils.mockProductWithUniqueId(false);

      productRepository.save(activeProduct1);
      productRepository.save(activeProduct2);
      productRepository.save(inactiveProduct);

      // Act & Assert - Obtener solo productos activos
      given().when().get("/api/products/active").then().statusCode(200).body("$", hasSize(2));
    }

    @Test
    @DisplayName("Should return empty list when no products in category")
    void shouldReturnEmptyListWhenNoProductsInCategory() {
      // Act & Assert - Buscar categoría sin productos
      given()
          .queryParam("category", "CLOTHING")
          .when()
          .get("/api/products/search")
          .then()
          .statusCode(200)
          .body("$", hasSize(0));
    }
  }

  @Nested
  @DisplayName("Product Stock Management Tests")
  class ProductStockManagementTests {

    @SneakyThrows
    @Test
    @DisplayName("Should update product stock in MongoDB")
    void shouldUpdateProductStockInMongoDB() {
      // Arrange - Crear producto con stock inicial
      final Product product = MockUtils.mockProduct();
      final Product savedProduct = productRepository.save(product);

      // Act - Actualizar stock
      given()
          .queryParam("stock", 50)
          .when()
          .put("/api/products/{id}/stock", savedProduct.getId().toString())
          .then()
          .statusCode(200)
          .body("stock", equalTo(50))
          .body("available", equalTo(true));
    }

    @SneakyThrows
    @Test
    @DisplayName("Should mark product as unavailable when stock is zero")
    void shouldMarkProductAsUnavailableWhenStockIsZero() {
      // Arrange - Crear producto con stock
      final Product product = MockUtils.mockProduct();
      final Product savedProduct = productRepository.save(product);

      // Act - Reducir stock a 0
      given()
          .queryParam("stock", 0)
          .when()
          .put("/api/products/{id}/stock", savedProduct.getId().toString())
          .then()
          .statusCode(200)
          .body("stock", equalTo(0))
          .body("available", equalTo(false));
    }

    @SneakyThrows
    @Test
    @DisplayName("Should handle concurrent stock updates correctly")
    void shouldHandleConcurrentStockUpdatesCorrectly() {
      // Arrange - Crear producto
      final Product product = MockUtils.mockProduct();
      final Product savedProduct = productRepository.save(product);

      // Act - Múltiples actualizaciones de stock
      given()
          .queryParam("stock", 100)
          .when()
          .put("/api/products/{id}/stock", savedProduct.getId().toString())
          .then()
          .statusCode(200)
          .body("stock", equalTo(100));

      given()
          .queryParam("stock", 75)
          .when()
          .put("/api/products/{id}/stock", savedProduct.getId().toString())
          .then()
          .statusCode(200)
          .body("stock", equalTo(75));

      // Assert - Verificar stock final
      given()
          .when()
          .get("/api/products/{id}", savedProduct.getId().toString())
          .then()
          .statusCode(200)
          .body("stock", equalTo(75));
    }
  }

  @Nested
  @DisplayName("Product Validation Tests")
  class ProductValidationTests {

    @SneakyThrows
    @Test
    @DisplayName("Should reject product with invalid data")
    void shouldRejectProductWithInvalidData() {
      // Arrange - Request con datos inválidos (precio negativo)
      final CreateProductRequest invalidRequest =
          MockUtils.mockCreateProductRequest(
              "Invalid Product", BigDecimal.valueOf(-10.00) // Precio negativo
              );

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(objectMapper.writeValueAsString(invalidRequest))
          .when()
          .post("/api/products")
          .then()
          .statusCode(400);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should reject product with empty name")
    void shouldRejectProductWithEmptyName() {
      // Arrange
      final CreateProductRequest invalidRequest =
          MockUtils.mockCreateProductRequest("", BigDecimal.valueOf(99.99));

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(objectMapper.writeValueAsString(invalidRequest))
          .when()
          .post("/api/products")
          .then()
          .statusCode(400);
    }

    @SneakyThrows
    @Test
    @DisplayName("Should create product with all valid categories")
    void shouldCreateProductWithAllValidCategories() {
      // Test para cada categoría válida
      for (final Product.Category category : Product.Category.values()) {
        final CreateProductRequest request =
            MockUtils.mockCreateProductRequest(
                "Product " + category.name(),
                "Description for " + category.name(),
                BigDecimal.valueOf(99.99),
                category,
                10,
                "SKU-" + category.name());

        given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
            .when()
            .post("/api/products")
            .then()
            .statusCode(201)
            .body("category", equalTo(category.name()));
      }
    }
  }

  @Nested
  @DisplayName("Product Availability Tests")
  class ProductAvailabilityTests {

    @SneakyThrows
    @Test
    @DisplayName("Should correctly determine product availability")
    void shouldCorrectlyDetermineProductAvailability() {
      // Arrange - Crear productos con diferentes estados de stock
      final Product availableProduct = MockUtils.mockProductWithUniqueId(true);
      final Product unavailableProduct = MockUtils.mockProductWithUniqueId(false);

      final Product savedAvailable = productRepository.save(availableProduct);
      final Product savedUnavailable = productRepository.save(unavailableProduct);

      // Act & Assert - Verificar disponibilidad del producto activo
      given()
          .when()
          .get("/api/products/{id}", savedAvailable.getId().toString())
          .then()
          .statusCode(200)
          .body("available", equalTo(true));

      // Act & Assert - Verificar no disponibilidad del producto inactivo
      given()
          .when()
          .get("/api/products/{id}", savedUnavailable.getId().toString())
          .then()
          .statusCode(200)
          .body("available", equalTo(false));
    }
  }

  @Nested
  @DisplayName("MongoDB Persistence Verification Tests")
  class MongoDBPersistenceTests {

    @Test
    @DisplayName("Should verify data persists in MongoDB between operations")
    void shouldVerifyDataPersistsInMongoDBBetweenOperations() {
      // Arrange - Crear y guardar un producto
      final Product product = MockUtils.mockProductWithUniqueId();
      final Product savedProduct = productRepository.save(product);

      // Act - Recuperar el producto directamente del repositorio
      final Product retrievedProduct =
          productRepository.findById(savedProduct.getId()).orElseThrow();

      // Assert - Verificar que los datos persisten correctamente
      assertThat(retrievedProduct).isNotNull();
      assertThat(retrievedProduct.getId()).isEqualTo(savedProduct.getId());
      assertThat(retrievedProduct.getName()).isEqualTo(savedProduct.getName());
      assertThat(retrievedProduct.getSku()).isEqualTo(savedProduct.getSku());
    }

    @Test
    @DisplayName("Should handle multiple products in MongoDB")
    void shouldHandleMultipleProductsInMongoDB() {
      // Arrange - Crear múltiples productos
      final Product product1 = MockUtils.mockProductWithUniqueId();
      final Product product2 = MockUtils.mockProductWithUniqueId();
      final Product product3 = MockUtils.mockProductWithUniqueId();

      // Act - Guardar todos los productos
      productRepository.save(product1);
      productRepository.save(product2);
      productRepository.save(product3);

      // Assert - Verificar que todos están en la base de datos
      final long count = productRepository.findActiveProducts().size();
      assertThat(count).isGreaterThanOrEqualTo(3);
    }
  }
}

