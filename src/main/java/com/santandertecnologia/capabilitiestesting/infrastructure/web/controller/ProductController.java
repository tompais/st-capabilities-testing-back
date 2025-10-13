package com.santandertecnologia.capabilitiestesting.infrastructure.web.controller;

import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.CreateProductRequest;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.dto.ProductResponse;
import com.santandertecnologia.capabilitiestesting.infrastructure.web.service.ProductWebService;
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
 * Controlador REST minimalista para productos. Solo maneja routing, validación y respuestas HTTP.
 * La lógica de mapeo y negocio está delegada a ProductWebService.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProductController {

  private final ProductWebService productWebService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
    log.info("Creating product with SKU: {}", request.sku());
    return productWebService.createProduct(request);
  }

  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public ProductResponse getProductById(@PathVariable UUID id) {
    log.info("Getting product by ID: {}", id);
    return productWebService.getProductById(id);
  }

  @GetMapping("/search")
  @ResponseStatus(HttpStatus.OK)
  public List<ProductResponse> searchProductsByCategory(@RequestParam String category) {
    log.info("Searching products by category: {}", category);
    return productWebService.searchProductsByCategory(category);
  }

  @GetMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  public List<ProductResponse> getActiveProducts() {
    log.info("Getting all active products");
    return productWebService.getActiveProducts();
  }

  @PutMapping("/{id}/stock")
  @ResponseStatus(HttpStatus.OK)
  public ProductResponse updateStock(@PathVariable UUID id, @RequestParam Integer stock) {
    log.info("Updating product {} stock to: {}", id, stock);
    return productWebService.updateStock(id, stock);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteProduct(@PathVariable UUID id) {
    log.info("Deleting product with ID: {}", id);
    productWebService.deleteProduct(id);
  }
}
