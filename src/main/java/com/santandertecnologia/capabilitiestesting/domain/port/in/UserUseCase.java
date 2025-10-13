package com.santandertecnologia.capabilitiestesting.domain.port.in;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para casos de uso de usuarios. Utiliza Optional para evitar manejo directo de
 * nulls. Usa UUID para identificación y User.Status como inner class.
 */
public interface UserUseCase {

  /** Crea un nuevo usuario. */
  User createUser(User user);

  /** Obtiene un usuario por su ID. */
  Optional<User> getUserById(UUID id);

  /** Obtiene todos los usuarios activos. */
  List<User> getActiveUsers();

  /** Cambia el estado de un usuario. */
  Optional<User> changeUserStatus(UUID id, User.Status status);

  /** Actualiza el estado de un usuario (método alternativo para compatibilidad). */
  Optional<User> updateUserStatus(UUID id, User.Status status);

  /** Elimina un usuario. */
  boolean deleteUser(UUID id);
}
