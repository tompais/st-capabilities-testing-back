package com.santandertecnologia.capabilitiestesting.infrastructure.web.dto;

import com.santandertecnologia.capabilitiestesting.domain.model.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * DTO para solicitudes de creación de producto. Incluye validaciones Spring Validation para
 * garantizar datos válidos desde el cliente.
 */
@Builder(toBuilder = true)
@Jacksonized
public record CreateProductRequest(
    @NotBlank(message = "Product name is required")
        @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
        String name,
    @Size(max = 500, message = "Description cannot exceed 500 characters") String description,
    @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,
    @NotNull(message = "Category is required") Product.Category category,
    @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock cannot be negative")
        Integer stock,
    @NotBlank(message = "SKU is required")
        @Pattern(
            regexp = "^[A-Z0-9-]{3,20}$",
            message = "SKU must contain only uppercase letters, numbers and hyphens (3-20 chars)")
        String sku) {}
