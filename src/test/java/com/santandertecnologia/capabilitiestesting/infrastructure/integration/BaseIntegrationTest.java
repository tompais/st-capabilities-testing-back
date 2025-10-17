package com.santandertecnologia.capabilitiestesting.infrastructure.integration;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.github.tobi.laa.spring.boot.embedded.redis.standalone.EmbeddedRedisStandalone;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

/**
 * Clase base abstracta para todos los tests de integración.
 *
 * <p>Esta clase base asegura que TODOS los tests de integración compartan el MISMO contexto de
 * Spring, lo cual es crucial para:
 *
 * <ul>
 *   <li>Evitar que Flapdoodle MongoDB intente iniciar en el mismo puerto múltiples veces
 *   <li>Evitar que Redis embebido se cierre prematuramente entre tests
 *   <li>Mejorar el rendimiento al reutilizar el contexto entre tests
 *   <li>Configurar RestAssured MockMvc una sola vez para todos los tests
 * </ul>
 *
 * <p><strong>IMPORTANTE:</strong> NO usar {@code @DirtiesContext} en las subclases, ya que esto
 * destruirá el contexto compartido y causará errores con Flapdoodle y Redis.
 *
 * <p>La limpieza de datos entre tests debe hacerse manualmente en el método {@code @BeforeEach} de
 * cada test, usando {@code repository.deleteAll()} y {@code cacheManager.getCache().clear()}.
 *
 * <p>Ejemplo de uso:
 *
 * <pre>
 * class MyIntegrationTest extends BaseIntegrationTest {
 *
 *   {@literal @}Autowired
 *   private MyRepository myRepository;
 *
 *   {@literal @}Autowired
 *   private CacheManager cacheManager;
 *
 *   {@literal @}BeforeEach
 *   void setUp() {
 *     // Limpiar datos antes de cada test
 *     myRepository.deleteAll();
 *
 *     // Limpiar Redis cache
 *     cacheManager.getCacheNames().forEach(cacheName -> {
 *       var cache = cacheManager.getCache(cacheName);
 *       if (cache != null) {
 *         cache.clear();
 *       }
 *     });
 *   }
 *
 *   {@literal @}Test
 *   void myTest() {
 *     // Test implementation
 *   }
 * }
 * </pre>
 */
@AutoConfigureDataMongo
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(PER_CLASS)
@EmbeddedRedisStandalone
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseIntegrationTest {

  /**
   * Configuración inicial ejecutada UNA SOLA VEZ antes de todos los tests de la clase. Configura
   * RestAssured MockMvc con el contexto de Spring para realizar llamadas HTTP simuladas.
   *
   * @param webApplicationContext El contexto de Spring Web inyectado automáticamente
   */
  @BeforeAll
  void setUpRestAssured(@Autowired final WebApplicationContext webApplicationContext) {
    // Configurar RestAssured MockMvc una sola vez para toda la clase de test
    // Esto permite hacer llamadas HTTP simuladas sin levantar un servidor real
    RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
  }

  /**
   * Limpieza ejecutada UNA SOLA VEZ después de todos los tests de la clase. Resetea la
   * configuración de RestAssured MockMvc para evitar interferencias entre clases de test.
   */
  @AfterAll
  void tearDownRestAssured() {
    // Reset de RestAssured para limpiar toda la configuración
    // Esto asegura que cada clase de test comience con un estado limpio
    RestAssuredMockMvc.reset();
  }
}
