package com.santandertecnologia.capabilitiestesting.infrastructure.web.service;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import com.santandertecnologia.capabilitiestesting.domain.port.in.ProductUseCase;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateProductRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.ProductResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Servicio web que maneja la lógica específica de la capa web para productos. Responsable de mapeo
 * entre DTOs y entidades del dominio, manejo de excepciones y transformaciones de datos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductWebService {

  private static final String PRODUCT_NOT_FOUND = "Product not found with ID: ";
  private final ProductUseCase productUseCase;

  /** Crea un nuevo producto mapeando el DTO a entidad del dominio. */
  public ProductResponse createProduct(final CreateProductRequest request) {
    try {
      final Product product = mapRequestToDomain(request);
      final Product createdProduct = productUseCase.createProduct(product);
      return mapDomainToResponse(createdProduct);
    } catch (final IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  /** Obtiene un producto por ID con manejo de excepciones apropiado. */
  public ProductResponse getProductById(final UUID id) {
    return productUseCase
        .getProductById(id)
        .map(this::mapDomainToResponse)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, PRODUCT_NOT_FOUND + id));
  }

  /** Busca productos por categoría con validación de enum. */
  public List<ProductResponse> searchProductsByCategory(final String category) {
    try {
      final Product.Category productCategory = Product.Category.valueOf(category.toUpperCase());
      return productUseCase.getProductsByCategory(productCategory).stream()
          .map(this::mapDomainToResponse)
          .toList();
    } catch (final IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category: " + category, e);
    }
  }

  /** Obtiene todos los productos activos. */
  public List<ProductResponse> getActiveProducts() {
    return productUseCase.getActiveProducts().stream().map(this::mapDomainToResponse).toList();
  }

  /** Actualiza el stock de un producto con validaciones. */
  public ProductResponse updateStock(final UUID id, final Integer stock) {
    return productUseCase
        .updateStock(id, stock)
        .map(this::mapDomainToResponse)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, PRODUCT_NOT_FOUND + id));
  }

  /** Elimina un producto con validación de existencia. */
  public void deleteProduct(final UUID id) {
    final boolean deleted = productUseCase.deleteProduct(id);
    if (!deleted) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, PRODUCT_NOT_FOUND + id);
    }
  }

  /** Mapea CreateProductRequest a Product del dominio. */
  private Product mapRequestToDomain(final CreateProductRequest request) {
    return Product.builder()
        .name(request.name())
        .description(request.description())
        .price(request.price())
        .category(request.category()) // Ya no necesita conversión
        .stock(request.stock())
        .sku(request.sku())
        .active(true) // Por defecto activo
        .build();
  }

  /** Mapea Product del dominio a ProductResponse. */
  private ProductResponse mapDomainToResponse(final Product product) {
    return ProductResponse.builder()
        .id(product.getId()) // Ya no necesita toString()
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .category(product.getCategory()) // Ya no necesita toString()
        .stock(product.getStock())
        .available(product.isAvailable())
        .build();
  }

  /** Valida que el stock sea un valor válido. */
  private boolean isStockAvailable(final Integer stock) {
    if (stock == null) {
      throw new IllegalArgumentException("Stock cannot be null");
    }
    if (stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    return true;
  }
}
