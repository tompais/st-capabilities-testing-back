package com.santandertecnologia.capabilitiestesting.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Modelo de dominio para producto. Demuestra el uso de BigDecimal, UUID, inner classes y métodos de
 * negocio.
 */
@Data
@Builder(toBuilder = true)
@Jacksonized
public class Product {

  private final UUID id;
  private final String name;
  private final String description;
  private final BigDecimal price;
  private final Category category;
  private final Integer stock;
  private final String sku;
  private final String brand;
  private final Double weight;
  private final String imageUrl;

  @Builder.Default private final Boolean active = true;

  @Builder.Default private final LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default private final LocalDateTime updatedAt = LocalDateTime.now();

  /** Verifica si el producto está activo. */
  public boolean isActive() {
    return Boolean.TRUE.equals(active);
  }

  /**
   * Verifica si el producto está disponible para venta. Un producto está disponible si está activo
   * y tiene stock.
   */
  public boolean isAvailable() {
    return Boolean.TRUE.equals(active) && stock != null && stock > 0;
  }

  /**
   * Calcula el precio con descuento aplicado.
   *
   * @param discountPercentage el porcentaje de descuento (0.10 = 10%)
   * @return el precio con descuento aplicado
   */
  public BigDecimal calculateDiscountedPrice(BigDecimal discountPercentage) {
    if (discountPercentage == null || discountPercentage.compareTo(BigDecimal.ZERO) == 0) {
      return price;
    }
    BigDecimal discount = price.multiply(discountPercentage);
    return price.subtract(discount);
  }

  /**
   * Reduce el stock del producto.
   *
   * @param quantity cantidad a reducir
   * @return nuevo producto con stock reducido
   */
  public Product reduceStock(int quantity) {
    int newStock = (stock != null) ? Math.max(0, stock - quantity) : 0;
    return this.toBuilder().stock(newStock).updatedAt(LocalDateTime.now()).build();
  }

  /**
   * Enum inner class para las categorías de producto. Demuestra el uso de inner classes como parte
   * de los requerimientos de testing.
   */
  public enum Category {
    ELECTRONICS,
    CLOTHING,
    BOOKS,
    SPORTS,
    HOME,
    OTHER
  }
}
