package com.santandertecnologia.capabilitiestesting.infrastructure.web.service;

import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.in.UserUseCase;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Tests parametrizados para UserWebService. Enfocado en testear la LÓGICA DE MAPEO y MANEJO DE
 * EXCEPCIONES. Refactorizado para usar TestConstants.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserWebService Tests")
class UserWebServiceTest {

  @Mock private UserUseCase userUseCase;

  @InjectMocks private UserWebService userWebService;

  /** Proveedor de datos para testing de mapeo de requests. */
  private static Stream<Arguments> provideUserRequestMappingData() {
    return Stream.of(
        Arguments.of("user1", "user1@santander.com", "John", "Doe", "+34666123456", "IT"),
        Arguments.of(
            "admin", "admin@santander.com", "Admin", "User", "+34666789012", "Administration"),
        Arguments.of("test_user", "test@santander.com", "Test", "Testing", null, null),
        Arguments.of(
            "manager-1",
            "manager@santander.com",
            "Project",
            "Manager",
            "+5511987654321",
            "Management"));
  }

  /** Proveedor de datos para testing de mapeo de excepciones. */
  private static Stream<Arguments> provideExceptionMappingData() {
    return Stream.of(
        // Solo incluir excepciones que el UserWebService maneja específicamente
        Arguments.of(IllegalArgumentException.class, "Invalid user data", HttpStatus.BAD_REQUEST)
        // RuntimeException y NullPointerException ya no se capturan en el servicio
        // Spring las manejará automáticamente en el controlador como 500
        );
  }

  @ParameterizedTest(name = "Status string ''{0}'' should map to User.Status.{1}")
  @DisplayName("Should correctly parse status strings to User.Status enum")
  @CsvSource({
    "ACTIVE, ACTIVE",
    "active, ACTIVE",
    "Active, ACTIVE",
    "SUSPENDED, SUSPENDED",
    "suspended, SUSPENDED",
    "Suspended, SUSPENDED",
    "INACTIVE, INACTIVE",
    "inactive, INACTIVE",
    "Inactive, INACTIVE"
  })
  void shouldParseStatusStringsCorrectly(String statusString, User.Status expectedStatus) {
    // Arrange
    User user =
        User.builder()
            .id(USER_ID)
            .username("testuser")
            .email("test@santander.com")
            .firstName("Test")
            .lastName("User")
            .status(expectedStatus)
            .build();

    when(userUseCase.updateUserStatus(USER_ID, expectedStatus)).thenReturn(Optional.of(user));

    // Act
    UserResponse result = userWebService.updateUserStatus(USER_ID, statusString);

    // Assert - UserResponse solo tiene los campos: id, email, name, phone, active
    assertThat(result.active()).isEqualTo(expectedStatus == User.Status.ACTIVE);
  }

