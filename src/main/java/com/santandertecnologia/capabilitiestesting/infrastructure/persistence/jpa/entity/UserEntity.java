package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.jpa.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entidad JPA para usuarios. */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "phone_number")
  private String phoneNumber;

  private String department;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  private UserStatus status = UserStatus.ACTIVE;

  @Column(name = "created_at")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "updated_at")
  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  public boolean isActive() {
    return UserStatus.ACTIVE.equals(status);
  }

  public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    INACTIVE
  }
}
