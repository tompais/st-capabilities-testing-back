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
- ✅ **Usar tecnologías modernas** (UUIDs, Lombok, Records, etc.)
- ✅ **Centralizar utilidades de test** con MockUtils y TestConstants
- ✅ **Optimizar configuración** con @TestInstance y @SneakyThrows

## 🏗️ Arquitectura

```
src/main/java/
├── domain/                          # 🎯 Lógica de negocio pura
│   ├── model/                       # Entidades del dominio
│   └── port/                        # Interfaces (puertos)
│       ├── in/                      # Casos de uso (entradas)
│       └── out/                     # Servicios externos (salidas)
├── application/                     # 🔧 Casos de uso
│   └── service/                     # Servicios de aplicación  
└── infrastructure/                  # 🔌 Implementaciones técnicas
    ├── adapter/                     # Adaptadores externos
    ├── persistence/                 # Persistencia (JPA + MongoDB)
    │   ├── jpa/                     # H2/PostgreSQL
    │   └── mongodb/                 # MongoDB
    ├── web/                         # Controllers REST + DTOs
    │   ├── controller/              # REST Controllers
    │   ├── dto/                     # Data Transfer Objects
    │   └── service/                 # Web Services
    └── config/                      # Configuraciones Spring

src/test/java/
├── application/service/             # Tests unitarios de servicios
├── infrastructure/
│   ├── integration/                 # Tests de integración E2E
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

| Tecnología              | Propósito          | Características                            |
|-------------------------|--------------------|--------------------------------------------|
| **JUnit 5**             | Framework base     | @Nested, @ParameterizedTest, @TestInstance |
| **RestAssured MockMvc** | Testing API REST   | DSL fluido con Hamcrest matchers           |
| **AssertJ**             | Assertions fluidas | Verificaciones expresivas y legibles       |
| **Mockito 5**           | Mocking avanzado   | @Mock, @InjectMocks, verify()              |
| **Testcontainers**      | Integración real   | MongoDB en Docker                          |
| **Embedded Redis**      | Cache testing      | Redis en memoria para tests                |
| **MockWebServer**       | Servicios externos | Mock de APIs HTTP/REST                     |
| **H2 Database**         | Base de datos test | JPA en memoria con SQL                     |
| **Flapdoodle MongoDB**  | MongoDB embebido   | Tests NoSQL sin Docker                     |

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
        User result = userService.createUser(testUser);

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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserWebService userWebService;

    // Usar MockUtils para DTOs
    private final UserResponse testResponse = MockUtils.mockUserResponse();
    private final CreateUserRequest request = MockUtils.mockCreateUserRequest();

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

### **3. Tests de Integración E2E** (`@SpringBootTest`)

```java

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedRedisStandalone
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Permite @BeforeAll no estático
class UserIntegrationTest {

    private MockWebServer mockWebServer;

    @SneakyThrows // Elimina throws Exception
    @BeforeAll
    void setUpAll() {
        // Configurar una sola vez para todos los tests
        mockWebServer = new MockWebServer();
        mockWebServer.start(MOCK_SERVER_PORT);
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }

    @SneakyThrows
    @AfterAll
    void tearDownAll() {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
        RestAssuredMockMvc.reset();
    }

    @SneakyThrows
    @Test
    void shouldCompleteFullUserLifecycle() {
        // Crear → Cachear → Validar Externo → Actualizar → Eliminar
        ExternalCustomer mockCustomer = MockUtils.mockExternalCustomer(
                customerId, "Customer", email, true, RiskLevel.LOW
        );

        // Usar Map.of en lugar de HashMap
        Map<String, Object> data = Map.of(
                "customerId", customerId.toString(),
                "active", true,
                "riskLevel", "LOW"
        );

        mockWebServer.enqueue(new MockResponse().setBody(json));
        // ... resto del test
    }
}
```

### **4. Tests Parametrizados** (Múltiples fuentes)

```java
// @ValueSource - Arrays simples
@ParameterizedTest
@ValueSource(strings = {"INVALID", "ENABLED", "", "null"})
void shouldRejectInvalidStatus(String invalidStatus) {
    assertThatThrownBy(() -> service.updateStatus(invalidStatus))
            .isInstanceOf(ResponseStatusException.class);
}

