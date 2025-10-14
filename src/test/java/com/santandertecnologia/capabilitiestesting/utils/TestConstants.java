package com.santandertecnologia.capabilitiestesting.utils;

import java.util.UUID;

/**
 * Constantes compartidas para todos los tests. Centraliza valores comunes como IDs, emails,
 * nombres, etc.
 */
public final class TestConstants {

  // User test constants
  public static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  public static final UUID USER_ID_2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
  public static final String USER_USERNAME = "testuser";
  public static final String USER_EMAIL = "test@santander.com";
  public static final String USER_FIRST_NAME = "Test";
  public static final String USER_LAST_NAME = "User";
  public static final String USER_PHONE = "+34666123456";
  public static final String USER_DEPARTMENT = "Testing";
  // Product test constants
  public static final UUID PRODUCT_ID = UUID.fromString("223e4567-e89b-12d3-a456-426614174000");
  public static final String PRODUCT_NAME = "Test Product";
  public static final String PRODUCT_DESCRIPTION = "Test product description";
  public static final String PRODUCT_SKU = "TEST-SKU-001";
  // Customer test constants
  public static final UUID CUSTOMER_ID = UUID.fromString("323e4567-e89b-12d3-a456-426614174000");
  public static final String CUSTOMER_NAME = "Test Customer";
  public static final String CUSTOMER_EMAIL = "customer@santander.com";
  // Cache keys
  public static final String CACHE_KEY_USER_PREFIX = "user:";
  public static final String CACHE_KEY_PRODUCT_PREFIX = "product:";

  private TestConstants() {
    // Utility class - prevent instantiation
  }
}
