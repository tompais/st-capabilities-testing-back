package com.santandertecnologia.capabilitiestesting.infrastructure.web.dto;

import java.util.UUID;
import lombok.*;

/** DTO de respuesta para usuarios en la REST API. */
@Builder
public record UserResponse(UUID id, String email, String name, String phone, boolean active) {}
