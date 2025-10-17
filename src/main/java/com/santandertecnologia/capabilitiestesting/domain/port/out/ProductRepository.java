package com.santandertecnologia.capabilitiestesting.domain.port.out;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para operaciones de repositorio de productos. Utiliza Optional para evitar
 * manejo directo de nulls en operaciones de búsqueda. Usa UUID para identificación y
 * Product.Category como inner class.
 */
public interface ProductRepository {

  /** Guarda un producto. */
  Product save(Product product);

  /** Busca un producto por su ID. */
  Optional<Product> findById(UUID id);

  /** Obtiene todos los productos por categoría. */
  List<Product> findByCategory(Product.Category category);

  /** Obtiene productos activos. */
  List<Product> findActiveProducts();

  /** Elimina un producto por su ID. */
  void deleteById(UUID id);

  /** Elimina todos los productos. */
  void deleteAll();

  /** Verifica si existe un producto con el SKU dado. */
  boolean existsBySku(String sku);
}
