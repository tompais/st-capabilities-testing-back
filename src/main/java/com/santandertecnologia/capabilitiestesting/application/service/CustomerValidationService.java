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
  public Optional<Boolean> validateCustomerCanOperate(UUID customerId) {
    log.info("Validating if customer can operate: {}", customerId);

    return getCustomerInfo(customerId)
        .map(
            customer -> {
              boolean canOperate = customer.canPerformOperations();
              log.info("Customer {} can operate: {}", customerId, canOperate);
              return canOperate;
            })
        .or(
            () -> {
              log.warn("Customer not found for validation: {}", customerId);
              return Optional.empty();
            });
  }

  @Override
  public Optional<ExternalCustomer> getCustomerInfo(UUID customerId) {
    log.debug("Getting customer info for: {}", customerId);

    // Intentar obtener del caché primero
    Optional<ExternalCustomer> cachedCustomer =
        cacheService.get(CUSTOMER_CACHE_PREFIX + customerId, ExternalCustomer.class);

    if (cachedCustomer.isPresent()) {
      log.debug("Customer found in cache: {}", customerId);
      return cachedCustomer;
    }

    // Si no está en caché, obtener del servicio externo
    Optional<ExternalCustomer> customer = externalCustomerService.getCustomerById(customerId);

    // Cachear si se encuentra
    customer.ifPresent(
        c -> cacheService.put(CUSTOMER_CACHE_PREFIX + customerId, c, CACHE_TTL_SECONDS));

    return customer;
  }

  @Override
  public Optional<ExternalCustomer.RiskLevel> getCustomerRiskLevel(UUID customerId) {
    log.debug("Getting risk level for customer: {}", customerId);

    // Intentar obtener del caché primero
    Optional<ExternalCustomer.RiskLevel> cachedRiskLevel =
        cacheService.get(RISK_LEVEL_CACHE_PREFIX + customerId, ExternalCustomer.RiskLevel.class);

    if (cachedRiskLevel.isPresent()) {
      log.debug("Risk level found in cache for customer: {}", customerId);
      return cachedRiskLevel;
    }

    // Obtener del servicio externo o de la información del cliente
    Optional<ExternalCustomer.RiskLevel> riskLevel =
        externalCustomerService
            .getCustomerRiskLevel(customerId)
            .or(() -> getCustomerInfo(customerId).map(ExternalCustomer::riskLevel));

    // Cachear si se encuentra
    riskLevel.ifPresent(
        risk -> cacheService.put(RISK_LEVEL_CACHE_PREFIX + customerId, risk, CACHE_TTL_SECONDS));

    return riskLevel;
  }

  /** Método utilitario para realizar validaciones complejas de riesgo. */
  public Optional<Boolean> performComprehensiveRiskValidation(UUID customerId) {
    log.info("Performing comprehensive risk validation for customer: {}", customerId);

    return getCustomerInfo(customerId)
        .flatMap(
            customer ->
                getCustomerRiskLevel(customerId)
                    .map(
                        riskLevel -> {
                          boolean passesValidation =
                              customer.isActive()
                                  && customer.hasCompleteContactInfo()
                                  && riskLevel != ExternalCustomer.RiskLevel.HIGH
                                  && riskLevel != ExternalCustomer.RiskLevel.BLOCKED;

                          log.info(
                              "Customer {} comprehensive validation result: {}",
                              customerId,
                              passesValidation);
                          return passesValidation;
                        }));
  }

  /** Método utilitario para obtener un resumen del estado del cliente. */
  public Optional<String> getCustomerStatusSummary(UUID customerId) {
    log.debug("Getting status summary for customer: {}", customerId);

    return getCustomerInfo(customerId)
        .map(
            customer -> {
              String summaryText =
                  "Customer "
                      + customerId
                      + ": "
                      + "Active="
                      + customer.isActive()
                      + ", Risk="
                      + customer.riskLevel()
                      + ", CanOperate="
                      + customer.canPerformOperations()
                      + ", RecentActivity="
                      + customer.hasRecentActivity();
              log.debug("Customer summary: {}", summaryText);
              return summaryText;
            });
  }
}
