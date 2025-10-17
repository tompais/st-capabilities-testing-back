package com.santandertecnologia.capabilitiestesting.infrastructure.persistence.adapter;

import com.santandertecnologia.capabilitiestesting.domain.model.User;
import com.santandertecnologia.capabilitiestesting.domain.port.out.UserRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.jpa.repository.SpringDataUserRepository;
import com.santandertecnologia.capabilitiestesting.infrastructure.persistence.mapper.UserMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** Adaptador que implementa el puerto de salida UserRepository usando JPA. */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

  private final SpringDataUserRepository springDataUserRepository;
  private final UserMapper userMapper;

  @Override
  public User save(final User user) {
    final var entity = userMapper.toEntity(user);
    final var savedEntity = springDataUserRepository.save(entity);
    return userMapper.toDomain(savedEntity);
  }

  @Override
  public Optional<User> findById(final UUID id) {
    return springDataUserRepository.findById(id).map(userMapper::toDomain);
  }

  @Override
  public List<User> findByStatus(final User.Status status) {
    final var entityStatus = userMapper.toEntityStatus(status);
    return springDataUserRepository.findByStatus(entityStatus).stream()
        .map(userMapper::toDomain)
        .toList();
  }

  @Override
  public List<User> findAll() {
    return springDataUserRepository.findAll().stream().map(userMapper::toDomain).toList();
  }

  @Override
  public void deleteById(final UUID id) {
    springDataUserRepository.deleteById(id);
  }

  @Override
  public boolean existsByUsername(final String username) {
    return springDataUserRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(final String email) {
    return springDataUserRepository.existsByEmail(email);
  }

  @Override
  public void deleteAll() {
    springDataUserRepository.deleteAll();
  }
}
