package com.santandertecnologia.capabilitiestesting.infrastructure.integration;

import io.github.tobi.laa.spring.boot.embedded.redis.standalone.EmbeddedRedisStandalone;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EmbeddedRedisStandalone
public abstract class BaseIntegrationTest {

  /**
   * Constructor protegido para evitar instanciación directa. Solo las subclases pueden heredar de
   * esta clase.
   */
  protected BaseIntegrationTest() {
    // Constructor vacío intencional
  }
}
