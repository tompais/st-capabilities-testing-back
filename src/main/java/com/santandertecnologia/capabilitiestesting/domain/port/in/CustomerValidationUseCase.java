package com.santandertecnologia.capabilitiestesting.domain.port.in;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para casos de uso de validación de clientes. Utiliza Optional para evitar
 * manejo directo de nulls y enums para tipos seguros. Usa UUID para identificación y
 * ExternalCustomer.RiskLevel como inner class.
 */
public interface CustomerValidationUseCase {

  /** Valida si un cliente puede realizar operaciones. */
  Optional<Boolean> validateCustomerCanOperate(UUID customerId);

  /** Obtiene información completa de un cliente. */
  Optional<ExternalCustomer> getCustomerInfo(UUID customerId);

  /** Obtiene el nivel de riesgo de un cliente. */
  Optional<ExternalCustomer.RiskLevel> getCustomerRiskLevel(UUID customerId);
}
