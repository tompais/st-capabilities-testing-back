package com.santandertecnologia.capabilitiestesting.infrastructure.web.controller;

import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateUserRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.UserResponse;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.service.UserWebService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST minimalista para usuarios. Solo maneja routing, validación y respuestas HTTP. La
 * lógica de mapeo y negocio está delegada a UserWebService.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

  private final UserWebService userWebService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse createUser(@Valid @RequestBody final CreateUserRequest request) {
    log.info("Creating user with email: {}", request.email());
    return userWebService.createUser(request);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public UserResponse getUserById(@PathVariable final UUID id) {
    log.info("Getting user by ID: {}", id);
    return userWebService.getUserById(id);
  }

  @GetMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  public List<UserResponse> getActiveUsers() {
    log.info("Getting all active users");
    return userWebService.getActiveUsers();
  }

  @PutMapping("/{id}/status")
  @ResponseStatus(HttpStatus.OK)
  public UserResponse updateUserStatus(
      @PathVariable final UUID id, @RequestParam final String status) {
    log.info("Updating user {} status to: {}", id, status);
    return userWebService.updateUserStatus(id, status);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable final UUID id) {
    log.info("Deleting user with ID: {}", id);
    userWebService.deleteUser(id);
  }
}