// @CsvSource - Datos tabulares
@ParameterizedTest
@CsvSource({
        "ACTIVE, true",
        "SUSPENDED, false",
        "INACTIVE, false"
})
void shouldMapStatusToActive(User.Status status, boolean expectedActive) {
    User user = MockUtils.mockUser(status);
    assertThat(user.isActive()).isEqualTo(expectedActive);
}

// @MethodSource - Datos complejos
@ParameterizedTest
@MethodSource("provideExceptionMappingData")
void shouldMapExceptions(Exception exception, HttpStatus expectedStatus) {
    // Recibir excepciones ya construidas, sin reflection
    when(useCase.createUser(any())).thenThrow(exception);

    assertThatThrownBy(() -> service.createUser(request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(expectedStatus);
            });
}

private static Stream<Arguments> provideExceptionMappingData() {
    return Stream.of(
            Arguments.of(new IllegalArgumentException("Invalid"), HttpStatus.BAD_REQUEST),
            Arguments.of(new IllegalStateException("State error"), HttpStatus.CONFLICT)
    );
}
```

## 🎨 Utilidades de Testing Centralizadas

### **MockUtils.java** - Factory de objetos mock

```java
public final class MockUtils {

    // Usuarios
    public static User mockUser() { /* valores por defecto */ }

    public static User mockUser(UUID id) { /* con ID específico */ }

    public static User mockUser(User.Status status) { /* con estado */ }

    // DTOs Web
    public static UserResponse mockUserResponse() { /* por defecto */ }

    public static UserResponse mockUserResponse(UUID id) { /* con ID */ }

    public static CreateUserRequest mockCreateUserRequest() { /* por defecto */ }

    public static CreateUserRequest mockCreateUserRequest(String username, String email) { /* personalizado */ }

    // Productos
    public static Product mockProduct() { /* por defecto */ }

    public static Product mockProduct(UUID id) { /* con ID */ }

    // Clientes externos
    public static ExternalCustomer mockExternalCustomer() { /* por defecto */ }

    public static ExternalCustomer mockExternalCustomer(UUID id, String name, String email,
                                                        boolean active, RiskLevel riskLevel) { /* completo */ }
}
```

### **TestConstants.java** - Constantes compartidas

```java
public final class TestConstants {
    
    // User test constants
    public static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    public static final String USER_USERNAME = "testuser";
    public static final String USER_EMAIL = "test@santander.com";
    public static final String USER_FIRST_NAME = "Test";
    public static final String USER_LAST_NAME = "User";
    public static final String USER_FULL_NAME = USER_FIRST_NAME + " " + USER_LAST_NAME;
    public static final String USER_PHONE = "+34666123456";
    
    // Status strings
    public static final String STATUS_STRING_ACTIVE = "ACTIVE";
    public static final String STATUS_STRING_SUSPENDED = "SUSPENDED";
    
    // Product constants
    public static final UUID PRODUCT_ID = UUID.fromString("223e4567-...");
    public static final String PRODUCT_NAME = "Test Product";
    
    // Cache keys
    public static final String CACHE_KEY_USER_PREFIX = "user:";
}
```

## 🚀 Funcionalidades Implementadas

### **Modelos del Dominio con Validaciones**

```java

@Data
@Builder
@Entity
public class User {
    @Id
    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid username format")
    private String username;

    @Email(message = "Email must be valid")
    @NotBlank
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be E.164 format")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        ACTIVE, SUSPENDED, INACTIVE
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }
}
```

### **Persistencia Multi-Base de Datos**

- **H2 (JPA)**: Usuarios con transacciones ACID y queries derivadas
- **MongoDB**: Productos con escalabilidad NoSQL y documentos flexibles
- **Redis**: Cache distribuido con TTL para mejora de performance

### **Web Layer con DTOs Records**

```java
public record UserResponse(
        UUID id,
        String email,
        String name,
        String phone,
        boolean active
) {
}

public record CreateUserRequest(
        @NotBlank String username,
        @Email String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String department
) {
}
```

## 📊 Coverage y Calidad

### **Configuración JaCoCo**

- **Mínimo requerido**: 80% instruction coverage
- **Objetivo**: 85%+ en lógica de negocio
- **Build falla** si coverage < 80%

### **Exclusiones de Coverage**

```xml

