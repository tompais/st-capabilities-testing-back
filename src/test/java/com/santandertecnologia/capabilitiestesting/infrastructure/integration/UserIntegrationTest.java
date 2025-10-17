package com.santandertecnologia.capabilitiestesting.infrastructure.integration;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ExternalCustomerService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

@DisplayName("User Integration Tests - All Technologies with Embedded DBs")
class UserIntegrationTest extends BaseIntegrationTest {

  // Puerto fijo para MockWebServer que coincide con application-test.yml
  private static final int MOCK_SERVER_PORT = 8080;

  // MockWebServer para mockear servicios externos
  private MockWebServer mockWebServer;

  @Autowired private UserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private CacheManager cacheManager;

  // Mock para el servicio de External Customer (evitamos usar MongoDB containers)
  @MockitoBean private ExternalCustomerService externalCustomerService;

  @SneakyThrows
  @BeforeAll
  void setUpAll(@Autowired final WebApplicationContext webApplicationContext) {
    // Arrange - Configurar infraestructura de testing

    // Iniciar MockWebServer para simular servicios externos en el puerto 8080
    // Este puerto coincide con la URL configurada en application-test.yml
    mockWebServer = new MockWebServer();
    mockWebServer.start(MOCK_SERVER_PORT);

    // Configurar RestAssured una sola vez para todos los tests
    RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
  }

  @SneakyThrows
  @AfterAll
  void tearDownAll() {
    // Cleanup - Limpiar recursos después de todos los tests
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }

