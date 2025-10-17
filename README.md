# 🧪 Capabilities Testing - Spring Boot Testing Showcase

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![JUnit](https://img.shields.io/badge/JUnit-5-green)
![Coverage](https://img.shields.io/badge/Coverage-85%25+-brightgreen)

Este proyecto demuestra las **mejores prácticas y capacidades avanzadas de testing** en Spring Boot, implementando una *
*arquitectura hexagonal limpia** con testing completo usando las tecnologías más modernas y patrones de la industria.

## 🎯 Objetivos del Proyecto

- ✅ **Demostrar testing completo** con múltiples tecnologías integradas
- ✅ **Implementar Clean Architecture** siguiendo principios SOLID y DDD
- ✅ **Aplicar mejores prácticas** de testing (FIRST, AAA, DRY)
- ✅ **Mostrar integración real** de bases de datos, cache y servicios externos
- ✅ **Usar tecnologías modernas** (UUIDs, Lombok, Records, Java 21)
- ✅ **Centralizar utilidades de test** con MockUtils y TestConstants
- ✅ **Optimizar configuración** con @TestInstance y @SneakyThrows
- ✅ **Testing embebido completo** sin dependencias de Docker

## 🏗️ Arquitectura

```
src/main/java/
├── domain/                          # 🎯 Lógica de negocio pura
│   ├── model/                       # Entidades del dominio
│   │   ├── User.java                # Usuario con H2/JPA
│   │   ├── Product.java             # Producto con MongoDB
│   │   └── ExternalCustomer.java   # Cliente externo
│   └── port/                        # Interfaces (puertos)
│       ├── in/                      # Casos de uso (entradas)
│       │   ├── UserUseCase.java
│       │   └── ProductUseCase.java
│       └── out/                     # Servicios externos (salidas)
│           ├── UserRepository.java
│           ├── ProductRepository.java
│           └── ExternalCustomerService.java
├── application/                     # 🔧 Casos de uso
│   └── service/                     # Servicios de aplicación
│       ├── UserService.java
│       └── ProductService.java
└── infrastructure/                  # 🔌 Implementaciones técnicas
    ├── adapter/                     # Adaptadores externos
    ├── persistence/                 # Persistencia (JPA + MongoDB)
    │   ├── jpa/                     # H2/PostgreSQL para Users
    │   └── mongodb/                 # MongoDB para Products
    ├── web/                         # Controllers REST + DTOs
    │   ├── controller/              # REST Controllers
    │   ├── dto/                     # Data Transfer Objects
    │   └── service/                 # Web Services
    └── config/                      # Configuraciones Spring

src/test/java/
├── application/service/             # Tests unitarios de servicios
├── infrastructure/
│   ├── integration/                 # Tests de integración E2E
│   │   ├── UserIntegrationTest.java      # H2 + Redis + MockWebServer
│   │   └── ProductIntegrationTest.java    # MongoDB Flapdoodle
│   ├── web/
│   │   ├── controller/              # Tests de controladores
│   │   └── service/                 # Tests de web services
└── utils/                           # 🛠️ Utilidades de testing
    ├── MockUtils.java               # Factory de objetos mock
    └── TestConstants.java           # Constantes compartidas
```

## 🛠️ Stack Tecnológico

### **Core Framework**

- **Spring Boot 3.5.6** - Framework principal con auto-configuración
- **Java 21** - Versión LTS más reciente con Records y Pattern Matching
- **Maven 3.9+** - Gestión de dependencias y build
- **Lombok** - Reduce boilerplate con @Builder, @Data, @SneakyThrows

### **Testing Technologies** 🧪

| Tecnología                      | Propósito          | Características                             |
|---------------------------------|--------------------|---------------------------------------------|
| **JUnit 5**                     | Framework base     | @Nested, @ParameterizedTest, @TestInstance  |
| **RestAssured MockMvc**         | Testing API REST   | DSL fluido con Hamcrest matchers            |
| **AssertJ**                     | Assertions fluidas | Verificaciones expresivas y legibles        |
| **Mockito 5**                   | Mocking avanzado   | @Mock, @InjectMocks, verify()               |
| **Flapdoodle MongoDB Spring3x** | MongoDB embebido   | Tests NoSQL sin Docker para Spring Boot 3.x |
| **Embedded Redis**              | Cache testing      | Redis en memoria para tests                 |
| **MockWebServer**               | Servicios externos | Mock de APIs HTTP/REST                      |
| **H2 Database**                 | Base de datos test | JPA en memoria con SQL                      |

### **Validation & Quality** ✅

- **Spring Validation** - JSR-303 validación declarativa
- **JaCoCo 0.8.13** - Coverage de código (mínimo 80%)
- **Spotless** - Formateo automático Google Style
- **Log4j2** - Logging estructurado con YAML

## 🧪 Estrategia de Testing

### **1. Tests Unitarios** (`@ExtendWith(MockitoExtension.class)`)

```java

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    // Usar MockUtils para objetos de prueba consistentes
    private final User testUser = MockUtils.mockUser();

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        final User result = userService.createUser(testUser);

        // Assert
        assertThat(result.getId()).isEqualTo(USER_ID);
        verify(userRepository).save(any(User.class));
    }
}
```

### **2. Tests de Controladores** (`@WebMvcTest`)

```java

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserWebService userWebService;

    // Usar MockUtils para DTOs
    private final UserResponse testResponse = MockUtils.mockUserResponse();
    private final CreateUserRequest request = MockUtils.mockCreateUserRequest();

    @BeforeAll
    void setUpAll() {
        // Configurar una sola vez para todos los tests
        RestAssuredMockMvc.webAppContextSetup(mockMvc);
    }

    @AfterAll
    void tearDownAll() {
        // Limpiar configuración de RestAssured
        RestAssuredMockMvc.reset();
    }

    @Test
    void shouldCreateUserWith201Status() {
        when(userWebService.createUser(any())).thenReturn(testResponse);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .body("email", equalTo(USER_EMAIL));
    }
}
```

### **3. Tests de Integración Completos** 🎯

**Arquitectura de Tests de Integración:**

```
BaseIntegrationTest (clase abstracta)
├── @SpringBootTest(webEnvironment = RANDOM_PORT)
├── @AutoConfigureDataMongo (Flapdoodle MongoDB)
├── @EmbeddedRedisStandalone (Redis embebido)
├── @TestInstance(PER_CLASS) (comparte contexto)
├── Configura RestAssured MockMvc automáticamente
└── NO usa @DirtiesContext (evita problemas de puerto)

ProductIntegrationTest extends BaseIntegrationTest
└── Tests de MongoDB con Flapdoodle
    ├── CRUD completo de productos
    ├── Búsquedas por categoría
    ├── Gestión de stock
    └── Validaciones

UserIntegrationTest extends BaseIntegrationTest
├── MockWebServer en puerto 8081 (configurable)
└── Tests completos con múltiples tecnologías
    ├── H2 Database (JPA/SQL)
    ├── Redis Cache (embebido)
    ├── MockWebServer (servicios externos)
    └── RestAssured MockMvc (API REST)
```

**Características Clave:**

1. **Puerto MockWebServer Configurable**: Lee del archivo `application-test.yml` (puerto 8081 por defecto)
2. **Sin @MockitoBean**: Usa solo MockWebServer para mockear servicios externos HTTP reales
3. **SKU Únicos Automáticos**: Todos los productos en MockUtils generan SKU único por defecto
4. **Inyección por Constructor**: Usa `@RequiredArgsConstructor` con campos `final` para mejor testabilidad
5. **Contexto Compartido**: Un solo contexto de Spring para todos los tests (más rápido)

**Ejemplo de Test de Integración Completo:**

```java
@DisplayName("User Integration Tests - All Technologies with Embedded DBs")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserIntegrationTest extends BaseIntegrationTest {

  private MockWebServer mockWebServer;
  
  // Inyección por constructor
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  // Puerto configurable desde application-test.yml
  @Value("${test.mock.server.port}")
  private int mockServerPort;

  @BeforeAll
  void setUpMockWebServer() {
    // MockWebServer para servicios externos (NO @MockitoBean)
    mockWebServer = new MockWebServer();
    mockWebServer.start(mockServerPort); // Puerto 8081
  }

  @Test
  void shouldCreateUserWithAllTechnologies() {
    // Arrange - Mock del servicio externo HTTP
    final Map<String, Object> customerData = Map.of(
        "id", UUID.randomUUID().toString(),
        "fullName", "Test Customer",
        "email", "test@example.com",
        "active", true,
        "riskLevel", "LOW");

    mockWebServer.enqueue(
        new MockResponse()
            .setBody(objectMapper.writeValueAsString(customerData))
            .addHeader("Content-Type", "application/json"));

    final CreateUserRequest request = MockUtils.mockCreateUserRequest();

    // Act - Llamada HTTP real que usa RestClient
    given()
        .contentType(ContentType.JSON)
        .body(objectMapper.writeValueAsString(request))
        .when()
        .post("/api/users")
        .then()
        .statusCode(201)
        .body("active", equalTo(true));
  }
}
```

**Configuración de Test (`application-test.yml`):**

```yaml
# External service configuration
external:
  customer:
    service:
      url: http://localhost:8081  # MockWebServer URL
      timeout: 5000

# Test configuration
test:
  mock:
    server:
      port: 8081  # Puerto del MockWebServer (evita conflictos con 8080)
```

### **4. MockUtils - Factory Centralizado** 🏭

**Todos los objetos mock se crean con SKU/ID único por defecto:**

```java
// Productos siempre con SKU único
Product product = MockUtils.mockProduct();
Product electronics = MockUtils.mockProduct(Product.Category.ELECTRONICS);
Product activeProduct = MockUtils.mockProduct(true);

// Users con diferentes configuraciones
User user = MockUtils.mockUser();
User inactiveUser = MockUtils.mockUser(User.Status.INACTIVE);

// DTOs para requests
CreateUserRequest userRequest = MockUtils.mockCreateUserRequest();
CreateProductRequest productRequest = MockUtils.mockCreateProductRequest(
        "Laptop",
        BigDecimal.valueOf(999.99));

// External customers
ExternalCustomer customer = MockUtils.mockExternalCustomer(
        UUID.randomUUID(),
        "John Doe",
        "john@example.com",
        true,
        ExternalCustomer.RiskLevel.LOW);
```

**Ventajas de MockUtils:**

- ✅ Consistencia en todos los tests
- ✅ SKU únicos automáticos (evita conflictos)
- ✅ DRY - No repetir builders
- ✅ Fácil mantenimiento centralizado
- ✅ Sobrecarga de métodos para flexibilidad

## 📁 Configuración de Archivos de Test

**`application-test.yml`** - Configuración específica para tests:

```yaml
spring:
  # H2 Database en memoria
  datasource:
    url: jdbc:h2:mem:testdb
    
  # MongoDB embebido (puerto aleatorio)
  data:
    mongodb:
      port: 0  # Puerto aleatorio para evitar conflictos
    redis:
      port: 6379

# Servicio externo mockeado
external:
  customer:
    service:
      url: http://localhost:8081
      
test:
  mock:
    server:
      port: 8081  # Puerto configurable del MockWebServer
```

## 🚀 Ejecutar Tests

```bash
# Todos los tests
mvn test

# Solo tests de integración
mvn test -Dtest=*IntegrationTest

# Test específico
mvn test -Dtest=UserIntegrationTest

# Con coverage
mvn clean verify

# Formatear código
mvn spotless:apply
```

## 📊 Coverage y Calidad

- **JaCoCo**: Mínimo 80% de coverage
- **Spotless**: Google Java Style automático
- **Checkstyle**: Validación de estilo
- **Tests**: >85% de cobertura actual

## 🎓 Patrones y Best Practices Implementados

### **Testing Patterns** ✨

1. **AAA Pattern** (Arrange-Act-Assert) - Estructura clara en todos los tests
2. **FIRST Principles** - Fast, Independent, Repeatable, Self-validating, Timely
3. **Given-When-Then** - Narrativa clara con RestAssured DSL
4. **Test Fixtures** - MockUtils centralizado
5. **Builder Pattern** - Lombok @Builder para objetos de prueba
6. **Factory Pattern** - MockUtils como factory de mocks
7. **Strategy Pattern** - Diferentes estrategias de mock según necesidad

### **Integration Testing Best Practices** 🎯

1. **Un solo contexto compartido** - `@TestInstance(PER_CLASS)` para performance
2. **Limpieza manual de datos** - `repository.deleteAll()` en `@BeforeEach`
3. **NO usar `@DirtiesContext`** - Evita problemas con puertos ocupados
4. **MockWebServer real** - Simula servicios HTTP externos realmente
5. **Embedded databases** - MongoDB Flapdoodle, H2, Redis embebidos
6. **Puerto configurable** - MockWebServer en 8081 desde configuración
7. **Inyección por constructor** - Mejor testabilidad y campos `final`

### **Code Quality Practices** 💎

1. **Immutability** - Records, final fields, Lombok @Value
2. **Separation of Concerns** - Arquitectura hexagonal limpia
3. **Dependency Injection** - Constructor injection preferido
4. **Clean Code** - Nombres descriptivos, métodos pequeños
5. **SOLID Principles** - Especialmente SRP y DIP
6. **DRY** - MockUtils elimina duplicación

## 📝 Notas Importantes

### **Tests de Integración**

⚠️ **IMPORTANTE**: Los tests de integración usan:

- **MockWebServer en puerto 8081** (configurable en `application-test.yml`)
- **NO usan `@MockitoBean`** para servicios externos HTTP
- **RestClient real** conecta a MockWebServer para tests realistas
- **Un solo contexto de Spring** compartido entre todos los tests
- **Limpieza manual** de datos entre tests (no `@DirtiesContext`)

### **MockUtils vs Builders Inline**

✅ **Usar MockUtils cuando:**

- Necesitas objetos estándar de prueba
- Quieres consistencia entre tests
- El objeto se usa en múltiples tests

✅ **Usar Builders inline cuando:**

- El test necesita valores muy específicos
- Es un caso edge único
- Quieres destacar valores particulares en el test

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/amazing-test`)
3. Aplica Spotless (`mvn spotless:apply`)
4. Verifica tests (`mvn verify`)
5. Commit (`git commit -m 'Add amazing test'`)
6. Push (`git push origin feature/amazing-test`)
7. Abre un Pull Request

## 📚 Referencias

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [RestAssured Documentation](https://rest-assured.io/)
- [MockWebServer Guide](https://github.com/square/okhttp/tree/master/mockwebserver)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

**Creado con ❤️ para demostrar las mejores prácticas de testing en Spring Boot**
