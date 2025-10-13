package com.santandertecnologia.capabilitiestesting.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.port.out.CacheService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ExternalCustomerService;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Tests parametrizados para CustomerValidationService. Enfocado en testear LÓGICA DE NEGOCIO real
 * con diferentes combinaciones de datos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CustomerValidationService Parameterized Tests")
class CustomerValidationServiceParameterizedTest {

  @Mock private ExternalCustomerService externalCustomerService;
  @Mock private CacheService cacheService;

  @InjectMocks private CustomerValidationService customerValidationService;

  private UUID customerId;

  /** Proveedor de datos para validación comprehensiva. Combina múltiples factores de riesgo. */
  private static Stream<Arguments> provideComprehensiveValidationData() {
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

  /** Proveedor de datos para testing de summaries. */
  private static Stream<Arguments> provideCustomerStateData() {
    return Stream.of(
        Arguments.of(
            ExternalCustomer.RiskLevel.LOW, true, ".*Active=true.*Risk=LOW.*CanOperate=true.*"),
        Arguments.of(
            ExternalCustomer.RiskLevel.HIGH, true, ".*Active=true.*Risk=HIGH.*CanOperate=false.*"),
        Arguments.of(
            ExternalCustomer.RiskLevel.LOW, false, ".*Active=false.*Risk=LOW.*CanOperate=false.*"),
        Arguments.of(
            ExternalCustomer.RiskLevel.CRITICAL,
            false,
            ".*Active=false.*Risk=CRITICAL.*CanOperate=false.*"));
  }

  @BeforeEach
  void setUp() {
    customerId = UUID.randomUUID();
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
    "CRITICAL, false, false"
  })
  void shouldValidateCustomerOperationPermissions(
      ExternalCustomer.RiskLevel riskLevel, boolean active, boolean expectedCanOperate) {
    // Arrange
    ExternalCustomer customer =
        ExternalCustomer.builder()
            .customerId(customerId)
            .name("Test Customer")
            .email("test@santander.com")
            .active(active)
            .riskLevel(riskLevel)
            .build();

    when(cacheService.get(any(String.class), eq(ExternalCustomer.class)))
        .thenReturn(Optional.empty());
    when(externalCustomerService.getCustomerById(customerId)).thenReturn(Optional.of(customer));

    // Act
    Optional<Boolean> result = customerValidationService.validateCustomerCanOperate(customerId);

    // Assert
    assertThat(result).isPresent().contains(expectedCanOperate);
  }

  @ParameterizedTest
  @DisplayName("Should test all risk levels have correct validation logic")
  @EnumSource(ExternalCustomer.RiskLevel.class)
  void shouldTestRiskLevelValidationLogic(ExternalCustomer.RiskLevel riskLevel) {
    // Arrange - Customer activo con diferentes niveles de riesgo
    ExternalCustomer customer =
        ExternalCustomer.builder()
            .customerId(customerId)
            .name("Risk Test Customer")
            .email("risk@santander.com")
            .active(true)
            .riskLevel(riskLevel)
            .build();

    when(cacheService.get(any(String.class), eq(ExternalCustomer.class)))
        .thenReturn(Optional.empty());
    when(externalCustomerService.getCustomerById(customerId)).thenReturn(Optional.of(customer));

    // Act
    Optional<Boolean> canOperate = customerValidationService.validateCustomerCanOperate(customerId);
    Optional<ExternalCustomer.RiskLevel> retrievedRiskLevel =
        customerValidationService.getCustomerRiskLevel(customerId);

    // Assert - Lógica específica por nivel de riesgo
    assertThat(canOperate).isPresent();
    assertThat(retrievedRiskLevel).isPresent().contains(riskLevel);

    switch (riskLevel) {
      case LOW, MEDIUM -> assertThat(canOperate.get()).isTrue();
      case HIGH, CRITICAL, BLOCKED -> assertThat(canOperate.get()).isFalse();
      default -> throw new IllegalArgumentException("Unexpected risk level: " + riskLevel);
    }
  }

