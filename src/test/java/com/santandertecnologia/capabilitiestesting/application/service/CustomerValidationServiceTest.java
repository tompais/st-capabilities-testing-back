package com.santandertecnologia.capabilitiestesting.application.service;

import static com.santandertecnologia.capabilitiestesting.utils.TestConstants.CUSTOMER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ExternalCustomerService;
import com.santandertecnologia.capabilitiestesting.utils.MockUtils;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests parametrizados para CustomerValidationService. Demuestra @ParameterizedTest con diferentes
 * fuentes de datos. Refactorizado para usar MockUtils y TestConstants. Las pruebas de caché se
 * movieron a tests de integración ya que las anotaciones de caché solo funcionan con contexto de
 * Spring.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerValidationService Tests")
class CustomerValidationServiceTest {

  @Mock private ExternalCustomerService externalCustomerService;

  @InjectMocks private CustomerValidationService customerValidationService;

  /** Proveedor de datos para risk level validation. */
  private static Stream<Arguments> provideRiskLevelData() {
    return Stream.of(
        // Probar cada nivel de riesgo disponible
        Arguments.of(ExternalCustomer.RiskLevel.LOW),
        Arguments.of(ExternalCustomer.RiskLevel.MEDIUM),
        Arguments.of(ExternalCustomer.RiskLevel.HIGH),
        Arguments.of(ExternalCustomer.RiskLevel.CRITICAL),
        Arguments.of(ExternalCustomer.RiskLevel.BLOCKED));
  }

  /** Proveedor de datos para combined validation scenarios. */
  private static Stream<Arguments> provideCombinedValidationData() {
    return Stream.of(
        // active, risk, hasCompleteInfo, expected
        Arguments.of(true, ExternalCustomer.RiskLevel.LOW, true, true),
        Arguments.of(true, ExternalCustomer.RiskLevel.LOW, false, false),
        Arguments.of(true, ExternalCustomer.RiskLevel.MEDIUM, true, true),
        Arguments.of(true, ExternalCustomer.RiskLevel.MEDIUM, false, false),
        Arguments.of(true, ExternalCustomer.RiskLevel.HIGH, true, false),
        Arguments.of(true, ExternalCustomer.RiskLevel.HIGH, false, false),
        Arguments.of(false, ExternalCustomer.RiskLevel.LOW, true, false),
        Arguments.of(false, ExternalCustomer.RiskLevel.LOW, false, false),
        Arguments.of(
            true,
            ExternalCustomer.RiskLevel.CRITICAL,
            true,
            true), // CRITICAL es permitido según la lógica real
        Arguments.of(false, ExternalCustomer.RiskLevel.CRITICAL, false, false),
        Arguments.of(true, ExternalCustomer.RiskLevel.BLOCKED, true, false),
        Arguments.of(false, ExternalCustomer.RiskLevel.BLOCKED, false, false));
  }

  @ParameterizedTest(
      name = "Customer with risk level {0} and active={1} should be able to operate: {2}")
  @DisplayName("Should validate customer operation permissions based on risk level and status")
  @CsvSource({
    "LOW, true, true",
    "LOW, false, false",
    "MEDIUM, true, true",
    "MEDIUM, false, false",
    "HIGH, true, false",
    "HIGH, false, false",
    "CRITICAL, true, false",
    "CRITICAL, false, false",
    "BLOCKED, true, false",
    "BLOCKED, false, false"
  })
  void shouldValidateCustomerOperationPermissionsBasedOnRiskAndStatus(
      final ExternalCustomer.RiskLevel riskLevel,
      final boolean active,
      final boolean expectedCanOperate) {
    // Arrange - Usar MockUtils para crear el customer con los parámetros necesarios
    final ExternalCustomer customer = MockUtils.mockExternalCustomer(riskLevel, active);
    when(externalCustomerService.getCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));

    // Act
    final Optional<Boolean> result =
        customerValidationService.validateCustomerCanOperate(CUSTOMER_ID);

    // Assert
    assertThat(result).isPresent().contains(expectedCanOperate);
  }

  @ParameterizedTest(name = "Risk level {0} should map to risk category correctly")
  @DisplayName("Should retrieve and validate customer risk level from external service")
  @MethodSource("provideRiskLevelData")
  void shouldRetrieveAndValidateCustomerRiskLevel(final ExternalCustomer.RiskLevel riskLevel) {
    // Arrange
    final ExternalCustomer customer = MockUtils.mockExternalCustomer(riskLevel);
    when(externalCustomerService.getCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    when(externalCustomerService.getCustomerRiskLevel(CUSTOMER_ID))
        .thenReturn(Optional.of(riskLevel));

    // Act
    final Optional<Boolean> canOperate =
        customerValidationService.validateCustomerCanOperate(CUSTOMER_ID);
    final Optional<ExternalCustomer.RiskLevel> retrievedRiskLevel =
        customerValidationService.getCustomerRiskLevel(CUSTOMER_ID);

    // Assert
    assertThat(canOperate).isPresent();
    assertThat(retrievedRiskLevel).isPresent().contains(riskLevel);

    // Validar lógica de negocio según el nivel de riesgo
    // Para customers activos, solo LOW y MEDIUM pueden operar
    switch (riskLevel) {
      case LOW, MEDIUM -> assertThat(canOperate.get()).isTrue();
      case HIGH, CRITICAL, BLOCKED -> assertThat(canOperate.get()).isFalse();
    }
  }

  @ParameterizedTest(
      name = "Comprehensive validation: active={0}, risk={1}, hasCompleteInfo={2}, expected={3}")
  @DisplayName("Should perform comprehensive risk validation with multiple criteria")
  @MethodSource("provideCombinedValidationData")
  void shouldPerformComprehensiveRiskValidation(
      final boolean active,
      final ExternalCustomer.RiskLevel riskLevel,
      final boolean hasCompleteInfo,
      final boolean expectedValidation) {
    // Arrange - Create customer with complete or incomplete contact info
    final ExternalCustomer.ExternalCustomerBuilder customerBuilder =
        ExternalCustomer.builder()
            .customerId(CUSTOMER_ID)
            .name("Comprehensive Test")
            .active(active)
            .riskLevel(riskLevel);

    if (hasCompleteInfo) {
      customerBuilder.email("complete@santander.com").phoneNumber("+34666123456");
    } else {
      customerBuilder.email(""); // Incomplete info
    }

    final ExternalCustomer customer = customerBuilder.build();

    when(externalCustomerService.getCustomerById(CUSTOMER_ID)).thenReturn(Optional.of(customer));
    // No se necesita stub del cache aquí - performComprehensiveRiskValidation no usa cache
    // directamente

    // Act
    final Optional<Boolean> result =
        customerValidationService.performComprehensiveRiskValidation(CUSTOMER_ID);

    // Assert
    assertThat(result).isPresent().contains(expectedValidation);
  }
}
