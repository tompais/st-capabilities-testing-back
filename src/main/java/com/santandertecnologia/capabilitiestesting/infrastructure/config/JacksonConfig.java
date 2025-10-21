package com.santandertecnologia.capabilitiestesting.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Configuración general de Jackson para toda la aplicación. Define el ObjectMapper principal que se
 * usará en toda la aplicación incluyendo controladores REST, serialización de caché, etc.
 */
@Configuration
public class JacksonConfig {

  /**
   * ObjectMapper principal de la aplicación. Este bean será usado automáticamente por Spring Boot
   * en toda la aplicación (controladores REST, etc.). Configurado con: - Soporte para Java 8
   * date/time (JSR-310) - Serialización de fechas como ISO-8601 strings - Tolerancia a propiedades
   * desconocidas
   *
   * @return ObjectMapper configurado
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder.json()
        .modules(new JavaTimeModule())
        .featuresToDisable(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
  }

  /**
   * ObjectMapper especializado para serialización en Redis Cache. Incluye configuración adicional
   * para polimorfismo que es necesaria para el almacenamiento en caché.
   *
   * @return ObjectMapper configurado para Redis
   */
  @Bean
  public ObjectMapper redisCacheObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();

    // Registrar módulo de Java Time para manejo de fechas/tiempos
    mapper.registerModule(new JavaTimeModule());

    // Deshabilitar escritura de fechas como timestamps - usar formato ISO-8601
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // No fallar ante propiedades desconocidas
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Configurar type information para polimorfismo (necesario para Redis)
    mapper.activateDefaultTyping(
        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    return mapper;
  }
}
