package com.santandertecnologia.capabilitiestesting.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Configuración para clientes REST y servicios externos. */
@Configuration
public class RestClientConfig {

  @Value("${external.customer.service.url}")
  private String baseUrl;

  /**
   * Bean para RestClient usado en comunicación con servicios externos.
   *
   * @return RestClient configurado
   */
  @Bean
  public RestClient restClient() {
    return RestClient.builder().baseUrl(baseUrl).build();
  }
}
