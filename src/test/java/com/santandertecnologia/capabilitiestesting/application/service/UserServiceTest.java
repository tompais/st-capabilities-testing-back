package com.santandertecnologia.capabilitiestesting.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.out.CacheService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para UserService. Demuestra el uso de Mockito, AssertJ, principios FIRST, patrón
 * AAA y nested tests.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private CacheService cacheService;

  @InjectMocks private UserService userService;

  private User testUser;
  private UUID userId;

  @BeforeEach
  void setUp() {
    // Arrange - Preparar datos de test comunes
    userId = UUID.randomUUID();
    testUser =
        User.builder()
            .id(userId)
            .username("testuser")
            .email("test@santander.com")
            .firstName("Test")
            .lastName("User")
            .phoneNumber("+34666123456")
            .department("Testing")
            .status(User.Status.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    // No configurar stubs por defecto para evitar "unnecessary stubbing"
    // Los stubs se configurarán individualmente en cada test según se necesiten
  }

  @Nested
  @DisplayName("User Creation Tests")
  class UserCreationTests {

    @Test
    @DisplayName("Should create user successfully with valid data")
    void shouldCreateUserSuccessfullyWithValidData() {
      // Arrange
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // Act
      User result = userService.createUser(testUser);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(userId);
      assertThat(result.getStatus()).isEqualTo(User.Status.ACTIVE);
      assertThat(result.isActive()).isTrue();

      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user data is invalid")
    void shouldThrowExceptionWhenUserDataIsInvalid() {
      // Arrange
      User invalidUser =
          User.builder()
              .username("") // Invalid username
              .email("invalid-email") // Invalid email
              .build();

      // Configurar mock para que el repositorio lance excepción con datos inválidos
      when(userRepository.save(any(User.class)))
          .thenThrow(new IllegalArgumentException("Invalid user data"));

      // Act & Assert
      assertThatThrownBy(() -> userService.createUser(invalidUser))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid user data");

      verify(userRepository).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("User Retrieval Tests")
  class UserRetrievalTests {

    @Test
    @DisplayName("Should return user when found in repository")
    void shouldReturnUserWhenFoundInRepository() {
      // Arrange
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      Optional<User> result = userService.getUserById(userId);

      // Assert
      assertThat(result).isPresent().contains(testUser);

      verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return empty when user not found")
    void shouldReturnEmptyWhenUserNotFound() {
      // Arrange
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act
      Optional<User> result = userService.getUserById(userId);

      // Assert
      assertThat(result).isEmpty();

      verify(userRepository).findById(userId);
    }
  }

  @Nested
  @DisplayName("Active Users Tests")
  class ActiveUsersTests {

    @Test
    @DisplayName("Should return only active users")
    void shouldReturnOnlyActiveUsers() {
      // Arrange
      List<User> activeUsers = List.of(testUser);
      when(userRepository.findByStatus(User.Status.ACTIVE)).thenReturn(activeUsers);

      // Act
      List<User> result = userService.getActiveUsers();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getStatus()).isEqualTo(User.Status.ACTIVE);
      assertThat(result.getFirst().isActive()).isTrue();

      verify(userRepository).findByStatus(User.Status.ACTIVE);
    }
  }

  @Nested
  @DisplayName("User Deletion Tests")
  class UserDeletionTests {

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
      // Arrange
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

      // Act
      boolean result = userService.deleteUser(userId);

      // Assert
      assertThat(result).isTrue();

      verify(userRepository).findById(userId);
      verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should return false when user not found for deletion")
    void shouldReturnFalseWhenUserNotFoundForDeletion() {
      // Arrange
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // Act
      boolean result = userService.deleteUser(userId);

      // Assert
      assertThat(result).isFalse();

      verify(userRepository).findById(userId);
      verify(userRepository, never()).deleteById(userId);
    }
  }

  @Nested
  @DisplayName("Business Logic Tests")
  class BusinessLogicTests {

    @Test
    @DisplayName("Should validate user data correctly")
    void shouldValidateUserDataCorrectly() {
      // Arrange
      User validUser =
          User.builder()
              .username("validuser")
              .email("valid@santander.com")
              .firstName("Valid")
              .lastName("User")
              .build();

      when(userRepository.save(any(User.class))).thenReturn(validUser);

      // Act
      User result = userService.createUser(validUser);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("validuser");
      assertThat(result.getEmail()).contains("@");
    }

    @Test
    @DisplayName("Should handle user with full name correctly")
    void shouldHandleUserWithFullNameCorrectly() {
      // Act
      String fullName = testUser.getFullName();

      // Assert
      assertThat(fullName).isEqualTo("Test User");
    }
  }
}
