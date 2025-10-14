package com.santandertecnologia.capabilitiestesting.infrastructure.web.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.service.UserWebService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tests de integración para UserController usando RestAssured MockMvc. Actualizado para la nueva
 * estructura con @ResponseStatus y UserWebService.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test") // Forzar uso del perfil test
@DisplayName("UserController Integration Tests - Refactored")
class UserControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserWebService userWebService;

  private UserResponse testUserResponse;
  private CreateUserRequest createUserRequest;
  private UUID userId;

  @BeforeEach
  void setUp() {
    RestAssuredMockMvc.mockMvc(mockMvc);

    userId = UUID.randomUUID();
    testUserResponse =
        UserResponse.builder()
            .id(userId)
            .email("test@santander.com")
            .name("Test User") // Solo usar campos disponibles
            .phone("+34666123456")
            .active(true)
            .build();

    createUserRequest =
        CreateUserRequest.builder()
            .username("testuser")
            .email("test@santander.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+34666123456")
            .department("Testing")
            .build();
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
          .body("id", equalTo(userId.toString()))
          .body("email", equalTo("test@santander.com"))
          .body("name", equalTo("Test User"))
          .body("phone", equalTo("+34666123456"))
          .body("active", equalTo(true));
    }

    @Test
    @DisplayName("Should return 400 for validation errors")
    void shouldReturn400ForValidationErrors() {
      // Arrange
      CreateUserRequest invalidRequest =
          CreateUserRequest.builder()
              .username("") // Invalid: empty username
              .email("invalid-email") // Invalid: no @ symbol
              .build();

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
      when(userWebService.getUserById(userId)).thenReturn(testUserResponse);

      // Act & Assert
      given()
          .pathParam("id", userId)
          .when()
          .get("/api/users/{id}")
          .then()
          .statusCode(200) // HttpStatus.OK
          .body("id", equalTo(userId.toString()))
          .body("email", equalTo("test@santander.com"))
          .body("name", equalTo("Test User"))
          .body("phone", equalTo("+34666123456"))
          .body("active", equalTo(true));
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() {
      // Arrange
      when(userWebService.getUserById(userId))
          .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      // Act & Assert
      given()
          .pathParam("id", userId)
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
      // Arrange
      UserResponse suspendedUser =
          UserResponse.builder()
              .id(userId)
              .email("test@santander.com")
              .name("Test User")
              .phone("+34666123456")
              .active(false) // SUSPENDED = inactive
              .build();

      when(userWebService.updateUserStatus(userId, "SUSPENDED")).thenReturn(suspendedUser);

      // Act & Assert
      given()
          .pathParam("id", userId)
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
      when(userWebService.updateUserStatus(userId, "INVALID"))
          .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status"));

      // Act & Assert
      given()
          .pathParam("id", userId)
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
      // Arrange
      List<UserResponse> activeUsers = List.of(testUserResponse);
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
          .pathParam("id", userId)
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
          .deleteUser(userId);

      // Act & Assert
      given().pathParam("id", userId).when().delete("/api/users/{id}").then().statusCode(404);
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
