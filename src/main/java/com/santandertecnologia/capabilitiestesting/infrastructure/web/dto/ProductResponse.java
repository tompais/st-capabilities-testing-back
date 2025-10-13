package com.santandertecnologia.capabilitiestesting.infrastructure.web.dto;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

/** DTO de respuesta para productos en la REST API. */
@Builder
public record ProductResponse(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    Product.Category category,
    Integer stock,
    boolean available) {}
