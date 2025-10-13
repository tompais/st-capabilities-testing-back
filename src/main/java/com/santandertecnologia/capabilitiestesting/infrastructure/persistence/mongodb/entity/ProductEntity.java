package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mongodb.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Entidad MongoDB para productos. */
@Document(collection = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

  @Id private UUID id;

  private String name;
  private String description;
  private BigDecimal price;
  private ProductCategory category;
  private Integer stock;
  private String sku;
  private String brand;
  private Double weight;
  private String imageUrl;

  @Builder.Default private Boolean active = true;

  @Builder.Default private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default private LocalDateTime updatedAt = LocalDateTime.now();

  public boolean isAvailable() {
    return Boolean.TRUE.equals(active) && stock != null && stock > 0;
  }

  public enum ProductCategory {
    ELECTRONICS,
    CLOTHING,
    BOOKS,
    SPORTS,
    HOME,
    OTHER
  }
}
