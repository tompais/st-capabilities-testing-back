package com.santandertecnologia.capabilitiestesting.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Modelo de dominio para usuario. Sin Optional - usa valores por defecto o null según corresponda.
 * Usa UUID para identificación y Status como inner class.
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class User {

  @Builder.Default private final UUID id = UUID.randomUUID();
  private final String username;
  private final String email;
  private final String firstName;
  private final String lastName;

  @Builder.Default private final Status status = Status.ACTIVE;

  @Builder.Default private final LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default private final LocalDateTime updatedAt = LocalDateTime.now();

  // Campos opcionales que pueden ser null
  private final String phoneNumber;
  private final String department;
  private final LocalDateTime lastLoginAt;

  /** Obtiene el nombre completo del usuario. */
  public String getFullName() {
    if (firstName == null && lastName == null) {
      return username;
    }
    if (firstName == null) {
      return lastName;
    }
    if (lastName == null) {
      return firstName;
    }
    return firstName + " " + lastName;
  }

  /** Verifica si el usuario está activo. */
  public boolean isActive() {
    return Status.ACTIVE.equals(status);
  }

  /**
   * Enum inner class para el estado del usuario. Demuestra el uso de inner classes como parte de
   * los requerimientos de testing.
   */
  public enum Status {
    ACTIVE,
    SUSPENDED,
    INACTIVE
  }
}
