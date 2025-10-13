package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mongodb.repository;

import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mongodb.entity.ProductEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Repositorio MongoDB para operaciones de persistencia de productos. */
@Repository
public interface SpringDataProductRepository extends MongoRepository<ProductEntity, UUID> {

  List<ProductEntity> findByCategory(ProductEntity.ProductCategory category);

  List<ProductEntity> findByActiveTrue();

  boolean existsBySku(String sku);
}
