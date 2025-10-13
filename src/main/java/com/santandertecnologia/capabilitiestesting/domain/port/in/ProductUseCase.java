package com.santandertecnologia.capabilitiestesting.domain.port.in;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para casos de uso de productos. Utiliza Optional para evitar manejo directo de
 * nulls. Usa UUID para identificación y Product.Category como inner class.
 */
public interface ProductUseCase {

  /** Crea un nuevo producto. */
  Product createProduct(Product product);

  /** Obtiene un producto por su ID. */
  Optional<Product> getProductById(UUID id);

  /** Obtiene productos por categoría. */
  List<Product> getProductsByCategory(Product.Category category);

  /** Obtiene productos activos. */
  List<Product> getActiveProducts();

  /** Actualiza el stock de un producto. */
  Optional<Product> updateProductStock(UUID id, Integer newStock);

  /** Actualiza el stock de un producto (método alternativo para compatibilidad). */
  Optional<Product> updateStock(UUID id, Integer newStock);

  /** Elimina un producto. */
  boolean deleteProduct(UUID id);
}
