package com.santandertecnologia.capabilitiestesting.infrastructure.web.controller;

import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_ID;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.service.UserWebService;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tests de integración para UserController usando RestAssured MockMvc. Actualizado para la nueva
 * estructura con @ResponseStatus y UserWebService. Refactorizado para usar MockUtils.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UserController Tests")
class UserControllerTest {

  // Inicializar objetos de prueba a nivel de clase usando MockUtils
  private final UserResponse testUserResponse = MockUtils.mockUserResponse();
  private final CreateUserRequest createUserRequest = MockUtils.mockCreateUserRequest();

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserWebService userWebService;

  @BeforeAll
  void setUpAll() {
    // Configurar RestAssured MockMvc una sola vez para todos los tests
    // Usar mockMvc() en lugar de standaloneSetup() para usar el contexto completo de Spring
    RestAssuredMockMvc.mockMvc(mockMvc);
  }

  @AfterAll
  void tearDownAll() {
    // Limpiar configuración de RestAssured después de todos los tests
    RestAssuredMockMvc.reset();
  }

  @Nested
  @DisplayName("Create User Tests")
  class CreateUserTests {

    @Test
    @DisplayName("Should create user successfully with 201 status")
    void shouldCreateUserSuccessfullyWith201Status() {
      // Arrange
      when(userWebService.createUser(any(CreateUserRequest.class))).thenReturn(testUserResponse);

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(createUserRequest)
          .when()
          .post("/api/users")
          .then()
          .statusCode(201) // HttpStatus.CREATED
          .body("id", equalTo(USER_ID.toString()))
          .body("email", equalTo("test@santander.com"))
          .body("name", equalTo("Test User"))
          .body("phone", equalTo("+34666123456"))
          .body("active", equalTo(true));
    }

    @Test
    @DisplayName("Should return 400 for validation errors")
    void shouldReturn400ForValidationErrors() {
      // Arrange - Usar MockUtils con parámetros inválidos
      final CreateUserRequest invalidRequest = MockUtils.mockCreateUserRequest("", "invalid-email");

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(invalidRequest)
          .when()
          .post("/api/users")
          .then()
          .statusCode(400); // Validation error
    }

    @Test
    @DisplayName("Should return 400 when service throws ResponseStatusException")
    void shouldReturn400WhenServiceThrowsResponseStatusException() {
      // Arrange
      when(userWebService.createUser(any(CreateUserRequest.class)))
          .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data"));

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(createUserRequest)
          .when()
          .post("/api/users")
          .then()
          .statusCode(400);
    }
  }

  @Nested
  @DisplayName("Get User Tests")
  class GetUserTests {

    @Test
    @DisplayName("Should return user with 200 status when found")
    void shouldReturnUserWith200StatusWhenFound() {
      // Arrange
      when(userWebService.getUserById(USER_ID)).thenReturn(testUserResponse);

      // Act & Assert
      given()
          .pathParam("id", USER_ID)
          .when()
          .get("/api/users/{id}")
          .then()
          .statusCode(200) // HttpStatus.OK
          .body("id", equalTo(USER_ID.toString()))
          .body("email", equalTo("test@santander.com"))
          .body("name", equalTo("Test User"))
          .body("phone", equalTo("+34666123456"))
          .body("active", equalTo(true));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() {
      // Arrange
      when(userWebService.getUserById(USER_ID))
          .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      // Act & Assert
      given()
          .pathParam("id", USER_ID)
          .when()
          .get("/api/users/{id}")
          .then()
          .statusCode(404); // HttpStatus.NOT_FOUND
    }
  }

  @Nested
  @DisplayName("Update User Status Tests")
  class UpdateUserStatusTests {

    @Test
    @DisplayName("Should update status with 200 response")
    void shouldUpdateStatusWith200Response() {
      // Arrange - Usar MockUtils con estado inactivo
      final UserResponse suspendedUser = MockUtils.mockUserResponse(false);

      when(userWebService.updateUserStatus(USER_ID, "SUSPENDED")).thenReturn(suspendedUser);

      // Act & Assert
      given()
          .pathParam("id", USER_ID)
          .queryParam("status", "SUSPENDED")
          .when()
          .put("/api/users/{id}/status")
          .then()
          .statusCode(200) // HttpStatus.OK
          .body("active", equalTo(false));
    }

    @Test
    @DisplayName("Should return 400 for invalid status")
    void shouldReturn400ForInvalidStatus() {
      // Arrange
      when(userWebService.updateUserStatus(USER_ID, "INVALID"))
          .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status"));

      // Act & Assert
      given()
          .pathParam("id", USER_ID)
          .queryParam("status", "INVALID")
          .when()
          .put("/api/users/{id}/status")
          .then()
          .statusCode(400);
    }
  }

  @Nested
  @DisplayName("Get Active Users Tests")
  class GetActiveUsersTests {

    @Test
    @DisplayName("Should return active users list with 200 status")
    void shouldReturnActiveUsersListWith200Status() {
      // Arrange - Usar MockUtils para crear la lista
      final List<UserResponse> activeUsers = List.of(testUserResponse);
      when(userWebService.getActiveUsers()).thenReturn(activeUsers);

      // Act & Assert
      given()
          .when()
          .get("/api/users/active")
          .then()
          .statusCode(200) // HttpStatus.OK
          .body("$", hasSize(1))
          .body("[0].active", equalTo(true));
    }

    @Test
    @DisplayName("Should return empty list when no active users")
    void shouldReturnEmptyListWhenNoActiveUsers() {
      // Arrange
      when(userWebService.getActiveUsers()).thenReturn(List.of());

      // Act & Assert
      given().when().get("/api/users/active").then().statusCode(200).body("$", hasSize(0));
    }
  }

  @Nested
  @DisplayName("Delete User Tests")
  class DeleteUserTests {

    @Test
    @DisplayName("Should delete user with 204 No Content status")
    void shouldDeleteUserWith204NoContentStatus() {
      // Arrange - No exception means successful deletion
      // (void method, no return needed)

      // Act & Assert
      given()
          .pathParam("id", USER_ID)
          .when()
          .delete("/api/users/{id}")
          .then()
          .statusCode(204); // HttpStatus.NO_CONTENT
    }

    @Test
    @DisplayName("Should return 404 when user not found for deletion")
    void shouldReturn404WhenUserNotFoundForDeletion() {
      // Arrange
      doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
          .when(userWebService)
          .deleteUser(USER_ID);

      // Act & Assert
      given().pathParam("id", USER_ID).when().delete("/api/users/{id}").then().statusCode(404);
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle internal server errors with 500 status")
    void shouldHandleInternalServerErrorsWith500Status() {
      // Arrange
      when(userWebService.createUser(any(CreateUserRequest.class)))
          .thenThrow(
              new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"));

      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body(createUserRequest)
          .when()
          .post("/api/users")
          .then()
          .statusCode(500);
    }

    @Test
    @DisplayName("Should handle malformed JSON with 400 status")
    void shouldHandleMalformedJsonWith400Status() {
      // Act & Assert
      given()
          .contentType(ContentType.JSON)
          .body("{ invalid json }")
          .when()
          .post("/api/users")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("Should handle missing content type with 415 status")
    void shouldHandleMissingContentTypeWith415Status() {
      // Act & Assert
      given()
          .body(createUserRequest)
          .when()
          .post("/api/users")
          .then()
          .statusCode(415); // Unsupported Media Type
    }
  }
}
