package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.jpa.repository;

import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.jpa.entity.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repositorio JPA para operaciones de persistencia de usuarios. */
@Repository
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

  List<UserEntity> findByStatus(UserEntity.UserStatus status);

  /**
   * Verifica si existe un usuario con el email dado.
   *
   * @param email email a verificar
   * @return true si existe
   */
  boolean existsByEmail(String email);

  /**
   * Verifica si existe un usuario con el username dado.
   *
   * @param username username a verificar
   * @return true si existe
   */
  boolean existsByUsername(String username);
}