  @ParameterizedTest(
      name = "Comprehensive validation: active={0}, risk={1}, hasCompleteInfo={2}, expected={3}")
  @DisplayName("Should perform comprehensive risk validation with multiple criteria")
  @MethodSource("provideComprehensiveValidationData")
  void shouldPerformComprehensiveRiskValidation(
      boolean active,
      ExternalCustomer.RiskLevel riskLevel,
      boolean hasCompleteInfo,
      boolean expectedValidation) {
    // Arrange - Create customer with complete or incomplete contact info
    ExternalCustomer.ExternalCustomerBuilder customerBuilder =
        ExternalCustomer.builder()
            .customerId(customerId)
            .name("Comprehensive Test")
            .active(active)
            .riskLevel(riskLevel);

    if (hasCompleteInfo) {
      customerBuilder.email("complete@santander.com").phoneNumber("+34666123456");
    } else {
      customerBuilder.email(""); // Incomplete info
    }

    ExternalCustomer customer = customerBuilder.build();

    when(cacheService.get(any(String.class), eq(ExternalCustomer.class)))
        .thenReturn(Optional.empty());
    when(externalCustomerService.getCustomerById(customerId)).thenReturn(Optional.of(customer));

    // Act
    Optional<Boolean> result =
        customerValidationService.performComprehensiveRiskValidation(customerId);

    // Assert
    assertThat(result).isPresent().contains(expectedValidation);
  }

  @ParameterizedTest(name = "Cache behavior: inCache={0}, inService={1}")
  @DisplayName("Should test cache behavior with different scenarios")
  @CsvSource({
    "true, true", // En cache, no debe llamar servicio
    "false, true", // No en cache, debe llamar servicio
    "false, false" // No en cache, servicio no encuentra
  })
  void shouldTestCacheBehaviorScenarios(boolean customerInCache, boolean customerInService) {
    // Arrange
    ExternalCustomer customer =
        ExternalCustomer.builder()
            .customerId(customerId)
            .name("Cache Test")
            .email("cache@santander.com")
            .active(true)
            .riskLevel(ExternalCustomer.RiskLevel.LOW)
            .build();

    // Configurar cache siempre para evitar unnecessary stubbing
    if (customerInCache) {
      when(cacheService.get(any(String.class), eq(ExternalCustomer.class)))
          .thenReturn(Optional.of(customer));
    } else {
      when(cacheService.get(any(String.class), eq(ExternalCustomer.class)))
          .thenReturn(Optional.empty());
    }

    // Configurar servicio externo siempre para evitar unnecessary stubbing
    if (customerInService) {
      when(externalCustomerService.getCustomerById(customerId)).thenReturn(Optional.of(customer));
    } else {
      when(externalCustomerService.getCustomerById(customerId)).thenReturn(Optional.empty());
    }

    // Act
    Optional<ExternalCustomer> result = customerValidationService.getCustomerInfo(customerId);

    // Assert
    if (customerInCache || customerInService) {
      assertThat(result).isPresent();
      assertThat(result.get().customerId()).isEqualTo(customerId);
    } else {
      assertThat(result).isEmpty();
    }
  }

  @ParameterizedTest(name = "Status summary for risk={0} and active={1}")
  @DisplayName("Should generate correct status summaries for different customer states")
  @MethodSource("provideCustomerStateData")
  void shouldGenerateCorrectStatusSummaries(
      ExternalCustomer.RiskLevel riskLevel, boolean active, String expectedSummaryPattern) {
    // Arrange
    ExternalCustomer customer =
        ExternalCustomer.builder()
            .customerId(customerId)
            .name("Summary Test")
            .email("summary@santander.com")
            .active(active)
            .riskLevel(riskLevel)
            .build();

    when(cacheService.get(any(String.class), eq(ExternalCustomer.class)))
        .thenReturn(Optional.empty());
    when(externalCustomerService.getCustomerById(customerId)).thenReturn(Optional.of(customer));

    // Act
    Optional<String> summary = customerValidationService.getCustomerStatusSummary(customerId);

    // Assert
    assertThat(summary).isPresent();
    assertThat(summary.get()).contains(customerId.toString());
    assertThat(summary.get()).contains("Active=" + active);
    assertThat(summary.get()).contains("Risk=" + riskLevel);
    assertThat(summary.get()).matches(expectedSummaryPattern);
  }
}
