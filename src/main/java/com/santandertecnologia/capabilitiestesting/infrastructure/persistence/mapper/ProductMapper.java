package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mapper;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mongodb.entity.ProductEntity;
import org.springframework.stereotype.Component;

/** Mapper para conversión entre Product (dominio) y ProductEntity (infraestructura). */
@Component
public class ProductMapper {

  /**
   * Convierte de entidad de dominio a entidad de infraestructura.
   *
   * @param product modelo de dominio
   * @return entidad MongoDB
   */
  public ProductEntity toEntity(final Product product) {
    if (product == null) {
      return null;
    }

    return ProductEntity.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .category(toEntityCategory(product.getCategory()))
        .stock(product.getStock())
        .sku(product.getSku())
        .brand(product.getBrand())
        .weight(product.getWeight())
        .imageUrl(product.getImageUrl())
        .active(product.getActive())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }

  /**
   * Convierte de entidad de infraestructura a entidad de dominio.
   *
   * @param entity entidad MongoDB
   * @return modelo de dominio
   */
  public Product toDomain(final ProductEntity entity) {
    if (entity == null) {
      return null;
    }

    return Product.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .price(entity.getPrice())
        .category(toDomainCategory(entity.getCategory()))
        .stock(entity.getStock())
        .sku(entity.getSku())
        .brand(entity.getBrand())
        .weight(entity.getWeight())
        .imageUrl(entity.getImageUrl())
        .active(entity.getActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  /** Convierte de enum de dominio a enum de entidad. */
  public ProductEntity.ProductCategory toEntityCategory(final Product.Category domainCategory) {
    if (domainCategory == null) {
      return null;
    }
    return ProductEntity.ProductCategory.valueOf(domainCategory.name());
  }

  /** Convierte de enum de entidad a enum de dominio. */
  public Product.Category toDomainCategory(final ProductEntity.ProductCategory entityCategory) {
    if (entityCategory == null) {
      return null;
    }
    return Product.Category.valueOf(entityCategory.name());
  }
}
