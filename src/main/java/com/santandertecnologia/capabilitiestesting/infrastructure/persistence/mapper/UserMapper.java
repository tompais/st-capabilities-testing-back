package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mapper;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.jpa.entity.UserEntity;
import org.springframework.stereotype.Component;

/** Mapper para conversión entre User (dominio) y UserEntity (infraestructura). */
@Component
public class UserMapper {

  /**
   * Convierte de entidad de dominio a entidad de infraestructura.
   *
   * @param user modelo de dominio
   * @return entidad JPA
   */
  public UserEntity toEntity(User user) {
    if (user == null) {
      return null;
    }

    return UserEntity.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .phoneNumber(user.getPhoneNumber())
        .department(user.getDepartment())
        .status(toEntityStatus(user.getStatus()))
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .lastLoginAt(user.getLastLoginAt())
        .build();
  }

  /**
   * Convierte de entidad de infraestructura a entidad de dominio.
   *
   * @param entity entidad JPA
   * @return modelo de dominio
   */
  public User toDomain(UserEntity entity) {
    if (entity == null) {
      return null;
    }

    return User.builder()
        .id(entity.getId())
        .username(entity.getUsername())
        .email(entity.getEmail())
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .phoneNumber(entity.getPhoneNumber())
        .department(entity.getDepartment())
        .status(toDomainStatus(entity.getStatus()))
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .lastLoginAt(entity.getLastLoginAt())
        .build();
  }

  /** Convierte de enum de dominio a enum de entidad. */
  public UserEntity.UserStatus toEntityStatus(User.Status domainStatus) {
    if (domainStatus == null) {
      return null;
    }
    return UserEntity.UserStatus.valueOf(domainStatus.name());
  }

  /** Convierte de enum de entidad a enum de dominio. */
  public User.Status toDomainStatus(UserEntity.UserStatus entityStatus) {
    if (entityStatus == null) {
      return null;
    }
    return User.Status.valueOf(entityStatus.name());
  }
}
