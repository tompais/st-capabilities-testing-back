package com.santandertecnologia.capabilitiestesting.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * DTO para solicitudes de creación de usuario. Incluye validaciones Spring Validation para
 * garantizar datos válidos desde el cliente.
 */
@Builder(toBuilder = true)
@Jacksonized
public record CreateUserRequest(
    @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(
            regexp = "^[a-zA-Z0-9_-]+$",
            message = "Username can only contain letters, numbers, underscore and hyphen")
        String username,
    @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,
    @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,
    @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,
    @Pattern(
            regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Phone number must be in international format (+1234567890)")
        String phoneNumber,
    @Size(max = 100, message = "Department cannot exceed 100 characters") String department) {}
