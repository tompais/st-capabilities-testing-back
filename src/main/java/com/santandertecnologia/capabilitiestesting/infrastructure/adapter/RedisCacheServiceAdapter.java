package com.santandertecnologia.capabilitiestesting.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.santandertecnologia.capabilitiestesting.domain.port.out.CacheService;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Adaptador que implementa el puerto de salida CacheService usando Redis. */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceAdapter implements CacheService {

  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public <T> Optional<T> get(String key, Class<T> valueType) {
    try {
      String jsonValue = redisTemplate.opsForValue().get(key);
      if (jsonValue == null) {
        log.debug("Cache miss for key: {}", key);
        return Optional.empty();
      }

      T value = objectMapper.readValue(jsonValue, valueType);
      log.debug("Cache hit for key: {}", key);
      return Optional.of(value);

    } catch (JsonProcessingException e) {
      log.error("Error deserializing cached value for key: {}", key, e);
      return Optional.empty();
    } catch (Exception e) {
      log.error("Unexpected error retrieving cache value for key: {}", key, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve cached value", e);
    }
  }

  @Override
  public void put(String key, Object value, long ttlSeconds) {
    try {
      String jsonValue = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, jsonValue, Duration.ofSeconds(ttlSeconds));
      log.debug("Stored value in cache with key: {} and TTL: {} seconds", key, ttlSeconds);
    } catch (JsonProcessingException e) {
      log.error("Error serializing value for cache key: {}", key, e);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Failed to serialize value for caching", e);
    } catch (Exception e) {
      log.error("Unexpected error storing cache value for key: {}", key, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store value in cache", e);
    }
  }

  @Override
  public void evict(String key) {
    try {
      boolean wasDeleted = redisTemplate.delete(key);
      log.debug("Evicted cache key: {}, was present: {}", key, wasDeleted);
    } catch (Exception e) {
      log.error("Error evicting cache key: {}", key, e);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to evict cache key", e);
    }
  }
}
