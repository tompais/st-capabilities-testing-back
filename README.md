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

### **3. Tests de Integración E2E con H2 + Redis** (`@SpringBootTest`)

```java

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedRedisStandalone
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserIntegrationTest {

    private MockWebServer mockWebServer;

    @SneakyThrows
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

### **4. Tests de Integración con MongoDB Flapdoodle** (`@SpringBootTest`)

```java
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Product Integration Tests - MongoDB with Flapdoodle")
class ProductIntegrationTest {
    
    @Autowired private ProductRepository productRepository;
    
    @BeforeAll
    void setUpAll() {
        RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
    }
    
    @AfterAll
    void tearDownAll() {
        RestAssuredMockMvc.reset();
    }
    
    @SneakyThrows
    @Test
    @DisplayName("Should create product and persist in MongoDB")
    void shouldCreateProductAndPersistInMongoDB() {
        // Arrange - Usar MockUtils
        CreateProductRequest request = MockUtils.mockCreateProductRequest(
            "Laptop Gaming",
            "High performance gaming laptop",
            BigDecimal.valueOf(1299.99),
            Product.Category.ELECTRONICS,
            15,
            "LAP-GAM-001"
        );
        
        // Act - Crear producto via API REST
        given()
            .contentType(ContentType.JSON)
            .body(objectMapper.writeValueAsString(request))
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("name", equalTo("Laptop Gaming"))
            .body("category", equalTo("ELECTRONICS"))
            .body("available", equalTo(true));
    }
}
```

### **5. Tests Parametrizados** (Múltiples fuentes)

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

// @MethodSource - Datos complejos (excepciones ya construidas)
@ParameterizedTest
@MethodSource("provideExceptionMappingData")
void shouldMapExceptions(Exception exception, HttpStatus expectedStatus) {
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

    // DTOs Web - Usuarios
    public static UserResponse mockUserResponse() { /* por defecto */ }

    public static UserResponse mockUserResponse(UUID id) { /* con ID */ }

    public static UserResponse mockUserResponse(boolean active) { /* con estado */ }

    public static CreateUserRequest mockCreateUserRequest() { /* por defecto */ }

    public static CreateUserRequest mockCreateUserRequest(String username, String email) { /* básico */ }

    // Productos
    public static Product mockProduct() { /* por defecto */ }

    public static Product mockProduct(UUID id) { /* con ID */ }

    public static Product mockProduct(Product.Category category) { /* con categoría */ }

    // DTOs Web - Productos
    public static ProductResponse mockProductResponse() { /* por defecto */ }

    public static ProductResponse mockProductResponse(boolean available) { /* con disponibilidad */ }

    public static CreateProductRequest mockCreateProductRequest() { /* por defecto */ }

    public static CreateProductRequest mockCreateProductRequest(String name, BigDecimal price) { /* básico */ }

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
    public static final String PRODUCT_SKU = "TEST-SKU-001";

    // Customer constants
    public static final UUID CUSTOMER_ID = UUID.fromString("323e4567-...");

    // Cache keys
    public static final String CACHE_KEY_USER_PREFIX = "user:";
    public static final String CACHE_KEY_PRODUCT_PREFIX = "product:";
}
```

## 🚀 Funcionalidades Implementadas

### **Modelos del Dominio**

#### **User (JPA/H2)**

```java
@Data
@Builder
@Entity
public class User {
    @Id private UUID id;
    
    @NotBlank @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    private String username;
    
    @Email @NotBlank
    private String email;
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    public enum Status { ACTIVE, SUSPENDED, INACTIVE }
    
    public boolean isActive() {
        return status == Status.ACTIVE;
    }
}
```

#### **Product (MongoDB)**

```java
@Data
@Builder
@Document(collection = "products")
public class Product {
    @Id private UUID id;
    private String name;
    private BigDecimal price;
    private Category category;
    private Integer stock;
    private Boolean active;
    
    public enum Category {
        ELECTRONICS, CLOTHING, BOOKS, SPORTS, HOME, OTHER
    }
    
    public boolean isAvailable() {
        return active && stock != null && stock > 0;
    }
}
```

### **Persistencia Multi-Base de Datos**

- **H2 (JPA)**: Usuarios con transacciones ACID y queries derivadas
- **MongoDB Flapdoodle**: Productos NoSQL embebido sin Docker
- **Redis**: Cache distribuido con TTL para mejora de performance

### **Web Layer con DTOs Records**

```java
// Record de respuesta (Java 17+)
public record UserResponse(
                UUID id,
                String email,
                String name,
                String phone,
                boolean active
        ) {
}

// Record de request con validaciones
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

### **Ejecutar Tests y Coverage**

```bash
# Ejecutar todos los tests
mvn clean test

# Generar reporte de coverage
mvn clean verify

# Ver reporte HTML
open target/site/jacoco/index.html
```

### **Exclusiones de Coverage**

```xml

