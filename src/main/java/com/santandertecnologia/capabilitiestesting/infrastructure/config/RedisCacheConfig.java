package com.santandertecnologia.capabilitiestesting.infrastructure.config;

import java.time.Duration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de caché con Redis para Spring Cache. Reemplaza el adaptador personalizado de Redis
 * con las anotaciones estándar de Spring Cache (@Cacheable, @CacheEvict, @CachePut).
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

  /**
   * Configura el RedisCacheManager para usar JSON serialization y TTL configurado.
   *
   * @param connectionFactory la fábrica de conexiones Redis
   * @return el RedisCacheManager configurado
   */
  @Bean
  public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory) {
    final RedisCacheConfiguration cacheConfig =
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5)) // TTL por defecto de 5 minutos
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));

    return RedisCacheManager.builder(connectionFactory).cacheDefaults(cacheConfig).build();
  }
}
