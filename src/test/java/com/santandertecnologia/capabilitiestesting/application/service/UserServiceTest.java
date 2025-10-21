package com.santandertecnologia.capabilitiestesting.application.service;

import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitarios para UserService. Demuestra el uso de Mockito, AssertJ, principios FIRST, patrón
 * AAA y nested tests. Refactorizado para usar Spring Cache annotations. Las pruebas de caché se
 * movieron a tests de integración ya que las anotaciones de caché solo funcionan con contexto de
 * Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  // Usar MockUtils para crear objetos de prueba consistentes
  private final User testUser = MockUtils.mockUser();
  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

  @Nested
  @DisplayName("User Creation Tests")
  class UserCreationTests {

    @Test
    @DisplayName("Should create user successfully with valid data")
    void shouldCreateUserSuccessfullyWithValidData() {
      // Arrange
      when(userRepository.save(any(User.class))).thenReturn(testUser);

      // Act
      final User result = userService.createUser(testUser);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(USER_ID);
      assertThat(result.getStatus()).isEqualTo(User.Status.ACTIVE);
      assertThat(result.isActive()).isTrue();

      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user data is invalid")
    void shouldThrowExceptionWhenUserDataIsInvalid() {
      // Arrange
      final User invalidUser = MockUtils.mockUser(USER_ID, "", "invalid-email");

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
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

      // Act
      final Optional<User> result = userService.getUserById(USER_ID);

      // Assert
      assertThat(result).isPresent().contains(testUser);

      verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("Should return empty when user not found in repository")
    void shouldReturnEmptyWhenUserNotFoundInRepository() {
      // Arrange
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      // Act
      final Optional<User> result = userService.getUserById(USER_ID);

      // Assert
      assertThat(result).isEmpty();

      verify(userRepository).findById(USER_ID);
    }
  }

  @Nested
  @DisplayName("Active Users Tests")
  class ActiveUsersTests {

    @Test
    @DisplayName("Should return only active users")
    void shouldReturnOnlyActiveUsers() {
      // Arrange
      final List<User> activeUsers = List.of(testUser);
      when(userRepository.findByStatus(User.Status.ACTIVE)).thenReturn(activeUsers);

      // Act
      final List<User> result = userService.getActiveUsers();

      // Assert
      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getStatus()).isEqualTo(User.Status.ACTIVE);
      assertThat(result.getFirst().isActive()).isTrue();

      verify(userRepository).findByStatus(User.Status.ACTIVE);
    }
  }

  @Nested
  @DisplayName("User Status Update Tests")
  class UserStatusUpdateTests {

    @Test
    @DisplayName("Should update user status successfully")
    void shouldUpdateUserStatusSuccessfully() {
      // Arrange - Usar MockUtils con status personalizado
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              invocation -> {
                final User user = invocation.getArgument(0);
                // Usar MockUtils para crear el usuario con status SUSPENDED
                return MockUtils.mockUser(
                    user.getId(), user.getUsername(), user.getEmail(), User.Status.SUSPENDED);
              });

      // Act
      final Optional<User> result = userService.updateUserStatus(USER_ID, User.Status.SUSPENDED);

      // Assert
      assertThat(result).isPresent();
      assertThat(result.get().getStatus()).isEqualTo(User.Status.SUSPENDED);

      verify(userRepository).findById(USER_ID);
      verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should return empty when updating non-existent user")
    void shouldReturnEmptyWhenUpdatingNonExistentUser() {
      // Arrange
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      // Act
      final Optional<User> result = userService.updateUserStatus(USER_ID, User.Status.INACTIVE);

      // Assert
      assertThat(result).isEmpty();

      verify(userRepository).findById(USER_ID);
      verify(userRepository, never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("User Deletion Tests")
  class UserDeletionTests {

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
      // Arrange
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

      // Act
      final boolean result = userService.deleteUser(USER_ID);

      // Assert
      assertThat(result).isTrue();

      verify(userRepository).findById(USER_ID);
      verify(userRepository).deleteById(USER_ID);
    }

    @Test
    @DisplayName("Should return false when user not found for deletion")
    void shouldReturnFalseWhenUserNotFoundForDeletion() {
      // Arrange
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      // Act
      final boolean result = userService.deleteUser(USER_ID);

      // Assert
      assertThat(result).isFalse();

      verify(userRepository).findById(USER_ID);
      verify(userRepository, never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("Business Logic Tests")
  class BusinessLogicTests {

    @Test
    @DisplayName("Should validate user data correctly")
    void shouldValidateUserDataCorrectly() {
      // Arrange
      final User validUser = MockUtils.mockUser(USER_ID, "validuser", "valid@santander.com");
      when(userRepository.save(any(User.class))).thenReturn(validUser);

      // Act
      final User result = userService.createUser(validUser);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("validuser");
      assertThat(result.getEmail()).contains("@");
    }

    @Test
    @DisplayName("Should handle user with full name correctly")
    void shouldHandleUserWithFullNameCorrectly() {
      // Act
      final String fullName = testUser.getFullName();

      // Assert
      assertThat(fullName).isEqualTo("Test User");
    }
  }
}