    // Reset de RestAssured para limpiar toda la configuración
    RestAssuredMockMvc.reset();
  }

  @BeforeEach
  void setUp() {
    // Arrange - Limpiar datos antes de cada test

    // Limpiar base de datos H2 antes de cada test
    userRepository.deleteAll();

    // Limpiar Redis cache manualmente para evitar interferencias entre tests
    if (cacheManager != null) {
      cacheManager.getCacheNames().forEach(cacheName -> {
        final var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
          cache.clear();
        }
      });
    }

    // Configurar comportamiento por defecto del mock del servicio externo
    when(externalCustomerService.getCustomerById(any(UUID.class))).thenReturn(Optional.empty());
  }

  /**
   * Nested class para organizar tests de CRUD de usuarios Demuestra el patrón de organización con
   * inner classes
   */
  @Nested
  @DisplayName("User CRUD Operations with Multiple Technologies")
  class UserCrudOperationsTests {

    /**
     * Test que demuestra integración completa: API REST + Base de datos H2 + Redis cache + Servicio
     * externo mockeado
     */
    @SneakyThrows
    @Test
    @DisplayName("Should create user and validate complete flow with all technologies")
    void shouldCreateUserAndValidateCompleteFlowWithAllTechnologies() {
      // Arrange - Preparar datos de prueba con UUID único
      final UUID userId = UUID.randomUUID();
      final String userEmail = "integration.test+" + userId + "@santander.com";

      final CreateUserRequest request =
          CreateUserRequest.builder()
              .username("integrationuser" + userId.toString().substring(0, 8))
              .email(userEmail)
              .firstName("Integration")
              .lastName("User")
              .build();

      // Mock del servicio externo de validación
      mockWebServer.enqueue(
          new MockResponse()
              .setBody("{\"valid\": true, \"risk\": \"LOW\"}")
              .addHeader("Content-Type", "application/json"));

      // Mock para el servicio de External Customer usando MockUtils
      final ExternalCustomer mockCustomer =
          MockUtils.mockExternalCustomer(
              UUID.randomUUID(),
              "Integration Customer",
              userEmail,
              true,
              ExternalCustomer.RiskLevel.LOW);

      when(externalCustomerService.getCustomerById(any(UUID.class)))
          .thenReturn(Optional.of(mockCustomer));

      // Act - Ejecutar operación de creación via API REST y verificar respuesta
      given()
          .contentType(ContentType.JSON)
          .body(objectMapper.writeValueAsString(request))
          .when()
          .post("/api/users")
          .then()
          .statusCode(201)
          .body("email", equalTo(request.email()))
          .body("name", equalTo(request.firstName() + " " + request.lastName()))
          .body("active", equalTo(true));
    }

    @Test
    @DisplayName("Should retrieve user from Redis cache on second request")
    void shouldRetrieveUserFromRedisCacheOnSecondRequest() {
      // Arrange - Crear usuario en base de datos H2
      final User user =
          User.builder()
              .username("cacheduser")
              .email("cached@santander.com")
              .firstName("Cached")
              .lastName("User")
              .status(User.Status.ACTIVE)
              .build();

      final User savedUser = userRepository.save(user);

      // Act & Assert - Primera request (desde DB, se guarda en Redis cache)
      given()
          .when()
          .get("/api/users/{id}", savedUser.getId().toString())
          .then()
          .statusCode(200)
          .body("email", equalTo("cached@santander.com"))
          .body("name", equalTo("Cached User"))
          .body("active", equalTo(true));

      // Act & Assert - Segunda request (desde Redis cache)
      given()
          .when()
          .get("/api/users/{id}", savedUser.getId().toString())
          .then()
          .statusCode(200)
          .body("email", equalTo("cached@santander.com"))
          .body("name", equalTo("Cached User"))
          .body("active", equalTo(true));
    }
  }

  /** Nested class para tests de validaciones complejas usando servicio externo mockeado */
  @Nested
  @DisplayName("Complex Validation Tests with External Service Integration")
  class ComplexValidationTests {

    @SneakyThrows
    @Test
    @DisplayName("Should validate user with external customer service integration")
    void shouldValidateUserWithExternalCustomerServiceIntegration() {
      // Arrange - Configurar mock del servicio externo usando MockUtils
      final UUID customerId = UUID.randomUUID();
      final ExternalCustomer mockCustomer =
          MockUtils.mockExternalCustomer(
              customerId,
              "Validated Customer",
              "validated@santander.com",
              true,
              ExternalCustomer.RiskLevel.LOW);

      when(externalCustomerService.getCustomerById(customerId))
          .thenReturn(Optional.of(mockCustomer));

      // Configurar mock del servicio HTTP externo usando Map.of
      final Map<String, Object> customerData =
          Map.of(
              "customerId",
              customerId.toString(),
              "active",
              true,
              "riskLevel",
              "LOW",
              "creditScore",
              750);

      mockWebServer.enqueue(
          new MockResponse()
              .setBody(objectMapper.writeValueAsString(customerData))
              .addHeader("Content-Type", "application/json"));

      final CreateUserRequest request =
          CreateUserRequest.builder()
              .username("validateduser")
              .email("validated@santander.com")
              .firstName("Validated")
              .lastName("User")
              .build();

      // Act - Crear usuario con validación externa
      given()
          .contentType(ContentType.JSON)
          .body(objectMapper.writeValueAsString(request))
          .when()
          .post("/api/users")
          .then()
          .statusCode(201)
          .body("active", equalTo(true));
    }
  }

  /** Nested class para tests de operaciones batch con H2 */
  @Nested
  @DisplayName("Batch Operations Tests with H2 Database")
  class BatchOperationsTests {

    @Test
    @DisplayName("Should retrieve all active users efficiently from H2")
    void shouldRetrieveAllActiveUsersEfficientlyFromH2() {
      // Arrange - Crear múltiples usuarios con diferentes estados
      for (int i = 0; i < 5; i++) {
        final User user =
            User.builder()
                .username("batchuser" + i)
                .email("batch" + i + "@santander.com")
                .firstName("Batch")
                .lastName("User" + i)
                .status(i % 2 == 0 ? User.Status.ACTIVE : User.Status.INACTIVE)
                .build();
        userRepository.save(user);
      }

      // Act & Assert - Obtener solo usuarios activos
      given()
          .when()
          .get("/api/users/active")
          .then()
          .statusCode(200)
          .body("", hasSize(3)) // Solo 3 usuarios activos (índices 0, 2, 4)
          .body("[0].active", equalTo(true))
          .body("[1].active", equalTo(true))
          .body("[2].active", equalTo(true));
    }

    @Test
    @DisplayName("Should handle cross-service operations between H2 and external services")
    void shouldHandleCrossServiceOperationsBetweenH2AndExternalServices() {
      // Arrange - Crear datos en H2 y configurar mock de servicio externo
      final User user =
          User.builder()
              .username("crossserviceuser")
              .email("crossservice@santander.com")
              .firstName("CrossService")
              .lastName("User")
              .status(User.Status.ACTIVE)
              .build();
      final User savedUser = userRepository.save(user);

      // Configurar mock del servicio externo usando MockUtils
      final ExternalCustomer mockCustomer =
          MockUtils.mockExternalCustomer(
              savedUser.getId(),
              "Cross Service Customer",
              "crossservice@santander.com",
              true,
              ExternalCustomer.RiskLevel.MEDIUM);

      when(externalCustomerService.getCustomerById(savedUser.getId()))
          .thenReturn(Optional.of(mockCustomer));

      // Act & Assert - Verificar que podemos acceder a datos de H2
      given()
          .when()
          .get("/api/users/{id}", savedUser.getId().toString())
          .then()
          .statusCode(200)
          .body("email", equalTo("crossservice@santander.com"))
          .body("name", equalTo("CrossService User"))
          .body("active", equalTo(true));
    }
  }

  /** Nested class para tests de manejo de errores */
  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @SneakyThrows
    @Test
    @DisplayName("Should handle external service failure gracefully")
    void shouldHandleExternalServiceFailureGracefully() {
      // Arrange - Configurar fallo del servicio externo
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));

      final CreateUserRequest request =
          CreateUserRequest.builder()
              .username("failureuser")
              .email("failure@santander.com")
              .firstName("Failure")
              .lastName("User")
              .build();

      // Act & Assert - El usuario debería crearse a pesar del fallo externo
      given()
          .contentType(ContentType.JSON)
          .body(objectMapper.writeValueAsString(request))
          .when()
          .post("/api/users")
          .then()
          .statusCode(201); // Se crea el usuario con validación por defecto
    }

    @Test
    @DisplayName("Should return 404 for non-existent user")
    void shouldReturn404ForNonExistentUser() {
      // Arrange
      final UUID nonExistentId = UUID.randomUUID();

      // Act & Assert
      given().when().get("/api/users/{id}", nonExistentId.toString()).then().statusCode(404);
    }
  }
}

