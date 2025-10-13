package com.santandertecnologia.capabilitiestesting.domain.port.out;

import java.util.Optional;

/**
 * Puerto de salida para operaciones de caché. Utiliza Optional para evitar manejo directo de nulls.
 */
public interface CacheService {

  /**
   * Obtiene un valor del caché.
   *
   * @param key la clave del elemento a buscar
   * @param valueType la clase del tipo de valor esperado
   * @param <T> el tipo del valor
   * @return Optional con el valor si existe, Optional.empty() si no existe
   */
  <T> Optional<T> get(String key, Class<T> valueType);

  /**
   * Almacena un valor en el caché con TTL en segundos.
   *
   * @param key la clave del elemento
   * @param value el valor a almacenar
   * @param ttlSeconds tiempo de vida en segundos
   */
  void put(String key, Object value, long ttlSeconds);

  /**
   * Elimina un elemento del caché.
   *
   * @param key la clave del elemento a eliminar
   */
  void evict(String key);
}
