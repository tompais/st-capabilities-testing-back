package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.adapter;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ProductRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mapper.ProductMapper;
import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mongodb.repository.SpringDataProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adaptador que implementa el puerto de salida ProductRepository usando MongoDB. */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

  private final SpringDataProductRepository springDataProductRepository;
  private final ProductMapper productMapper;

  @Override
  public Product save(final Product product) {
    final var entity = productMapper.toEntity(product);
    final var savedEntity = springDataProductRepository.save(entity);
    return productMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<Product> findById(final UUID id) {
    return springDataProductRepository.findById(id).map(productMapper::toDomain);
  }

  @Override
  public List<Product> findByCategory(final Product.Category category) {
    final var entityCategory = productMapper.toEntityCategory(category);
    return springDataProductRepository.findByCategory(entityCategory).stream()
        .map(productMapper::toDomain)
        .toList();
  }

  @Override
  public List<Product> findActiveProducts() {
    return springDataProductRepository.findByActiveTrue().stream()
        .map(productMapper::toDomain)
        .toList();
  }

  @Override
  public void deleteById(final UUID id) {
    springDataProductRepository.deleteById(id);
  }

  @Override
  public void deleteAll() {
    springDataProductRepository.deleteAll();
  }

  @Override
  public boolean existsBySku(final String sku) {
    return springDataProductRepository.existsBySku(sku);
  }
}
