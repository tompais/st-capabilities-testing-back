package com.santandertecnologia.capabilitiestesting.infrastructure.integration;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.santandertecnologia.capabilitiestesting.CapabilitiesTestingApplication;
import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ExternalCustomerService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import io.github.tobi.laa.spring.boot.embedded.redis.standalone.EmbeddedRedisStandalone;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests de integración completos que demuestran todas las tecnologías de testing: - RestAssured
 * MockMvc para tests de API REST - H2 para base de datos relacional en memoria - Flapdoodle MongoDB
 * embebido para NoSQL (usando mocks para simplificar) - Spring Boot embedded Redis para caché -
 * MockWebServer para servicios externos - AssertJ para assertions claras - Principios FIRST y
 * patrón AAA - UUIDs e inner classes
 */
@SpringBootTest(classes = CapabilitiesTestingApplication.class)
@EmbeddedRedisStandalone // Habilitar Redis embebido para tests
@Transactional // Asegurar aislamiento transaccional
@Rollback // Rollback automático después de cada test
@DisplayName("Complete Integration Tests - All Technologies with Embedded DBs")
class CompleteIntegrationTest {

  // MockWebServer para mockear servicios externos
  private static MockWebServer mockWebServer;

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private UserRepository userRepository;

  @Autowired private ObjectMapper objectMapper;

  // Mock para el servicio de External Customer (evitamos usar MongoDB containers)
  @MockitoBean private ExternalCustomerService externalCustomerService;

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    // Redis embebido se configura automáticamente con @EmbeddedRedisStandalone
    registry.add("spring.cache.type", () -> "redis");

    // H2 en memoria para JPA
    registry.add(
        "spring.datasource.url", () -> "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    // Configurar servicio externo mockeado
    if (mockWebServer != null) {
      registry.add(
          "external.customer.service.url", () -> mockWebServer.url("/customers").toString());
    }
  }

  @BeforeAll
  static void setUpAll() throws Exception {
    // Arrange - Configurar infraestructura de testing

    // Iniciar MockWebServer para simular servicios externos
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterAll
  static void tearDownAll() throws Exception {
    // Cleanup - Limpiar recursos después de todos los tests
    if (mockWebServer != null) {
      mockWebServer.shutdown();
    }
  }

  @BeforeEach
  void setUp() {
    // Arrange - Configurar RestAssured y limpiar datos antes de cada test
    RestAssuredMockMvc.webAppContextSetup(webApplicationContext);

    // Limpiar base de datos H2 antes de cada test
    userRepository.deleteAll();

    // Configurar comportamiento por defecto del mock del servicio externo
    when(externalCustomerService.getCustomerById(any(UUID.class))).thenReturn(Optional.empty());
  }

  @AfterEach
  void tearDown() {
    // Cleanup - Limpiar datos después de cada test para aislamiento
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
     * Test que demuestra integración completa: API REST + Base de datos H2 + Redis cache + Servicio
     * externo mockeado
     */
    @Test
    @DisplayName("Should create user and validate complete flow with all technologies")
    void shouldCreateUserAndValidateCompleteFlowWithAllTechnologies() throws Exception {
      // Arrange - Preparar datos de prueba con UUID único
      UUID userId = UUID.randomUUID();
      String userEmail = "integration.test+" + userId + "@santander.com";

      CreateUserRequest request =
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

      // Mock para el servicio de External Customer
      ExternalCustomer mockCustomer =
          ExternalCustomer.builder()
              .customerId(UUID.randomUUID())
              .name("Integration Customer")
              .email(userEmail)
              .active(true)
              .riskLevel(ExternalCustomer.RiskLevel.LOW)
              .build();

      when(externalCustomerService.getCustomerById(any(UUID.class)))
          .thenReturn(Optional.of(mockCustomer));

      // Act - Ejecutar operación de creación via API REST
      String location =
          given()
              .contentType(ContentType.JSON)
              .body(objectMapper.writeValueAsString(request))
              .when()
              .post("/api/users")
              .then()
              .statusCode(201)
              .body("email", equalTo(request.email()))
              .body("name", equalTo(request.firstName() + " " + request.lastName()))
              .body("active", equalTo(true))
              .extract()
              .header("Location");

      // Assert - Verificar que el usuario fue persistido en H2
      assertThat(location).isNotNull();
      String createdUserId = location.substring(location.lastIndexOf("/") + 1);

      Optional<User> savedUser = userRepository.findById(UUID.fromString(createdUserId));
      assertThat(savedUser).isPresent();
      assertThat(savedUser.get().getEmail()).isEqualTo(userEmail);
      assertThat(savedUser.get().getStatus()).isEqualTo(User.Status.ACTIVE);
    }

    @Test
    @DisplayName("Should retrieve user from Redis cache on second request")
    void shouldRetrieveUserFromRedisCacheOnSecondRequest() {
      // Arrange - Crear usuario en base de datos H2
      User user =
          User.builder()
              .username("cacheduser")
              .email("cached@santander.com")
              .firstName("Cached")
              .lastName("User")
              .status(User.Status.ACTIVE)
              .build();

      User savedUser = userRepository.save(user);

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

    @Test
    @DisplayName("Should validate user with external customer service integration")
    void shouldValidateUserWithExternalCustomerServiceIntegration() throws Exception {
      // Arrange - Configurar mock del servicio externo
      UUID customerId = UUID.randomUUID();
      ExternalCustomer mockCustomer =
          ExternalCustomer.builder()
              .customerId(customerId)
              .name("Validated Customer")
              .email("validated@santander.com")
              .active(true)
              .riskLevel(ExternalCustomer.RiskLevel.LOW)
              .build();

      when(externalCustomerService.getCustomerById(customerId))
          .thenReturn(Optional.of(mockCustomer));

      // Configurar mock del servicio HTTP externo
      Map<String, Object> customerData = new HashMap<>();
      customerData.put("customerId", customerId.toString());
      customerData.put("active", true);
      customerData.put("riskLevel", "LOW");
      customerData.put("creditScore", 750);

      mockWebServer.enqueue(
          new MockResponse()
              .setBody(objectMapper.writeValueAsString(customerData))
              .addHeader("Content-Type", "application/json"));

      CreateUserRequest request =
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
      List<User> users = new ArrayList<>();

      for (int i = 0; i < 5; i++) {
        User user =
            User.builder()
                .username("batchuser" + i)
                .email("batch" + i + "@santander.com")
                .firstName("Batch")
                .lastName("User" + i)
                .status(i % 2 == 0 ? User.Status.ACTIVE : User.Status.INACTIVE)
                .build();
        users.add(userRepository.save(user));
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
      User user =
          User.builder()
              .username("crossserviceuser")
              .email("crossservice@santander.com")
              .firstName("CrossService")
              .lastName("User")
              .status(User.Status.ACTIVE)
              .build();
      User savedUser = userRepository.save(user);

      // Configurar mock del servicio externo
      ExternalCustomer mockCustomer =
          ExternalCustomer.builder()
              .customerId(savedUser.getId())
              .name("Cross Service Customer")
              .email("crossservice@santander.com")
              .active(true)
              .riskLevel(ExternalCustomer.RiskLevel.MEDIUM)
              .build();

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

    @Test
    @DisplayName("Should handle external service failure gracefully")
    void shouldHandleExternalServiceFailureGracefully() throws Exception {
      // Arrange - Configurar fallo del servicio externo
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));

      CreateUserRequest request =
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
      UUID nonExistentId = UUID.randomUUID();

      // Act & Assert
      given().when().get("/api/users/{id}", nonExistentId.toString()).then().statusCode(404);
    }
  }
}
