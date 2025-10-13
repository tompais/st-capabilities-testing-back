package com.santandertecnologia.capabilitiestesting.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Modelo de dominio para cliente externo. Demuestra el uso de inner classes para niveles de riesgo
 * y validación de negocio.
 */
@Builder(toBuilder = true)
@Jacksonized
public record ExternalCustomer(
    UUID customerId,
    String name,
    String email,
    String phoneNumber,
    Boolean active,
    RiskLevel riskLevel,
    LocalDateTime validatedAt) {

  /** Verifica si el cliente está activo. */
  public boolean isActive() {
    return Boolean.TRUE.equals(active);
  }

  /** Verifica si el cliente puede realizar operaciones. */
  public boolean canPerformOperations() {
    return isActive() && (RiskLevel.LOW.equals(riskLevel) || RiskLevel.MEDIUM.equals(riskLevel));
  }

  /** Verifica si el cliente tiene actividad reciente. */
  public boolean hasRecentActivity() {
    return validatedAt != null && validatedAt.isAfter(LocalDateTime.now().minusDays(30));
  }

  /** Verifica si el cliente tiene información de contacto completa. */
  public boolean hasCompleteContactInfo() {
    return email != null
        && !email.trim().isEmpty()
        && phoneNumber != null
        && !phoneNumber.trim().isEmpty();
  }

  /**
   * Verifica si el cliente es válido para registro. Un cliente es válido si está activo y tiene
   * riesgo bajo o medio.
   */
  public boolean isValidForRegistration() {
    return Boolean.TRUE.equals(active)
        && (RiskLevel.LOW.equals(riskLevel) || RiskLevel.MEDIUM.equals(riskLevel));
  }

  /**
   * Enum inner class para los niveles de riesgo del cliente. Demuestra el uso de inner classes con
   * métodos de negocio.
   */
  @Getter
  public enum RiskLevel {
    LOW(1, "Bajo riesgo"),
    MEDIUM(2, "Riesgo medio"),
    HIGH(3, "Alto riesgo"),
    CRITICAL(4, "Riesgo crítico"),
    BLOCKED(5, "Bloqueado");

    private final int priority;
    private final String description;

    RiskLevel(int priority, String description) {
      this.priority = priority;
      this.description = description;
    }
  }
}
