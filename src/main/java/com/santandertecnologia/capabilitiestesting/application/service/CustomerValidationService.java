package com.santandertecnologia.capabilitiestesting.application.service;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.port.in.CustomerValidationUseCase;
import com.santandertecnologia.capabilitiestesting.domain.port.out.CacheService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ExternalCustomerService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación para validación de clientes. Implementa Optional para evitar manejo
 * directo de nulls y usa enums para tipos seguros. Usa UUID para identificación y ExternalCustomer.
 * RiskLevel como inner class.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CustomerValidationService implements CustomerValidationUseCase {

  private static final String CUSTOMER_CACHE_PREFIX = "customer:";
  private static final String RISK_LEVEL_CACHE_PREFIX = "risk:";
  private static final long CACHE_TTL_SECONDS = 300; // 5 minutos
  private final ExternalCustomerService externalCustomerService;
  private final CacheService cacheService;

  @Override
  public Optional<Boolean> validateCustomerCanOperate(final UUID customerId) {
    log.debug("Validating if customer can operate: {}", customerId);

    return externalCustomerService
        .getCustomerById(customerId)
        .map(
            customer -> {
              final boolean canOperate =
                  customer.isActive()
                      && (customer.riskLevel() == ExternalCustomer.RiskLevel.LOW
                          || customer.riskLevel() == ExternalCustomer.RiskLevel.MEDIUM);
              log.debug("Customer {} can operate: {}", customerId, canOperate);
              return canOperate;
            });
  }

  @Override
  public Optional<ExternalCustomer> getCustomerInfo(final UUID customerId) {
    log.debug("Getting customer info for: {}", customerId);

    // Intentar obtener del caché primero
    final Optional<ExternalCustomer> cachedCustomer =
        cacheService.get(CUSTOMER_CACHE_PREFIX + customerId, ExternalCustomer.class);

    if (cachedCustomer.isPresent()) {
      log.debug("Customer found in cache: {}", customerId);
      return cachedCustomer;
    }

    // Si no está en caché, obtener del servicio externo
    final Optional<ExternalCustomer> customer = externalCustomerService.getCustomerById(customerId);
    customer.ifPresent(
        c -> cacheService.put(CUSTOMER_CACHE_PREFIX + customerId, c, CACHE_TTL_SECONDS));

    return customer;
  }

  @Override
  public Optional<ExternalCustomer.RiskLevel> getCustomerRiskLevel(final UUID customerId) {
    log.debug("Getting risk level for customer: {}", customerId);

    // Intentar obtener del caché primero
    final Optional<ExternalCustomer.RiskLevel> cachedRiskLevel =
        cacheService.get(RISK_LEVEL_CACHE_PREFIX + customerId, ExternalCustomer.RiskLevel.class);

    if (cachedRiskLevel.isPresent()) {
      log.debug("Risk level found in cache for customer: {}", customerId);
      return cachedRiskLevel;
    }

    // Obtener del servicio externo o de la información del cliente
    final Optional<ExternalCustomer.RiskLevel> riskLevel =
        externalCustomerService
            .getCustomerRiskLevel(customerId)
            .or(() -> getCustomerInfo(customerId).map(ExternalCustomer::riskLevel));
    riskLevel.ifPresent(
        risk -> cacheService.put(RISK_LEVEL_CACHE_PREFIX + customerId, risk, CACHE_TTL_SECONDS));

    return riskLevel;
  }

  /** Método utilitario para realizar validaciones complejas de riesgo. */
  public Optional<Boolean> performComprehensiveRiskValidation(final UUID customerId) {
    log.debug("Performing comprehensive risk validation for customer: {}", customerId);

    return externalCustomerService
        .getCustomerById(customerId)
        .map(
            customer -> {
              // Validaciones comprehensivas
              final boolean passesValidation =
                  customer.isActive()
                      && customer.riskLevel() != ExternalCustomer.RiskLevel.HIGH
                      && customer.riskLevel() != ExternalCustomer.RiskLevel.BLOCKED
                      && hasCompleteContactInfo(customer);

              log.info(
                  "Customer {} comprehensive validation result: {}", customerId, passesValidation);
              return passesValidation;
            });
  }

  private boolean hasCompleteContactInfo(final ExternalCustomer customer) {
    return customer.email() != null && customer.phoneNumber() != null;
  }
}
