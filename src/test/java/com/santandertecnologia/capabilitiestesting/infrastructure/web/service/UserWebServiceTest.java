package com.santandertecnologia.capabilitiestesting.infrastructure.web.service;

import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.STATUS_STRING_ACTIVE;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_DEPARTMENT;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_DEPARTMENT_ADMIN;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_EMAIL;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_EMAIL_ADMIN;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_FIRST_NAME;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_FIRST_NAME_ADMIN;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_FULL_NAME;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_ID;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_LAST_NAME;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_LAST_NAME_ADMIN;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_PHONE;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_PHONE_ADMIN;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_USERNAME;
import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_USERNAME_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.in.UserUseCase;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
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
 * EXCEPCIONES. Refactorizado para usar TestConstants y MockUtils.
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
            USER_USERNAME_ADMIN,
            USER_EMAIL_ADMIN,
            USER_FIRST_NAME_ADMIN,
            USER_LAST_NAME_ADMIN,
            USER_PHONE_ADMIN,
            USER_DEPARTMENT_ADMIN),
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
        // Enviar la excepción ya construida en lugar de usar reflection
        Arguments.of(new IllegalArgumentException("Invalid user data"), HttpStatus.BAD_REQUEST));
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
  void shouldParseStatusStringsCorrectly(
      final String statusString, final User.Status expectedStatus) {
    // Arrange - Usar MockUtils con status específico
    final User user = MockUtils.mockUser(expectedStatus);

    when(userUseCase.updateUserStatus(USER_ID, expectedStatus)).thenReturn(Optional.of(user));

    // Act
    final UserResponse result = userWebService.updateUserStatus(USER_ID, statusString);

    // Assert - UserResponse solo tiene los campos: id, email, name, phone, active
    assertThat(result.active()).isEqualTo(expectedStatus == User.Status.ACTIVE);
  }

  @ParameterizedTest(name = "Invalid status ''{0}'' should throw BAD_REQUEST exception")
  @DisplayName("Should throw ResponseStatusException for invalid status strings")
  @ValueSource(strings = {"INVALID", "ENABLED", "DISABLED", "", "null", "123", "active_user"})
  void shouldThrowExceptionForInvalidStatusStrings(final String invalidStatus) {
    // Act & Assert
    assertThatThrownBy(() -> userWebService.updateUserStatus(USER_ID, invalidStatus))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              final ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).contains("Invalid status");
            });
  }

  @ParameterizedTest(
      name = "Request with fields: username={0}, email={1}, firstName={2} should map correctly")
  @DisplayName("Should correctly map CreateUserRequest to Domain User")
  @MethodSource("provideUserRequestMappingData")
  void shouldMapCreateUserRequestToDomain(
      final String username,
      final String email,
      final String firstName,
      final String lastName,
      final String phoneNumber,
      final String department) {
    // Arrange - Usar MockUtils para CreateUserRequest
    final CreateUserRequest request =
        MockUtils.mockCreateUserRequest(
            username, email, firstName, lastName, phoneNumber, department);

    // Usar MockUtils para User con parámetros personalizados
    final User expectedUser =
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
    final UserResponse result = userWebService.createUser(request);

    // Assert - Solo verificar campos disponibles en UserResponse
    assertThat(result.email()).isEqualTo(email);
    assertThat(result.name()).isEqualTo(firstName + " " + lastName); // getFullName()
    assertThat(result.phone()).isEqualTo(phoneNumber);
    assertThat(result.active()).isTrue();
  }

  @ParameterizedTest(name = "Exception {0} should map to HTTP status {1}")
  @DisplayName("Should correctly map IllegalArgumentException to BAD_REQUEST")
  @MethodSource("provideExceptionMappingData")
  void shouldMapExceptionsToCorrectHttpStatus(
      final Exception exception, final HttpStatus expectedStatus) {
    // Arrange - Usar MockUtils para CreateUserRequest
    final CreateUserRequest request = MockUtils.mockCreateUserRequest();

    when(userUseCase.createUser(any(User.class))).thenThrow(exception);

    // Act & Assert
    assertThatThrownBy(() -> userWebService.createUser(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              final ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });
  }

  @ParameterizedTest(name = "User not found scenario should throw {1}")
  @DisplayName("Should handle user not found scenarios correctly")
  @CsvSource({
    "false, NOT_FOUND" // Cuando deleteUser devuelve false, debe lanzar NOT_FOUND
  })
  void shouldHandleUserNotFoundScenarios(
      final boolean deleteResult, final HttpStatus expectedStatus) {
    // Arrange - Simular que el usuario no existe
    when(userUseCase.getUserById(USER_ID)).thenReturn(Optional.empty());
    when(userUseCase.updateUserStatus(USER_ID, User.Status.ACTIVE)).thenReturn(Optional.empty());
    when(userUseCase.deleteUser(USER_ID)).thenReturn(deleteResult);

    // Act & Assert - Test para getUserById
    assertThatThrownBy(() -> userWebService.getUserById(USER_ID))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              final ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });

    // Act & Assert - Test para updateUserStatus usando constante
    assertThatThrownBy(() -> userWebService.updateUserStatus(USER_ID, STATUS_STRING_ACTIVE))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              final ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });

    // Act & Assert - Test para deleteUser
    assertThatThrownBy(() -> userWebService.deleteUser(USER_ID))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              final ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(expectedStatus);
            });
  }

  @ParameterizedTest(name = "Domain User with status {0} should map to response active={1}")
  @DisplayName("Should correctly map domain User to UserResponse")
  @CsvSource({"ACTIVE, true", "SUSPENDED, false", "INACTIVE, false"})
  void shouldMapDomainUserToResponse(final User.Status status, final boolean expectedActive) {
    // Arrange - Crear usuario con USER_ID específico y el status deseado
    final User user =
        User.builder()
            .id(USER_ID)
            .username(USER_USERNAME)
            .email(USER_EMAIL)
            .firstName(USER_FIRST_NAME)
            .lastName(USER_LAST_NAME)
            .phoneNumber(USER_PHONE)
            .department(USER_DEPARTMENT)
            .status(status)
            .build();

    when(userUseCase.getUserById(USER_ID)).thenReturn(Optional.of(user));

    // Act
    final UserResponse result = userWebService.getUserById(USER_ID);

    // Assert - Solo verificar campos disponibles en UserResponse usando constantes
    assertThat(result.id()).isEqualTo(USER_ID);
    assertThat(result.email()).isEqualTo(USER_EMAIL);
    assertThat(result.name()).isEqualTo(USER_FULL_NAME); // getFullName() mapeado a name
    assertThat(result.phone()).isEqualTo(USER_PHONE);
    assertThat(result.active()).isEqualTo(expectedActive);
  }
}
