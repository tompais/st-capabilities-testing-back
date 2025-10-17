package com.santandertecnologia.capabilitiestesting.application.service;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.port.in.ProductUseCase;
import com.santandertecnologia.capabilitiestesting.domain.port.out.CacheService;
import com.santandertecnologia.capabilitiestesting.domain.port.out.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación para gestión de productos. Implementa Optional para evitar manejo directo
 * de nulls. Usa UUID para identificación y Product.Category como inner class.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ProductService implements ProductUseCase {

  private static final String PRODUCT_CACHE_PREFIX = "product:";
  private static final long CACHE_TTL_SECONDS = 600; // 10 minutos
  private final ProductRepository productRepository;
  private final CacheService cacheService;

  @Override
  public Product createProduct(final Product product) {
    log.info("Creating new product with name: {}", product.getName());

    // Validar SKU si se proporciona
    if (product.getSku() != null && productRepository.existsBySku(product.getSku())) {
      throw new IllegalArgumentException("SKU already exists: " + product.getSku());
    }

    // Generar UUID si no se proporciona
    final Product productToSave =
        product.getId() == null
            ? Product.builder()
                .id(UUID.randomUUID())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .sku(product.getSku())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build()
            : product;

    final Product savedProduct = productRepository.save(productToSave);
    cacheProduct(savedProduct);

    log.info("Product created successfully with ID: {}", savedProduct.getId());
    return savedProduct;
  }

  @Override
  public Optional<Product> getProductById(final UUID id) {
    log.debug("Getting product by ID: {}", id);

    // Intentar obtener del caché primero
    final Optional<Product> cachedProduct =
        cacheService.get(PRODUCT_CACHE_PREFIX + id, Product.class);
    if (cachedProduct.isPresent()) {
      log.debug("Product found in cache with ID: {}", id);
      return cachedProduct;
    }

    // Si no está en caché, buscar en repositorio
    final Optional<Product> product = productRepository.findById(id);
    product.ifPresent(this::cacheProduct);

    return product;
  }

  @Override
  public List<Product> getProductsByCategory(final Product.Category category) {
    log.debug("Getting products by category: {}", category);
    return productRepository.findByCategory(category);
  }

  @Override
  public Optional<Product> updateProductStock(final UUID id, final Integer newStock) {
    log.info("Updating stock for product ID: {} to: {}", id, newStock);

    return productRepository
        .findById(id)
        .map(
            existingProduct -> {
              // Actualizar el estado activo basado en el stock: si stock es 0, marcar como inactivo
              final boolean isActive = newStock != null && newStock > 0;

              final Product updatedProduct =
                  Product.builder()
                      .id(existingProduct.getId())
                      .name(existingProduct.getName())
                      .description(existingProduct.getDescription())
                      .price(existingProduct.getPrice())
                      .category(existingProduct.getCategory())
                      .active(isActive) // Actualizar estado basado en stock
                      .stock(newStock)
                      .sku(existingProduct.getSku())
                      .brand(existingProduct.getBrand())
                      .weight(existingProduct.getWeight())
                      .imageUrl(existingProduct.getImageUrl())
                      .createdAt(existingProduct.getCreatedAt())
                      .updatedAt(LocalDateTime.now())
                      .build();

              final Product savedProduct = productRepository.save(updatedProduct);
              cacheProduct(savedProduct);

              log.info(
                  "Product stock updated successfully for ID: {}. Active status: {}", id, isActive);
              return savedProduct;
            });
  }

  @Override
  public boolean deleteProduct(final UUID id) {
    log.info("Deleting product with ID: {}", id);

    return productRepository
        .findById(id)
        .map(
            product -> {
              productRepository.deleteById(id);
              cacheService.evict(PRODUCT_CACHE_PREFIX + id);
              log.info("Product deleted successfully with ID: {}", id);
              return true;
            })
        .orElseGet(
            () -> {
              log.warn("Product not found for deletion with ID: {}", id);
              return false;
            });
  }

  @Override
  public List<Product> getActiveProducts() {
    log.debug("Getting active products");
    return productRepository.findActiveProducts();
  }

  @Override
  public Optional<Product> updateStock(final UUID id, final Integer newStock) {
    log.debug("Updating stock for product ID: {} (delegating to updateProductStock)", id);
    return updateProductStock(id, newStock);
  }

  private boolean validateStock(final UUID id, final Integer newStock) {
    return newStock != null && newStock >= 0;
  }

  private void cacheProduct(final Product product) {
    if (product != null && product.getId() != null) {
      cacheService.put(PRODUCT_CACHE_PREFIX + product.getId(), product, CACHE_TTL_SECONDS);
    }
  }
}
