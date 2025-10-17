package com.santandertecnologia.capabilitiestesting.utils;

import com.santandertecnologia.capabilitiestesting.domain.model.ExternalCustomer;
import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateProductRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.ProductResponse;
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

  private static final String SKU_PREFIX = "SKU-";
  private static final String HYPHEN = "-";

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
  public static User mockUser(final UUID id) {
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
   * Crea un User mock con status específico y ID único.
   *
   * @param status Status del usuario
   * @return User con el status especificado, ID único y demás datos por defecto
   */
  public static User mockUser(final User.Status status) {
    final UUID uniqueId = UUID.randomUUID();
    return User.builder()
        .id(uniqueId)
        .username(TestConstants.USER_USERNAME + HYPHEN + uniqueId.toString().substring(0, 8))
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
  public static User mockUser(final UUID id, final String username, final String email) {
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
   * Crea un User mock con parámetros personalizados incluyendo status.
   *
   * @param id UUID del usuario
   * @param username nombre de usuario
   * @param email email del usuario
   * @param status Status del usuario
   * @return User con los parámetros especificados
   */
  public static User mockUser(
      final UUID id, final String username, final String email, final User.Status status) {
    return User.builder()
        .id(id)
        .username(username)
        .email(email)
        .firstName(TestConstants.USER_FIRST_NAME)
        .lastName(TestConstants.USER_LAST_NAME)
        .phoneNumber(TestConstants.USER_PHONE)
        .department(TestConstants.USER_DEPARTMENT)
        .status(status)
        .build();
  }

  /**
   * Crea un User mock completamente parametrizado.
   *
   * @param id UUID del usuario
   * @param username nombre de usuario
   * @param email email del usuario
   * @param firstName Nombre
   * @param lastName Apellido
   * @param phoneNumber Teléfono
   * @param department Departamento
   * @param status Status del usuario
   * @return User con todos los parámetros especificados
   */
  public static User mockUser(
      final UUID id,
      final String username,
      final String email,
      final String firstName,
      final String lastName,
      final String phoneNumber,
      final String department,
      final User.Status status) {
    return User.builder()
        .id(id)
        .username(username)
        .email(email)
        .firstName(firstName)
        .lastName(lastName)
        .phoneNumber(phoneNumber)
        .department(department)
        .status(status)
        .build();
  }

  /**
   * Crea un Product mock con valores por defecto y SKU único. TODOS los productos creados tendrán
   * SKU único para evitar conflictos en tests.
   *
   * @return Product con datos de prueba estándar, ID y SKU únicos
   */
  public static Product mockProduct() {
    final UUID uniqueId = UUID.randomUUID();
    return Product.builder()
        .id(uniqueId)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .sku(SKU_PREFIX + uniqueId.toString().substring(0, 8).toUpperCase())
        .price(BigDecimal.valueOf(99.99))
        .stock(10)
        .category(Product.Category.ELECTRONICS)
        .active(true)
        .build();
  }

  /**
   * Crea un Product mock con un ID específico. Útil cuando necesitas controlar el ID del producto
   * en tests.
   *
   * @param id UUID del producto
   * @return Product con el ID especificado y demás datos por defecto
   */
  public static Product mockProduct(final UUID id) {
    return Product.builder()
        .id(id)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .sku(SKU_PREFIX + id.toString().substring(0, 8).toUpperCase())
        .price(BigDecimal.valueOf(99.99))
        .stock(10)
        .category(Product.Category.ELECTRONICS)
        .active(true)
        .build();
  }

  /**
   * Crea un Product mock con categoría específica y SKU único.
   *
   * @param category Categoría del producto
   * @return Product con la categoría especificada, ID y SKU únicos
   */
  public static Product mockProduct(final Product.Category category) {
    final UUID uniqueId = UUID.randomUUID();
    return Product.builder()
        .id(uniqueId)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .sku(
            SKU_PREFIX
                + category.name()
                + HYPHEN
                + uniqueId.toString().substring(0, 8).toUpperCase())
        .price(BigDecimal.valueOf(99.99))
        .stock(10)
        .category(category)
        .active(true)
        .build();
  }

  /**
   * Crea un Product mock con estado activo específico y SKU único.
   *
   * @param active Si el producto está activo o no
   * @return Product con el estado especificado, ID y SKU únicos
   */
  public static Product mockProduct(final boolean active) {
    final UUID uniqueId = UUID.randomUUID();
    return Product.builder()
        .id(uniqueId)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .sku(SKU_PREFIX + uniqueId.toString().substring(0, 8).toUpperCase())
        .price(BigDecimal.valueOf(99.99))
        .stock(active ? 10 : 0)
        .category(Product.Category.ELECTRONICS)
        .active(active)
        .build();
  }

  /**
   * Crea un Product mock completamente parametrizado con ID y SKU únicos.
   *
   * @param name Nombre del producto
   * @param description Descripción del producto
   * @param price Precio del producto
   * @param category Categoría del producto
   * @param stock Stock disponible
   * @param active Si el producto está activo
   * @return Product con todos los parámetros especificados y ID/SKU únicos
   */
  public static Product mockProduct(
      final String name,
      final String description,
      final BigDecimal price,
      final Product.Category category,
      final Integer stock,
      final boolean active) {
    final UUID uniqueId = UUID.randomUUID();
    return Product.builder()
        .id(uniqueId)
        .name(name)
        .description(description)
        .sku(
            SKU_PREFIX
                + category.name()
                + HYPHEN
                + uniqueId.toString().substring(0, 8).toUpperCase())
        .price(price)
        .stock(stock)
        .category(category)
        .active(active)
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
  public static ExternalCustomer mockExternalCustomer(final UUID id) {
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
  public static ExternalCustomer mockExternalCustomer(final ExternalCustomer.RiskLevel riskLevel) {
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
      final ExternalCustomer.RiskLevel riskLevel, final boolean active) {
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
      final UUID id,
      final String name,
      final String email,
      final boolean active,
      final ExternalCustomer.RiskLevel riskLevel) {
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
  public static UserResponse mockUserResponse(final UUID id) {
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
  public static UserResponse mockUserResponse(final boolean active) {
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
      final UUID id,
      final String email,
      final String name,
      final String phone,
      final boolean active) {
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
  public static CreateUserRequest mockCreateUserRequest(final String username, final String email) {
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
      final String username,
      final String email,
      final String firstName,
      final String lastName,
      final String phoneNumber,
      final String department) {
    return CreateUserRequest.builder()
        .username(username)
        .email(email)
        .firstName(firstName)
        .lastName(lastName)
        .phoneNumber(phoneNumber)
        .department(department)
        .build();
  }

  /**
   * Crea un ProductResponse mock con valores por defecto.
   *
   * @return ProductResponse con datos de prueba estándar
   */
  public static ProductResponse mockProductResponse() {
    return mockProductResponse(TestConstants.PRODUCT_ID);
  }

  /**
   * Crea un ProductResponse mock con un ID específico.
   *
   * @param id UUID del producto
   * @return ProductResponse con el ID especificado y demás datos por defecto
   */
  public static ProductResponse mockProductResponse(final UUID id) {
    return ProductResponse.builder()
        .id(id)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .price(BigDecimal.valueOf(99.99))
        .category(Product.Category.ELECTRONICS)
        .stock(10)
        .available(true)
        .build();
  }

  /**
   * Crea un ProductResponse mock con disponibilidad específica.
   *
   * @param available Si el producto está disponible o no
   * @return ProductResponse con la disponibilidad especificada y demás datos por defecto
   */
  public static ProductResponse mockProductResponse(final boolean available) {
    return ProductResponse.builder()
        .id(TestConstants.PRODUCT_ID)
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .price(BigDecimal.valueOf(99.99))
        .category(Product.Category.ELECTRONICS)
        .stock(available ? 10 : 0)
        .available(available)
        .build();
  }

  /**
   * Crea un ProductResponse mock completamente parametrizado.
   *
   * @param id UUID del producto
   * @param name Nombre del producto
   * @param price Precio del producto
   * @param stock Stock disponible
   * @param available Si el producto está disponible
   * @return ProductResponse con todos los parámetros especificados
   */
  public static ProductResponse mockProductResponse(
      final UUID id,
      final String name,
      final BigDecimal price,
      final Integer stock,
      final boolean available) {
    return ProductResponse.builder()
        .id(id)
        .name(name)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .price(price)
        .category(Product.Category.ELECTRONICS)
        .stock(stock)
        .available(available)
        .build();
  }

  /**
   * Crea un CreateProductRequest mock con valores por defecto.
   *
   * @return CreateProductRequest con datos de prueba estándar
   */
  public static CreateProductRequest mockCreateProductRequest() {
    return CreateProductRequest.builder()
        .name(TestConstants.PRODUCT_NAME)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .price(BigDecimal.valueOf(99.99))
        .category(Product.Category.ELECTRONICS)
        .stock(10)
        .sku(TestConstants.PRODUCT_SKU)
        .build();
  }

  /**
   * Crea un CreateProductRequest mock con nombre y precio específicos.
   *
   * @param name Nombre del producto
   * @param price Precio del producto
   * @return CreateProductRequest con los parámetros especificados
   */
  public static CreateProductRequest mockCreateProductRequest(
      final String name, final BigDecimal price) {
    return CreateProductRequest.builder()
        .name(name)
        .description(TestConstants.PRODUCT_DESCRIPTION)
        .price(price)
        .category(Product.Category.ELECTRONICS)
        .stock(10)
        .sku(TestConstants.PRODUCT_SKU)
        .build();
  }

  /**
   * Crea un CreateProductRequest mock completamente parametrizado.
   *
   * @param name Nombre del producto
   * @param description Descripción del producto
   * @param price Precio del producto
   * @param category Categoría del producto
   * @param stock Stock disponible
   * @param sku SKU del producto
   * @return CreateProductRequest con todos los parámetros especificados
   */
  public static CreateProductRequest mockCreateProductRequest(
      final String name,
      final String description,
      final BigDecimal price,
      final Product.Category category,
      final Integer stock,
      final String sku) {
    return CreateProductRequest.builder()
        .name(name)
        .description(description)
        .price(price)
        .category(category)
        .stock(stock)
        .sku(sku)
        .build();
  }
}
