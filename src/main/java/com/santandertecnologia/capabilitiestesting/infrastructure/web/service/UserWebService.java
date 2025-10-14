package com.santandertecnologia.capabilitiestesting.infrastructure.web.service;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.in.UserUseCase;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio web que maneja la lógica específica de la capa web. Responsable de mapeo entre DTOs y
 * entidades del dominio, manejo de excepciones y transformaciones de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserWebService {

  private final UserUseCase userUseCase;

  /** Crea un nuevo usuario mapeando el DTO a entidad del dominio. */
  public UserResponse createUser(CreateUserRequest request) {
    try {
      User user = mapRequestToDomain(request);
      User createdUser = userUseCase.createUser(user);
      return mapDomainToResponse(createdUser);
    } catch (IllegalArgumentException e) {
      log.error("Invalid user data: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid user data: " + e.getMessage(), e);
    }
  }

  /** Obtiene un usuario por ID con manejo de excepciones apropiado. */
  public UserResponse getUserById(UUID id) {
    return userUseCase
        .getUserById(id)
        .map(this::mapDomainToResponse)
        .orElseThrow(
            () -> {
              log.warn("User not found with ID: {}", id);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, "User not found with ID: " + id);
            });
  }

  /** Obtiene todos los usuarios activos. */
  public List<UserResponse> getActiveUsers() {
    return userUseCase.getActiveUsers().stream().map(this::mapDomainToResponse).toList();
  }

  /** Actualiza el estado de un usuario con validación de enum. */
  public UserResponse updateUserStatus(UUID id, String status) {
    try {
      User.Status userStatus = parseUserStatus(status);
      return userUseCase
          .updateUserStatus(id, userStatus)
          .map(this::mapDomainToResponse)
          .orElseThrow(
              () -> {
                log.warn("User not found for status update: {}", id);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found with ID: " + id);
              });
    } catch (IllegalArgumentException e) {
      log.error("Invalid status value: {}", status);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Invalid status: " + status + ". Valid values: ACTIVE, SUSPENDED, INACTIVE",
          e);
    }
  }

  /** Elimina un usuario con validación de existencia. */
  public void deleteUser(UUID id) {
    boolean deleted = userUseCase.deleteUser(id);
    if (!deleted) {
      log.warn("User not found for deletion: {}", id);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with ID: " + id);
    }
  }

  /** Mapea CreateUserRequest a User del dominio. */
  private User mapRequestToDomain(CreateUserRequest request) {
    return User.builder()
        .username(request.username())
        .email(request.email())
        .firstName(request.firstName())
        .lastName(request.lastName())
        .phoneNumber(request.phoneNumber())
        .department(request.department())
        .status(User.Status.ACTIVE) // Por defecto activo
        .build();
  }

  /** Mapea User del dominio a UserResponse. */
  private UserResponse mapDomainToResponse(User user) {
    return UserResponse.builder()
        .id(user.getId()) // Ya no necesita conversión a Long
        .email(user.getEmail())
        .name(user.getFullName())
        .phone(user.getPhoneNumber())
        .active(user.isActive())
        .build();
  }

  /** Parsea string a User.Status con validación. */
  private User.Status parseUserStatus(String status) {
    try {
      return User.Status.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid status: " + status);
    }
  }
}
