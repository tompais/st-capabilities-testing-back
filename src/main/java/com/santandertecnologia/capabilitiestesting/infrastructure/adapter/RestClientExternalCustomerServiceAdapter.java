package com.santandertecnologia.capabilitiestesting.infrastructure.adapter;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ExternalCustomerService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Adaptador que implementa el puerto de salida ExternalCustomerService usando RestClient. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestClientExternalCustomerServiceAdapter implements ExternalCustomerService {

  private final RestClient restClient;

  @Override
  public Optional<ExternalCustomer> getCustomerById(UUID customerId) {
    log.debug("Fetching customer information for ID: {}", customerId);

    try {
      ExternalCustomerResponse response =
          restClient
              .get()
              .uri("/customers/{id}", customerId)
              .retrieve()
              .body(ExternalCustomerResponse.class);

      if (response == null) {
        log.warn("No customer found with ID: {}", customerId);
        return Optional.empty();
      }

      ExternalCustomer customer = mapToExternalCustomer(response);
      log.info("Successfully retrieved customer information for ID: {}", customerId);
      return Optional.of(customer);

    } catch (RestClientException e) {
      log.error("Error fetching customer with ID: {}", customerId, e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<ExternalCustomer.RiskLevel> getCustomerRiskLevel(UUID customerId) {
    log.debug("Getting risk level for customer ID: {}", customerId);

    try {
      RiskLevelResponse response =
          restClient
              .get()
              .uri("/customers/{id}/risk-level", customerId)
              .retrieve()
              .body(RiskLevelResponse.class);

      if (response == null || response.riskLevel() == null) {
        log.warn("No risk level found for customer ID: {}", customerId);
        return Optional.empty();
      }

      ExternalCustomer.RiskLevel riskLevel =
          ExternalCustomer.RiskLevel.valueOf(response.riskLevel().toUpperCase());
      log.info("Risk level for customer {}: {}", customerId, riskLevel);
      return Optional.of(riskLevel);

    } catch (RestClientException | IllegalArgumentException e) {
      log.error("Error fetching risk level for customer ID: {}", customerId, e);
      return Optional.empty();
    }
  }

  private ExternalCustomer mapToExternalCustomer(ExternalCustomerResponse response) {
    ExternalCustomer.RiskLevel riskLevel;
    try {
      riskLevel = ExternalCustomer.RiskLevel.valueOf(response.riskLevel().toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("Unknown risk level: {}, defaulting to LOW", response.riskLevel());
      riskLevel = ExternalCustomer.RiskLevel.LOW;
    }

    return ExternalCustomer.builder()
        .customerId(UUID.fromString(response.id()))
        .name(response.fullName())
        .email(response.email())
        .phoneNumber(response.phoneNumber())
        .active(response.active())
        .riskLevel(riskLevel)
        .validatedAt(LocalDateTime.now())
        .build();
  }

  /**
   * DTO para respuesta del servicio externo de clientes.
   *
   * @param id Getters y setters
   */
  public record ExternalCustomerResponse(
      String id,
      String fullName,
      String email,
      String phoneNumber,
      boolean active,
      String riskLevel) {}

  /** DTO para respuesta de nivel de riesgo. */
  public record RiskLevelResponse(String riskLevel) {}
}
