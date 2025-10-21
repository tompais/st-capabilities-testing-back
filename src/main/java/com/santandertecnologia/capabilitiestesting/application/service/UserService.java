package com.santandertecnologia.capabilitiestesting.application.service;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.in.UserUseCase;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación para gestión de usuarios. Implementa Optional para evitar manejo directo
 * de nulls. Usa UUID para identificación y User.Status como inner class. Utiliza Spring Cache
 * annotations para gestión automática de caché.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserService implements UserUseCase {

  private final UserRepository userRepository;

  @Override
  @CachePut(value = "users", key = "#result.id")
  public User createUser(final User user) {
    log.info("Creating new user with username: {}", user.getUsername());

    if (userRepository.existsByUsername(user.getUsername())) {
      throw new IllegalArgumentException("Username already exists: " + user.getUsername());
    }

    if (userRepository.existsByEmail(user.getEmail())) {
      throw new IllegalArgumentException("Email already exists: " + user.getEmail());
    }

    // Generar UUID si no se proporciona
    final User userToSave =
        user.getId() == null
            ? User.builder()
                .id(UUID.randomUUID())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus())
                .phoneNumber(user.getPhoneNumber())
                .department(user.getDepartment())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build()
            : user;

    final User savedUser = userRepository.save(userToSave);

    log.info("User created successfully with ID: {}", savedUser.getId());
    return savedUser;
  }

  @Override
  @Cacheable(value = "users", key = "#id")
  public Optional<User> getUserById(final UUID id) {
    log.debug("Getting user by ID: {}", id);

    final Optional<User> user = userRepository.findById(id);

    return user;
  }

  @Override
  public List<User> getActiveUsers() {
    log.debug("Getting all active users");
    return userRepository.findByStatus(User.Status.ACTIVE);
  }

  @Override
  @CachePut(value = "users", key = "#id")
  public Optional<User> changeUserStatus(final UUID id, final User.Status status) {
    log.info("Changing user status for ID: {} to: {}", id, status);

    return userRepository
        .findById(id)
        .map(
            existingUser -> {
              final User updatedUser =
                  User.builder()
                      .id(existingUser.getId())
                      .username(existingUser.getUsername())
                      .email(existingUser.getEmail())
                      .firstName(existingUser.getFirstName())
                      .lastName(existingUser.getLastName())
                      .status(status)
                      .phoneNumber(existingUser.getPhoneNumber())
                      .department(existingUser.getDepartment())
                      .createdAt(existingUser.getCreatedAt())
                      .lastLoginAt(existingUser.getLastLoginAt())
                      .build();

              final User savedUser = userRepository.save(updatedUser);

              log.info("User status changed successfully for ID: {}", id);
              return savedUser;
            });
  }

  @Override
  @CacheEvict(value = "users", key = "#id")
  public boolean deleteUser(final UUID id) {
    log.info("Deleting user with ID: {}", id);

    return userRepository
        .findById(id)
        .map(
            user -> {
              userRepository.deleteById(id);
              log.info("User deleted successfully with ID: {}", id);
              return true;
            })
        .orElseGet(
            () -> {
              log.warn("User not found for deletion with ID: {}", id);
              return false;
            });
  }

  @Override
  @CachePut(value = "users", key = "#id")
  public Optional<User> updateUserStatus(final UUID id, final User.Status status) {
    log.info("Updating status for user ID: {} to: {}", id, status);

    return userRepository
        .findById(id)
        .map(
            existingUser -> {
              final User updatedUser =
                  User.builder()
                      .id(existingUser.getId())
                      .username(existingUser.getUsername())
                      .email(existingUser.getEmail())
                      .firstName(existingUser.getFirstName())
                      .lastName(existingUser.getLastName())
                      .phoneNumber(existingUser.getPhoneNumber())
                      .department(existingUser.getDepartment())
                      .status(status)
                      .createdAt(existingUser.getCreatedAt())
                      .updatedAt(LocalDateTime.now())
                      .lastLoginAt(existingUser.getLastLoginAt())
                      .build();

              final User savedUser = userRepository.save(updatedUser);

              log.info("User status updated successfully for ID: {}", id);
              return savedUser;
            });
  }

  private boolean isUserStatusValid(final UUID id, final User.Status status) {
    return userRepository.findById(id).map(user -> user.getStatus() != status).orElse(false);
  }
}
