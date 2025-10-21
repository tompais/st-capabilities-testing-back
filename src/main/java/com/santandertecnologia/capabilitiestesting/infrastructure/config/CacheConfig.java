package com.santandertecnologia.capabilitiestesting.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * Configuración de caché con Redis para Spring Cache. Usa las anotaciones estándar de Spring Cache
 * (@Cacheable, @CacheEvict, @CachePut) y delega la configuración de serialización al ObjectMapper
 * definido en JacksonConfig.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  /**
   * Configura el RedisCacheManager para usar JSON serialization con el ObjectMapper especializado
   * para Redis (definido en JacksonConfig) y TTL configurado.
   *
   * @param connectionFactory la fábrica de conexiones Redis
   * @param redisCacheObjectMapper el ObjectMapper configurado para cache (inyectado desde
   *     JacksonConfig)
   * @return el RedisCacheManager configurado
   */
  @Bean
  public RedisCacheManager cacheManager(
      final RedisConnectionFactory connectionFactory,
      @Qualifier("redisCacheObjectMapper") final ObjectMapper redisCacheObjectMapper) {

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
