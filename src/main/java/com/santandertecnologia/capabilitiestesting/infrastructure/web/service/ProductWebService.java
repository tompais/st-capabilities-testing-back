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

  private final ProductUseCase productUseCase;

  /** Crea un nuevo producto mapeando el DTO a entidad del dominio. */
  public ProductResponse createProduct(CreateProductRequest request) {
    try {
      Product product = mapRequestToDomain(request);
      Product createdProduct = productUseCase.createProduct(product);
      return mapDomainToResponse(createdProduct);
    } catch (IllegalArgumentException e) {
      log.error("Invalid product data: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid product data: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Error creating product: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error creating product", e);
    }
  }

  /** Obtiene un producto por ID con manejo de excepciones apropiado. */
  public ProductResponse getProductById(UUID id) {
    return productUseCase
        .getProductById(id)
        .map(this::mapDomainToResponse)
        .orElseThrow(
            () -> {
              log.warn("Product not found with ID: {}", id);
              return new ResponseStatusException(
                  HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
            });
  }

  /** Busca productos por categoría con validación de enum. */
  public List<ProductResponse> searchProductsByCategory(String category) {
    try {
      Product.Category productCategory = Product.Category.valueOf(category.toUpperCase());
      return productUseCase.getProductsByCategory(productCategory).stream()
          .map(this::mapDomainToResponse)
          .toList();
    } catch (IllegalArgumentException e) {
      log.error("Invalid category value: {}", category);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Invalid category: "
              + category
              + ". Valid values: ELECTRONICS, CLOTHING, BOOKS, SPORTS, HOME, OTHER",
          e);
    } catch (Exception e) {
      log.error("Error searching products by category: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error searching products by category", e);
    }
  }

  /** Obtiene todos los productos activos. */
  public List<ProductResponse> getActiveProducts() {
    try {
      return productUseCase.getActiveProducts().stream().map(this::mapDomainToResponse).toList();
    } catch (Exception e) {
      log.error("Error retrieving active products: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving active products", e);
    }
  }

  /** Actualiza el stock de un producto con validaciones. */
  public ProductResponse updateStock(UUID id, Integer stock) {
    try {
      validateStock(stock);
      return productUseCase
          .updateStock(id, stock)
          .map(this::mapDomainToResponse)
          .orElseThrow(
              () -> {
                log.warn("Product not found for stock update: {}", id);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
              });
    } catch (IllegalArgumentException e) {
      log.error("Invalid stock value: {}", stock);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Invalid stock value: " + e.getMessage(), e);
    } catch (ResponseStatusException e) {
      throw e; // Re-lanzar excepciones ya manejadas
    } catch (Exception e) {
      log.error("Error updating product stock: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error updating product stock", e);
    }
  }

  /** Elimina un producto con validación de existencia. */
  public void deleteProduct(UUID id) {
    try {
      boolean deleted = productUseCase.deleteProduct(id);
      if (!deleted) {
        log.warn("Product not found for deletion: {}", id);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with ID: " + id);
      }
    } catch (ResponseStatusException e) {
      throw e; // Re-lanzar excepciones ya manejadas
    } catch (Exception e) {
      log.error("Error deleting product: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting product", e);
    }
  }

  /** Mapea CreateProductRequest a Product del dominio. */
  private Product mapRequestToDomain(CreateProductRequest request) {
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
  private ProductResponse mapDomainToResponse(Product product) {
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
  private void validateStock(Integer stock) {
    if (stock == null) {
      throw new IllegalArgumentException("Stock cannot be null");
    }
    if (stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
  }
}
