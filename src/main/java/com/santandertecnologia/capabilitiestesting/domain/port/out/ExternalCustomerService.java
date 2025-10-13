package com.santandertecnologia.capabilitiestesting.domain.port.out;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para el servicio externo de clientes. Utiliza Optional para evitar manejo
 * directo de nulls. Usa UUID para identificación y ExternalCustomer.RiskLevel como inner class.
 */
public interface ExternalCustomerService {

  /**
   * Obtiene un cliente por su ID.
   *
   * @param customerId el ID del cliente
   * @return Optional con el cliente si existe, Optional.empty() si no se encuentra
   */
  Optional<ExternalCustomer> getCustomerById(UUID customerId);

  /**
   * Obtiene el nivel de riesgo de un cliente.
   *
   * @param customerId el ID del cliente
   * @return Optional con el nivel de riesgo si está disponible
   */
  Optional<ExternalCustomer.RiskLevel> getCustomerRiskLevel(UUID customerId);
}
