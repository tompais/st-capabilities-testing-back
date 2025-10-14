package com.santandertecnologia.capabilitiestesting.utils;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.NoArgsConstructor;

/**
 * Utilidades para crear objetos mock consistentes en tests. Centraliza la creación de objetos de
 * prueba con valores por defecto.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class MockUtils {

  /**
   * Crea un User mock con valores por defecto.
   *
   * @return User con datos de prueba estándar
   */
  public static User mockUser() {
    return mockUser(TestConstants.USER_ID);
  }

  /**
   * Crea un User mock con un ID específico.
   *
   * @param id UUID del usuario
   * @return User con el ID especificado y demás datos por defecto
   */
  public static User mockUser(UUID id) {
    return User.builder()
        .id(id)
        .username(TestConstants.USER_USERNAME)
        .email(TestConstants.USER_EMAIL)
        .firstName(TestConstants.USER_FIRST_NAME)
        .lastName(TestConstants.USER_LAST_NAME)
        .phoneNumber(TestConstants.USER_PHONE)
        .department(TestConstants.USER_DEPARTMENT)
        .status(User.Status.ACTIVE)
        .build();
  }

  /**
   * Crea un User mock con status específico.
   *
   * @param status Status del usuario
   * @return User con el status especificado y demás datos por defecto
   */
  public static User mockUser(User.Status status) {
    return User.builder()
        .id(TestConstants.USER_ID)
        .username(TestConstants.USER_USERNAME)
        .email(TestConstants.USER_EMAIL)
        .firstName(TestConstants.USER_FIRST_NAME)
        .lastName(TestConstants.USER_LAST_NAME)
        .phoneNumber(TestConstants.USER_PHONE)
        .department(TestConstants.USER_DEPARTMENT)
        .status(status)
        .build();
  }

  /**
   * Crea un User mock con parámetros personalizados.
   *
   * @param id UUID del usuario
   * @param username nombre de usuario
   * @param email email del usuario
   * @return User con los parámetros especificados
   */
  public static User mockUser(UUID id, String username, String email) {
    return User.builder()
        .id(id)
        .username(username)
        .email(email)
        .firstName(TestConstants.USER_FIRST_NAME)
        .lastName(TestConstants.USER_LAST_NAME)
        .phoneNumber(TestConstants.USER_PHONE)
        .department(TestConstants.USER_DEPARTMENT)
        .status(User.Status.ACTIVE)
        .build();
  }

  /**
   * Crea un Product mock con valores por defecto.
   *
   * @return Product con datos de prueba estándar
   */
  public static Product mockProduct() {
    return mockProduct(TestConstants.PRODUCT_ID);
  }

  /**
   * Crea un Product mock con un ID específico.
   *
   * @param id UUID del producto
   * @return Product con el ID especificado y demás datos por defecto
   */
  public static Product mockProduct(UUID id) {
    return Product.builder()
        .id(id)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .sku(TestConstants.PRODUCT_SKU)
        .price(BigDecimal.valueOf(99.99))
        .stock(10)
        .category(Product.Category.ELECTRONICS)
        .active(true)
        .build();
  }

  /**
   * Crea un Product mock con categoría específica.
   *
   * @param category Categoría del producto
   * @return Product con la categoría especificada y demás datos por defecto
   */
  public static Product mockProduct(Product.Category category) {
    return Product.builder()
        .id(TestConstants.PRODUCT_ID)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .sku(TestConstants.PRODUCT_SKU)
        .price(BigDecimal.valueOf(99.99))
        .stock(10)
        .category(category)
        .active(true)
        .build();
  }

  /**
   * Crea un ExternalCustomer mock con valores por defecto.
   *
   * @return ExternalCustomer con datos de prueba estándar
   */
  public static ExternalCustomer mockExternalCustomer() {
    return mockExternalCustomer(TestConstants.CUSTOMER_ID);
  }

  /**
   * Crea un ExternalCustomer mock con un ID específico.
   *
   * @param id UUID del cliente
   * @return ExternalCustomer con el ID especificado y demás datos por defecto
   */
  public static ExternalCustomer mockExternalCustomer(UUID id) {
    return ExternalCustomer.builder()
        .customerId(id)
        .name(TestConstants.CUSTOMER_NAME)
        .email(TestConstants.CUSTOMER_EMAIL)
        .active(true)
        .riskLevel(ExternalCustomer.RiskLevel.LOW)
        .build();
  }

  /**
   * Crea un ExternalCustomer mock con nivel de riesgo específico.
   *
   * @param riskLevel Nivel de riesgo del cliente
   * @return ExternalCustomer con el nivel de riesgo especificado y demás datos por defecto
   */
  public static ExternalCustomer mockExternalCustomer(ExternalCustomer.RiskLevel riskLevel) {
    return ExternalCustomer.builder()
        .customerId(TestConstants.CUSTOMER_ID)
        .name(TestConstants.CUSTOMER_NAME)
        .email(TestConstants.CUSTOMER_EMAIL)
        .active(true)
        .riskLevel(riskLevel)
        .build();
  }

  /**
   * Crea un ExternalCustomer mock con nivel de riesgo y estado activo personalizados.
   *
   * @param riskLevel Nivel de riesgo del cliente
   * @param active Si el cliente está activo o no
   * @return ExternalCustomer con los parámetros especificados
   */
  public static ExternalCustomer mockExternalCustomer(
      ExternalCustomer.RiskLevel riskLevel, boolean active) {
    return ExternalCustomer.builder()
        .customerId(TestConstants.CUSTOMER_ID)
        .name(TestConstants.CUSTOMER_NAME)
        .email(TestConstants.CUSTOMER_EMAIL)
        .active(active)
        .riskLevel(riskLevel)
        .build();
  }

  /**
   * Crea un ExternalCustomer mock completamente parametrizado.
   *
   * @param id UUID del cliente
   * @param name Nombre del cliente
   * @param email Email del cliente
   * @param active Si el cliente está activo
   * @param riskLevel Nivel de riesgo
   * @return ExternalCustomer con todos los parámetros especificados
   */
  public static ExternalCustomer mockExternalCustomer(
      UUID id, String name, String email, boolean active, ExternalCustomer.RiskLevel riskLevel) {
    return ExternalCustomer.builder()
        .customerId(id)
        .name(name)
        .email(email)
        .active(active)
        .riskLevel(riskLevel)
        .build();
  }

  /**
   * Crea un UserResponse mock con valores por defecto.
   *
   * @return UserResponse con datos de prueba estándar
   */
  public static UserResponse mockUserResponse() {
    return mockUserResponse(TestConstants.USER_ID);
  }

  /**
   * Crea un UserResponse mock con un ID específico.
   *
   * @param id UUID del usuario
   * @return UserResponse con el ID especificado y demás datos por defecto
   */
  public static UserResponse mockUserResponse(UUID id) {
    return UserResponse.builder()
        .id(id)
        .email(TestConstants.USER_EMAIL)
        .name(TestConstants.USER_FIRST_NAME + " " + TestConstants.USER_LAST_NAME)
        .phone(TestConstants.USER_PHONE)
        .active(true)
        .build();
  }

  /**
   * Crea un UserResponse mock con estado activo específico.
   *
   * @param active Si el usuario está activo o no
   * @return UserResponse con el estado especificado y demás datos por defecto
   */
  public static UserResponse mockUserResponse(boolean active) {
    return UserResponse.builder()
        .id(TestConstants.USER_ID)
        .email(TestConstants.USER_EMAIL)
        .name(TestConstants.USER_FIRST_NAME + " " + TestConstants.USER_LAST_NAME)
        .phone(TestConstants.USER_PHONE)
        .active(active)
        .build();
  }

  /**
   * Crea un UserResponse mock completamente parametrizado.
   *
   * @param id UUID del usuario
   * @param email Email del usuario
   * @param name Nombre completo del usuario
   * @param phone Teléfono del usuario
   * @param active Si el usuario está activo
   * @return UserResponse con todos los parámetros especificados
   */
  public static UserResponse mockUserResponse(
      UUID id, String email, String name, String phone, boolean active) {
    return UserResponse.builder()
        .id(id)
        .email(email)
        .name(name)
        .phone(phone)
        .active(active)
        .build();
  }

  /**
   * Crea un CreateUserRequest mock con valores por defecto.
   *
   * @return CreateUserRequest con datos de prueba estándar
   */
  public static CreateUserRequest mockCreateUserRequest() {
    return CreateUserRequest.builder()
        .username(TestConstants.USER_USERNAME)
        .email(TestConstants.USER_EMAIL)
        .firstName(TestConstants.USER_FIRST_NAME)
        .lastName(TestConstants.USER_LAST_NAME)
        .phoneNumber(TestConstants.USER_PHONE)
        .department(TestConstants.USER_DEPARTMENT)
        .build();
  }

  /**
   * Crea un CreateUserRequest mock con username y email específicos.
   *
   * @param username Nombre de usuario
   * @param email Email del usuario
   * @return CreateUserRequest con los parámetros especificados
   */
  public static CreateUserRequest mockCreateUserRequest(String username, String email) {
    return CreateUserRequest.builder()
        .username(username)
        .email(email)
        .firstName(TestConstants.USER_FIRST_NAME)
        .lastName(TestConstants.USER_LAST_NAME)
        .phoneNumber(TestConstants.USER_PHONE)
        .department(TestConstants.USER_DEPARTMENT)
        .build();
  }

  /**
   * Crea un CreateUserRequest mock completamente parametrizado.
   *
   * @param username Nombre de usuario
   * @param email Email del usuario
   * @param firstName Nombre
   * @param lastName Apellido
   * @param phoneNumber Teléfono
   * @param department Departamento
   * @return CreateUserRequest con todos los parámetros especificados
   */
  public static CreateUserRequest mockCreateUserRequest(
      String username,
      String email,
      String firstName,
      String lastName,
      String phoneNumber,
      String department) {
    return CreateUserRequest.builder()
        .username(username)
        .email(email)
        .firstName(firstName)
        .lastName(lastName)
        .phoneNumber(phoneNumber)
        .department(department)
        .build();
  }
}
