package com.santandertecnologia.capabilitiestesting.domain.port.out;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para operaciones de repositorio de usuarios. Utiliza Optional para evitar manejo
 * directo de nulls en operaciones de búsqueda. Usa UUID para identificación y User.Status como
 * inner class.
 */
public interface UserRepository {

  /** Guarda un usuario. */
  User save(User user);

  /** Busca un usuario por su ID. */
  Optional<User> findById(UUID id);

  /** Obtiene todos los usuarios con un estado específico. */
  List<User> findByStatus(User.Status status);

  /** Obtiene todos los usuarios. */
  List<User> findAll();

  /** Elimina un usuario por su ID. */
  void deleteById(UUID id);

  /** Verifica si existe un usuario con el username dado. */
  boolean existsByUsername(String username);

  /** Verifica si existe un usuario con el email dado. */
  boolean existsByEmail(String email);

  /** Elimina todos los usuarios. Útil para limpiar datos en tests. */
  void deleteAll();
}