<excludes>
    <!-- Entidades sin lógica -->
    <exclude>**/infrastructure/persistence/jpa/entity/**</exclude>
    <exclude>**/infrastructure/persistence/mongodb/document/**</exclude>

    <!-- DTOs y Records -->
    <exclude>**/infrastructure/web/dto/**</exclude>

    <!-- Configuración -->
    <exclude>**/infrastructure/config/**</exclude>
    <exclude>**/CapabilitiesTestingApplication.class</exclude>

    <!-- Repositorios generados -->
    <exclude>**/infrastructure/persistence/**/repository/**</exclude>
</excludes>
```

### **Code Quality Tools**

- ✅ **Spotless**: Formateo automático Google Java Style
- ✅ **Maven Enforcer**: Validación de versiones y dependencias
- ✅ **Validation**: JSR-303 en todas las capas
- ✅ **Logging**: Log4j2 estructurado con niveles apropiados

## 🚀 Getting Started

### **Prerrequisitos**

```bash
java -version   # Java 21+ requerido
mvn -version    # Maven 3.9+ requerido
docker --version # Docker para Testcontainers (opcional)
```

### **Instalación y Ejecución**

```bash
# Clonar el proyecto
git clone https://github.com/tu-usuario/capabilities-testing.git
cd capabilities-testing

# Compilar el proyecto
mvn clean compile

# Ejecutar TODOS los tests
mvn clean test

# Ejecutar tests con coverage
mvn clean verify

# Ver reporte de coverage en navegador
open target/site/jacoco/index.html

# Ejecutar aplicación
mvn spring-boot:run
```

### **Tests Selectivos**

```bash
# Tests unitarios solamente
mvn test -Dtest="*ServiceTest"

# Tests de integración
mvn test -Dtest="*IntegrationTest"

# Tests de controladores
mvn test -Dtest="*ControllerTest"

# Tests parametrizados
mvn test -Dtest="*ParameterizedTest"

# Test específico
mvn test -Dtest="UserServiceTest#shouldCreateUserSuccessfully"

# Tests con logs detallados
mvn test -X
```

## 🎓 Conceptos y Patrones Demostrados

### **Testing Best Practices**

- ✅ **FIRST Principles**: Fast, Independent, Repeatable, Self-validating, Timely
- ✅ **AAA Pattern**: Arrange, Act, Assert en todos los tests
- ✅ **DRY Principle**: MockUtils y TestConstants eliminan duplicación
- ✅ **Test Data Builders**: Factory methods para construcción elegante
- ✅ **Test Organization**: @Nested classes para agrupación lógica
- ✅ **Descriptive Names**: Nombres de tests auto-documentados

### **Spring Boot Testing Annotations**

```java
@SpringBootTest                    // Tests de integración completos
@WebMvcTest                        // Tests de controladores aislados
@DataJpaTest                       // Tests de repositorios JPA
@MockitoBean                       // Mocks en contexto Spring
@TestConfiguration                 // Configuraciones específicas de test
@ActiveProfiles("test")            // Perfil de test separado
@TestInstance(Lifecycle.PER_CLASS) // Instancia única para @BeforeAll no estático
```

### **Lombok en Testing**

```java
@SneakyThrows                      // Elimina throws Exception
@Data @Builder                     // Constructores fluidos para tests
@NoArgsConstructor(access = PRIVATE) // Utility classes
```

### **Modern Java Features**

- ✅ **Records**: DTOs inmutables y concisos
- ✅ **Pattern Matching**: Switch expressions elegantes
- ✅ **Text Blocks**: JSON/SQL multi-línea legibles
- ✅ **Map.of()**: Mapas inmutables sin HashMap
- ✅ **List.of()**: Listas inmutables sin ArrayList

## 🔧 Configuración de Testing

### **application-test.yml**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

  data:
    mongodb:
      database: testing

  cache:
    type: redis
    redis:
      time-to-live: 300000

logging:
  level:
    com.santandertecnologia: DEBUG
    org.springframework.test: INFO
```

### **pom.xml - Dependencies destacadas**

```xml
<!-- Testing Core -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

        <!-- RestAssured MockMvc -->
<dependency>
<groupId>io.rest-assured</groupId>
<artifactId>spring-mock-mvc</artifactId>
<scope>test</scope>
</dependency>

        <!-- Embedded Redis -->
<dependency>
<groupId>io.github.tobi-laa</groupId>
<artifactId>spring-boot-embedded-redis-standalone</artifactId>
<version>1.2.2</version>
<scope>test</scope>
</dependency>

        <!-- MockWebServer -->
<dependency>
<groupId>com.squareup.okhttp3</groupId>
<artifactId>mockwebserver</artifactId>
<scope>test</scope>
</dependency>
```

## 📈 Métricas del Proyecto

| Métrica            | Valor       |
|--------------------|-------------|
| **Total Tests**    | 60+         |
| **Test Coverage**  | 85%+        |
| **Build Time**     | ~8 segundos |
| **Test Execution** | ~6 segundos |
| **LOC (main)**     | ~2,000      |
| **LOC (test)**     | ~3,500      |

## 🤝 Contribución

### **Guidelines de Código**

1. ✅ Usar **Google Java Format** (Spotless lo aplica automáticamente)
2. ✅ Seguir **Clean Code** principles
3. ✅ **80%+ test coverage** en nueva funcionalidad
4. ✅ **Documentation** en JavaDoc para APIs públicas
5. ✅ Commits descriptivos en español

### **Guidelines de Testing**

1. ✅ **Test pyramid**: Más unitarios → Menos integración → Mínimo E2E
2. ✅ **Test names**: Descriptivos con `should` prefix
3. ✅ **AAA pattern**: Arrange → Act → Assert claramente separados
4. ✅ **Independent tests**: Sin dependencias ni orden específico
5. ✅ **Fast feedback**: Tests deben correr en < 10 segundos
6. ✅ **Use MockUtils**: Para objetos de prueba consistentes
7. ✅ **Use TestConstants**: Para valores compartidos

### **Pull Request Process**

```bash
# 1. Crear branch
git checkout -b feature/nueva-funcionalidad

# 2. Desarrollar con TDD
mvn test # Ejecutar frecuentemente

# 3. Verificar coverage
mvn verify

# 4. Formatear código
mvn spotless:apply

# 5. Commit y push
git commit -m "feat: agregar nueva funcionalidad con tests"
git push origin feature/nueva-funcionalidad

# 6. Crear Pull Request en GitHub
```

## 📚 Referencias y Recursos

### **Documentación Oficial**

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [RestAssured Documentation](https://rest-assured.io/)
- [Testcontainers](https://www.testcontainers.org/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### **Arquitectura y Patrones**

- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)
- [FIRST Principles](https://agileinaflash.blogspot.com/2009/02/first.html)

### **Java y Spring**

- [Lombok Documentation](https://projectlombok.org/features/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)

## 🏆 Características Destacadas

### **✨ Optimizaciones Implementadas**

- 🚀 **@TestInstance(PER_CLASS)** - Permite @BeforeAll no estático para acceder a campos inyectados
- 🧹 **@SneakyThrows** - Elimina boilerplate de throws Exception en tests
- 🏭 **MockUtils** - Factory centralizada para objetos mock consistentes
- 📦 **TestConstants** - Constantes compartidas evitan valores mágicos
- 🗺️ **Map.of()** - Mapas inmutables en lugar de HashMap
- 🎯 **Excepciones parametrizadas** - Sin reflection, instancias directas
- 🔄 **RestAssured.reset()** - Limpieza de configuración en @AfterAll

### **🎯 Lo que hace único a este proyecto**

1. **Coverage superior a 85%** con exclusiones inteligentes
2. **Organización perfecta** con utilidades centralizadas
3. **Tests mantenibles** usando patrones modernos
4. **Documentación completa** en código y README
5. **CI/CD ready** con Maven y GitHub Actions compatible
6. **Demostración práctica** de todos los conceptos importantes

---

## 📄 Licencia

Este proyecto es de **código abierto** y está disponible para fines educativos y de demostración.

---

## 👨‍💻 Autor

**Proyecto de demostración** - Capabilities Testing Showcase

Para consultas o sugerencias, por favor abre un issue en GitHub.

---

**⭐ Si este proyecto te resulta útil, considera darle una estrella en GitHub!**

**Desarrollado con ❤️ para demostrar las mejores prácticas de testing en Spring Boot 3 y Java 21**
