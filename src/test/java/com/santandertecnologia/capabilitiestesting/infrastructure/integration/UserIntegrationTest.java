package com.santandertecnologia.capabilitiestesting.infrastructure.integration;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import com.santandertecnologia.capabilitiestesting.utils.TestConstants;
import io.restassured.http.ContentType;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;

@DisplayName("User Integration Tests - All Technologies with Embedded DBs")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserIntegrationTest extends BaseIntegrationTest {

  // Inyección por constructor de las dependencias usando Lombok
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  // MockWebServer para mockear servicios externos (RestClient)
  private MockWebServer mockWebServer;

  // Puerto del mock server leído desde configuración
  @Value("${test.mock.server.port}")
  private int mockServerPort;

  @SneakyThrows
  @BeforeAll
  void setUpMockWebServer() {
    // Arrange - Configurar MockWebServer para simular servicios externos
    // NOTA: La configuración de RestAssured se hereda de BaseIntegrationTest

    // Iniciar MockWebServer para simular las llamadas del RestClient
    // Este puerto coincide con la URL configurada en application-test.yml
    mockWebServer = new MockWebServer();
    mockWebServer.start(mockServerPort);
  }

  @SneakyThrows
  @AfterAll
  void tearDownMockWebServer() {
    // Cleanup - Cerrar MockWebServer después de todos los tests
    // NOTA: El reset de RestAssured se hereda de BaseIntegrationTest
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }

  @BeforeEach
  void setUp() {
    // Arrange - Limpiar datos antes de cada test

    // Limpiar base de datos H2 antes de cada test
    userRepository.deleteAll();
  }

  /**
   * Nested class para organizar tests de CRUD de usuarios Demuestra el patrón de organización con
   * inner classes
   */
  @Nested
  @DisplayName("User CRUD Operations with Multiple Technologies")
  class UserCrudOperationsTests {

    /**
     * Test que demuestra integración completa: API REST + Base de datos H2 + Redis cache +
     * MockWebServer para servicios externos
     */
    @SneakyThrows
    @Test
    @DisplayName("Should create user and validate complete flow with all technologies")
    void shouldCreateUserAndValidateCompleteFlowWithAllTechnologies() {
      // Arrange - Preparar datos de prueba con UUID único
      final UUID userId = UUID.randomUUID();
      final String userEmail = "integration.test+" + userId + "@santander.com";
      // Usar solo caracteres alfanuméricos del UUID (sin guiones) para el username
      final String username =
          "integrationuser" + userId.toString().replace("-", "").substring(0, 8);

      final CreateUserRequest request =
          MockUtils.mockCreateUserRequest(
              username,
              userEmail,
              "Integration",
              "User",
              "+34600000000", // Formato correcto sin guiones
              "IT");

      // Mock del servicio externo usando MockWebServer
      final Map<String, Object> customerData =
          Map.of(
              "id",
              userId.toString(),
              "fullName",
              "Integration Customer",
              "email",
              userEmail,
              "phoneNumber",
              "+34600000000", // Formato correcto sin guiones
              "active",
              true,
              "riskLevel",
              "LOW");

      mockWebServer.enqueue(
          new MockResponse()
              .setBody(objectMapper.writeValueAsString(customerData))
              .addHeader("Content-Type", "application/json"));

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
      // Arrange - Crear usuario en base de datos H2 usando MockUtils
      final User user = MockUtils.mockUser();
      final User savedUser = userRepository.save(user);

      // Act & Assert - Primera request (desde DB, se guarda en Redis cache)
      given()
          .when()
          .get("/api/users/{id}", savedUser.getId().toString())
          .then()
          .statusCode(200)
          .body("email", equalTo(TestConstants.USER_EMAIL)) // Usar constante correcta
          .body("active", equalTo(true));

      // Act & Assert - Segunda request (desde Redis cache)
      given()
          .when()
          .get("/api/users/{id}", savedUser.getId().toString())
          .then()
          .statusCode(200)
          .body("email", equalTo(TestConstants.USER_EMAIL)) // Usar constante correcta
          .body("active", equalTo(true));
    }
  }

  /** Nested class para tests de validaciones complejas usando MockWebServer */
  @Nested
  @DisplayName("Complex Validation Tests with External Service Integration")
  class ComplexValidationTests {

    @SneakyThrows
    @Test
    @DisplayName(
        "Should validate user with external customer service integration via MockWebServer")
    void shouldValidateUserWithExternalCustomerServiceIntegration() {
      // Arrange - Configurar mock del servicio externo usando MockWebServer
      final UUID customerId = UUID.randomUUID();

      // Configurar respuesta del servicio HTTP externo
      final Map<String, Object> customerData =
          Map.of(
              "id", customerId.toString(),
              "fullName", "Validated Customer",
              "email", "validated@santander.com",
              "phoneNumber", "+34-666-777-888",
              "active", true,
              "riskLevel", "LOW",
              "creditScore", 750);

      mockWebServer.enqueue(
          new MockResponse()
              .setBody(objectMapper.writeValueAsString(customerData))
              .addHeader("Content-Type", "application/json"));

      final CreateUserRequest request =
          MockUtils.mockCreateUserRequest("validateduser", "validated@santander.com");

      // Act - Crear usuario con validación externa via MockWebServer
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
      // Arrange - Crear múltiples usuarios con diferentes estados usando MockUtils
      for (int i = 0; i < 5; i++) {
        final User.Status status = i % 2 == 0 ? User.Status.ACTIVE : User.Status.INACTIVE;
        final User user =
            MockUtils.mockUser(
                UUID.randomUUID(), "batchuser" + i, "batchuser" + i + "@santander.com", status);
        userRepository.save(user);
      }

      // Act & Assert - Obtener solo usuarios activos
      given()
          .when()
          .get("/api/users/active")
          .then()
          .statusCode(200)
          .body("", hasSize(3)); // Solo 3 usuarios activos (índices 0, 2, 4)
    }

    @SneakyThrows
    @Test
    @DisplayName("Should handle cross-service operations between H2 and MockWebServer")
    void shouldHandleCrossServiceOperationsBetweenH2AndExternalServices() {
      // Arrange - Crear datos en H2 con email personalizado usando MockUtils
      final UUID userId = UUID.randomUUID();
      final User user =
          MockUtils.mockUser(userId, "crossserviceuser", "crossservice@santander.com");
      final User savedUser = userRepository.save(user);

      // Configurar respuesta del MockWebServer
      final Map<String, Object> customerData =
          Map.of(
              "id", savedUser.getId().toString(),
              "fullName", "Cross Service Customer",
              "email", "crossservice@santander.com",
              "phoneNumber", "+34-600-000-000",
              "active", true,
              "riskLevel", "MEDIUM");

      mockWebServer.enqueue(
          new MockResponse()
              .setBody(objectMapper.writeValueAsString(customerData))
              .addHeader("Content-Type", "application/json"));

      // Act & Assert - Verificar que podemos acceder a datos de H2
      given()
          .when()
          .get("/api/users/{id}", savedUser.getId().toString())
          .then()
          .statusCode(200)
          .body("email", equalTo("crossservice@santander.com"))
          .body("active", equalTo(true));
    }
  }

  /** Nested class para tests de manejo de errores */
  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @SneakyThrows
    @Test
    @DisplayName("Should handle external service failure gracefully via MockWebServer")
    void shouldHandleExternalServiceFailureGracefully() {
      // Arrange - Configurar fallo del servicio externo en MockWebServer
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));

      final CreateUserRequest request =
          MockUtils.mockCreateUserRequest("failureuser", "failure@santander.com");

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