<excludes>
    <!-- Entidades sin lógica -->
    <exclude>**/infrastructure/persistence/jpa/entity/**</exclude>
    <exclude>**/infrastructure/persistence/mongodb/entity/**</exclude>

    <!-- DTOs y Records -->
    <exclude>**/infrastructure/web/dto/**</exclude>

    <!-- Configuración -->
    <exclude>**/infrastructure/config/**</exclude>
    <exclude>**/CapabilitiesTestingApplication.class</exclude>

    <!-- Mappers simples -->
    <exclude>**/infrastructure/persistence/mapper/**</exclude>
</excludes>
```

## 🎯 Mejores Prácticas Aplicadas

### **1. Principios FIRST**

- ✅ **Fast**: Tests rápidos con bases de datos embebidas
- ✅ **Independent**: Cada test es independiente con setUp/tearDown
- ✅ **Repeatable**: Mismo resultado en cualquier entorno
- ✅ **Self-validating**: Assert claro de éxito/fallo
- ✅ **Timely**: Tests escritos junto con el código

### **2. Patrón AAA (Arrange-Act-Assert)**

```java
@Test
void shouldCreateUser() {
    // Arrange - Preparar datos
    CreateUserRequest request = MockUtils.mockCreateUserRequest();
    when(repository.save(any())).thenReturn(user);
    
    // Act - Ejecutar acción
    UserResponse result = service.createUser(request);
    
    // Assert - Verificar resultado
    assertThat(result.email()).isEqualTo(USER_EMAIL);
    verify(repository).save(any(User.class));
}
```

### **3. DRY con MockUtils y TestConstants**

- ❌ **Antes**: Repetir builders en cada test
- ✅ **Ahora**: `MockUtils.mockUser()` / `TestConstants.USER_ID`

### **4. @TestInstance.PER_CLASS para Setup Único**

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MyTest {
    
    @BeforeAll  // No necesita ser static
    void setUpAll() {
        // Configuración una sola vez
        RestAssuredMockMvc.webAppContextSetup(context);
    }
    
    @AfterAll
    void tearDownAll() {
        // Limpieza al final
        RestAssuredMockMvc.reset();
    }
}
```

### **5. @SneakyThrows para Tests Limpios**

```java
@SneakyThrows  // En lugar de throws Exception
@Test
void shouldDoSomething() {
    mockWebServer.enqueue(new MockResponse()...)
    String json = objectMapper.writeValueAsString(request);
    // No necesita try-catch ni throws
}
```

### **6. Map.of para Datos Inmutables**

```java
// ❌ Antes
Map<String, Object> data = new HashMap<>();
data.put("id", id);
data.put("active", true);

// ✅ Ahora
Map<String, Object> data = Map.of(
    "id", id,
    "active", true,
    "riskLevel", "LOW"
);
```

## 📦 Estructura de Tests

```
src/test/java/
├── application/service/
│   ├── UserServiceTest.java              # Tests unitarios con @Mock
│   ├── ProductServiceTest.java
│   └── CustomerValidationServiceTest.java
│
├── infrastructure/
│   ├── integration/
│   │   ├── UserIntegrationTest.java      # H2 + Redis + MockWebServer
│   │   └── ProductIntegrationTest.java   # MongoDB Flapdoodle embebido
│   │
│   └── web/
│       ├── controller/
│       │   ├── UserControllerTest.java   # @WebMvcTest + RestAssured
│       │   └── ProductControllerTest.java
│       │
│       └── service/
│           ├── UserWebServiceTest.java   # Tests parametrizados
│           └── ProductWebServiceTest.java
│
└── utils/
    ├── MockUtils.java                    # Factory centralizada
    └── TestConstants.java                # Constantes compartidas
```

## 🚀 Comenzar

### **Requisitos**

- Java 21+
- Maven 3.9+

### **Ejecutar la aplicación**

```bash
# Clonar el repositorio
git clone <repository-url>
cd capabilities-testing

# Ejecutar tests
mvn clean test

# Ejecutar con coverage
mvn clean verify

# Ejecutar la aplicación
mvn spring-boot:run
```

### **Endpoints disponibles**

#### **Users (H2/JPA)**

```http
POST   /api/users              - Crear usuario
GET    /api/users/{id}         - Obtener usuario
GET    /api/users/active       - Listar usuarios activos
PUT    /api/users/{id}/status  - Actualizar estado
DELETE /api/users/{id}         - Eliminar usuario
```

#### **Products (MongoDB)**

```http
POST   /api/products           - Crear producto
GET    /api/products/{id}      - Obtener producto
GET    /api/products/search    - Buscar por categoría
GET    /api/products/active    - Listar productos activos
PUT    /api/products/{id}/stock - Actualizar stock
DELETE /api/products/{id}      - Eliminar producto
```

## 📚 Recursos y Referencias

- [Spring Boot Testing](https://spring.io/guides/gs/testing-web)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [RestAssured](https://rest-assured.io/)
- [AssertJ](https://assertj.github.io/doc/)
- [Flapdoodle MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo.spring)
- [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/)

## 🤝 Contribuir

Este proyecto es un showcase de testing. Para mejoras:

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📝 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.

---

**Desarrollado con ❤️ para demostrar las mejores prácticas de testing en Spring Boot**