  @ParameterizedTest(name = "Invalid status ''{0}'' should throw BAD_REQUEST exception")
  @DisplayName("Should throw ResponseStatusException for invalid status strings")
  @ValueSource(strings = {"INVALID", "ENABLED", "DISABLED", "", "null", "123", "active_user"})
  void shouldThrowExceptionForInvalidStatusStrings(String invalidStatus) {
    // Act & Assert
    assertThatThrownBy(() -> userWebService.updateUserStatus(USER_ID, invalidStatus))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).contains("Invalid status");
            });
  }

  @ParameterizedTest(
      name = "Request with fields: username={0}, email={1}, firstName={2} should map correctly")
  @DisplayName("Should correctly map CreateUserRequest to Domain User")
  @MethodSource("provideUserRequestMappingData")
  void shouldMapCreateUserRequestToDomain(
      String username,
      String email,
      String firstName,
      String lastName,
      String phoneNumber,
      String department) {
    // Arrange
    CreateUserRequest request =
        CreateUserRequest.builder()
            .username(username)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phoneNumber)
            .department(department)
            .build();

    User expectedUser =
        User.builder()
            .id(UUID.randomUUID())
            .username(username)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phoneNumber)
            .department(department)
            .status(User.Status.ACTIVE)
            .build();

    when(userUseCase.createUser(any(User.class))).thenReturn(expectedUser);

    // Act
    UserResponse result = userWebService.createUser(request);

    // Assert - Solo verificar campos disponibles en UserResponse
    assertThat(result.email()).isEqualTo(email);
    assertThat(result.name()).isEqualTo(firstName + " " + lastName); // getFullName()
    assertThat(result.phone()).isEqualTo(phoneNumber);
    assertThat(result.active()).isTrue();
  }

  @ParameterizedTest(name = "Exception type {0} should map to HTTP status {1}")
  @DisplayName("Should correctly map IllegalArgumentException to BAD_REQUEST")
  @MethodSource("provideExceptionMappingData")
  void shouldMapExceptionsToCorrectHttpStatus(
      Class<? extends Exception> exceptionType,
      String exceptionMessage,
      HttpStatus expectedStatus) {
    // Arrange
    CreateUserRequest request =
        CreateUserRequest.builder()
            .username("testuser")
            .email("test@santander.com")
            .firstName("Test")
            .lastName("User")
            .build();

    Exception exception = createException(exceptionType, exceptionMessage);
    when(userUseCase.createUser(any(User.class))).thenThrow(exception);

    // Act & Assert
    assertThatThrownBy(() -> userWebService.createUser(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });
  }

  @ParameterizedTest(name = "User not found scenario should throw {1}")
  @DisplayName("Should handle user not found scenarios correctly")
  @CsvSource({
    "false, NOT_FOUND" // Cuando deleteUser devuelve false, debe lanzar NOT_FOUND
  })
  void shouldHandleUserNotFoundScenarios(boolean deleteResult, HttpStatus expectedStatus) {
    // Arrange - Simular que el usuario no existe
    when(userUseCase.getUserById(USER_ID)).thenReturn(Optional.empty());
    when(userUseCase.updateUserStatus(USER_ID, User.Status.ACTIVE)).thenReturn(Optional.empty());
    when(userUseCase.deleteUser(USER_ID)).thenReturn(deleteResult);

    // Act & Assert - Test para getUserById
    assertThatThrownBy(() -> userWebService.getUserById(USER_ID))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });

    // Act & Assert - Test para updateUserStatus
    assertThatThrownBy(() -> userWebService.updateUserStatus(USER_ID, "ACTIVE"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });

    // Act & Assert - Test para deleteUser
    assertThatThrownBy(() -> userWebService.deleteUser(USER_ID))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });
  }

  @ParameterizedTest(name = "Domain User with status {0} should map to response active={1}")
  @DisplayName("Should correctly map domain User to UserResponse")
  @CsvSource({"ACTIVE, true", "SUSPENDED, false", "INACTIVE, false"})
  void shouldMapDomainUserToResponse(User.Status status, boolean expectedActive) {
    // Arrange
    User user =
        User.builder()
            .id(USER_ID)
            .username("testuser")
            .email("test@santander.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+34666123456")
            .department("IT")
            .status(status)
            .build();

    when(userUseCase.getUserById(USER_ID)).thenReturn(Optional.of(user));

    // Act
    UserResponse result = userWebService.getUserById(USER_ID);

    // Assert - Solo verificar campos disponibles en UserResponse
    assertThat(result.id()).isEqualTo(USER_ID);
    assertThat(result.email()).isEqualTo("test@santander.com");
    assertThat(result.name()).isEqualTo("Test User"); // getFullName() mapeado a name
    assertThat(result.phone()).isEqualTo("+34666123456");
    assertThat(result.active()).isEqualTo(expectedActive);
  }

  /** Helper method para crear excepciones dinámicamente en tests parametrizados. */
  private Exception createException(Class<? extends Exception> exceptionType, String message) {
    try {
      return exceptionType.getConstructor(String.class).newInstance(message);
    } catch (Exception e) {
      return new RuntimeException(message);
    }
  }
}
