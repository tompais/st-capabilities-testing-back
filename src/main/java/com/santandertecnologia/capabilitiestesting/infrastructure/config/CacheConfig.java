package com.santandertecnologia.capabilitiestesting.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de caché con Redis para Spring Cache. Reemplaza el adaptador personalizado de Redis
 * con las anotaciones estándar de Spring Cache (@Cacheable, @CacheEvict, @CachePut).
 */
@Configuration
@EnableCaching
public class CacheConfig {

  /**
   * Configura el ObjectMapper para serialización Redis con soporte para Java Time y propiedades
   * desconocidas.
   *
   * @return el ObjectMapper configurado
   */
  @Bean
  public ObjectMapper redisCacheObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();

    // Registrar módulo de Java Time para manejo de fechas/tiempos
    objectMapper.registerModule(new JavaTimeModule());

    // No fallar ante propiedades desconocidas
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Configurar type information para polimorfismo
    objectMapper.activateDefaultTyping(
        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    return objectMapper;
  }

  /**
   * Configura el RedisCacheManager para usar JSON serialization con ObjectMapper personalizado y
   * TTL configurado.
   *
   * @param connectionFactory la fábrica de conexiones Redis
   * @param redisCacheObjectMapper el ObjectMapper configurado para cache
   * @return el RedisCacheManager configurado
   */
  @Bean
  public RedisCacheManager cacheManager(
      final RedisConnectionFactory connectionFactory, final ObjectMapper redisCacheObjectMapper) {

    final Jackson2JsonRedisSerializer<Object> serializer =
        new Jackson2JsonRedisSerializer<>(redisCacheObjectMapper, Object.class);

    final RedisCacheConfiguration cacheConfig =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // TTL por defecto de 5 minutos
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer));

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfig).build();
  }
}
