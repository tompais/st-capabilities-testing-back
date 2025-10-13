# 🧪 Capabilities Testing - Spring Boot Testing Showcase

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue)
![JUnit](https://img.shields.io/badge/JUnit-5-green)
![Coverage](https://img.shields.io/badge/Coverage-80%25+-brightgreen)

Este proyecto demuestra las **mejores prácticas y capacidades avanzadas de testing** en Spring Boot, implementando una **arquitectura hexagonal limpia** con testing completo usando las tecnologías más modernas.

## 🎯 Objetivos del Proyecto

- **Demostrar testing completo** con múltiples tecnologías integradas
- **Implementar Clean Architecture** siguiendo principios SOLID y DDD  
- **Aplicar mejores prácticas** de testing (FIRST, AAA, etc.)
- **Mostrar integración real** de bases de datos, cache y servicios externos
- **Usar tecnologías modernas** (UUIDs, inner classes, validation, etc.)

## 🏗️ Arquitectura

```
src/main/java/
├── domain/                          # 🎯 Lógica de negocio pura
│   ├── model/                       # Entidades del dominio
│   └── port/                        # Interfaces (puertos)
├── application/                     # 🔧 Casos de uso
│   └── service/                     # Servicios de aplicación  
└── infrastructure/                  # 🔌 Implementaciones técnicas
    ├── adapter/                     # Adaptadores externos
    ├── persistence/                 # Persistencia (JPA + MongoDB)
    ├── web/                         # Controllers REST
    └── config/                      # Configuraciones
```

## 🛠️ Stack Tecnológico

### **Core Framework**
- **Spring Boot 3.5.6** - Framework principal
- **Java 21** - Versión LTS más reciente
- **Maven** - Gestión de dependencias

### **Testing Technologies** 🧪
| Tecnología | Propósito | Casos de Uso |
|------------|-----------|--------------|
| **JUnit 5** | Framework base | Tests unitarios y de integración |
| **RestAssured** | Testing API REST | Tests de endpoints elegantes |
| **AssertJ** | Assertions fluidas | Verificaciones expresivas |
| **Mockito** | Mocking | Tests unitarios aislados |
| **Testcontainers** | Integración real | MongoDB en contenedores |
| **Embedded Redis** | Cache testing | Tests de caché distribuido |
| **MockWebServer** | Servicios externos | Mock de APIs HTTP |
| **H2 Database** | Base de datos test | JPA en memoria |
| **Flapdoodle MongoDB** | MongoDB embebido | Tests NoSQL |

### **Validation & Quality** ✅
- **Spring Validation** - Validación declarativa
- **JaCoCo** - Coverage de código (configurado 80%+)
- **Spotless** - Formateo automático
- **Log4j2** - Logging estructurado

## 🧪 Estrategia de Testing

### **1. Tests Unitarios** 
- **Dominio**: Lógica de negocio pura
- **Servicios**: Casos de uso con mocks
- **Validaciones**: Business rules y constraints

### **2. Tests de Integración**
- **Controllers**: API REST con RestAssured MockMvc
- **Repositories**: Persistencia real con H2/Testcontainers  
- **Cache**: Funcionalidad Redis embebido

### **3. Tests End-to-End**
- **Ciclo completo**: Usuario → Base datos → Cache → Servicios externos
- **Scenarios reales**: Flujos de negocio completos
- **Múltiples tecnologías**: Integración total

### **4. Tests Parametrizados**
- **@ValueSource**: Arrays de valores simples
- **@CsvSource**: Datos tabulares con CSV
- **@EnumSource**: Todos los valores de enums
- **@MethodSource**: Proveedores de datos complejos

## 📊 Coverage Strategy

### **Incluido en Coverage (80%+)**
✅ **Domain models** - Lógica de negocio  
✅ **Application services** - Casos de uso  
✅ **Infrastructure adapters** - Lógica de adaptación  
✅ **Controllers** - Validaciones y mapeos  

### **Excluido del Coverage** 
❌ **Entidades JPA/MongoDB** - Solo datos, sin lógica  
❌ **DTOs** - Transferencia de datos pura  
❌ **Configuraciones** - Sin lógica de negocio  
❌ **Repositorios Spring Data** - Generados automáticamente  
❌ **Mappers simples** - Solo conversión de datos  

## 🚀 Funcionalidades Implementadas

### **Modelos del Dominio** 
- **User**: Sistema de usuarios con estados (Active/Suspended/Inactive)
- **Product**: Catálogo con categorías y gestión de stock  
- **ExternalCustomer**: Validación de clientes con niveles de riesgo

### **Persistencia Multi-Base**
- **JPA (H2)**: Usuarios con transacciones ACID
- **MongoDB**: Productos con escalabilidad NoSQL  
- **Redis**: Cache distribuido para performance

### **Validaciones Declarativas**
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50)
@Pattern(regexp = "^[a-zA-Z0-9_-]+$")
private String username;

@Email(message = "Email must be valid")  
@Size(max = 100)
private String email;

@Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
private String phoneNumber;
```

### **Inner Classes & Enums**
```java
public enum Status {
    ACTIVE, SUSPENDED, INACTIVE
}

public enum RiskLevel {
    LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);
    
    private final int priority;
}
```

## 🧪 Ejemplos de Tests

### **Parameterized Test con múltiples fuentes**
```java
@ParameterizedTest(name = "Email ''{0}'' should be valid")
@ValueSource(strings = {
    "user@santander.com",
    "admin@santandertecnologia.com"
})
void shouldValidateEmailFormats(String email) {
    assertThat(email).contains("@").contains(".");
}

@ParameterizedTest
@CsvSource({
    "ACTIVE, true",
    "SUSPENDED, false", 
    "INACTIVE, false"
})
void shouldDetermineUserActiveStatus(Status status, boolean expectedActive) {
    // Test implementation
}
```

### **Integration Test completo**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@EnableEmbeddedRedis
class CompleteIntegrationTest {
    
    @Container
    static MongoDBContainer mongoDBContainer = 
        new MongoDBContainer("mongo:7.0");
        
    private static MockWebServer mockWebServer;
    
    @Test
    void shouldCompleteFullUserLifecycle() {
        // Crear → Cachear → Validar Externo → Actualizar → Eliminar
    }
}
```

### **Unit Test con Mockito y AssertJ**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;
    
    @Test
    void shouldCreateUserSuccessfully() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User result = userService.createUser(testUser);
        
        // Assert
        assertThat(result.isActive()).isTrue();
        verify(userRepository).save(any(User.class));
    }
}
```

## 🚀 Getting Started

### **Prerrequisitos**
- Java 21+
- Maven 3.9+
- Docker (para Testcontainers)

### **Instalación y Ejecución**
```bash
# Clonar el proyecto
git clone <repository-url>
cd capabilities-testing

# Ejecutar todos los tests
mvn clean test

# Generar reporte de coverage  
mvn clean verify

# Ver reporte (se abre en navegador)
open target/site/jacoco/index.html

# Ejecutar aplicación
mvn spring-boot:run
```

### **Tests por Categoría**
```bash
# Solo tests unitarios
mvn test

# Solo tests de integración  
mvn failsafe:integration-test

# Tests con coverage
mvn clean verify

# Tests específicos
mvn test -Dtest="*ParameterizedTest"
mvn test -Dtest="UserServiceTest"
```

## 📊 Métricas y Calidad

### **Coverage Requirements**
- **Mínimo**: 80% instruction coverage
- **Objetivo**: 85%+ en lógica de negocio
- **Exclusiones**: Configuradas en JaCoCo

### **Code Quality**
- **Spotless**: Formateo Google Style automático
- **Validation**: JSR-303 en todas las capas  
- **Logging**: Estructurado con Log4j2
- **Documentation**: JavaDoc completo

## 🎓 Conceptos Demostrados

### **Testing Patterns**
- ✅ **FIRST Principles** - Fast, Independent, Repeatable, Self-validating, Timely
- ✅ **AAA Pattern** - Arrange, Act, Assert en todos los tests  
- ✅ **Test Doubles** - Mocks, Stubs, Fakes apropiados
- ✅ **Test Data Builders** - Construcción de datos elegante

### **Spring Boot Testing**
- ✅ **@SpringBootTest** - Tests de integración completos
- ✅ **@WebMvcTest** - Tests de controladores aislados  
- ✅ **@DataJpaTest** - Tests de repositorios JPA
- ✅ **@TestConfiguration** - Configuraciones específicas

### **Advanced Features**
- ✅ **Parameterized Tests** - Múltiples fuentes de datos
- ✅ **Dynamic Tests** - Generación runtime  
- ✅ **Nested Tests** - Organización jerárquica
- ✅ **Custom Extensions** - Lógica reutilizable

## 🔧 Configuración de Testing

### **JaCoCo Configuration**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <excludes>
            <exclude>**/infrastructure/persistence/jpa/entity/**</exclude>
            <exclude>**/infrastructure/web/dto/**</exclude>
            <exclude>**/infrastructure/config/**</exclude>
        </excludes>
    </configuration>
</plugin>
```

### **Test Properties**
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  data:
    mongodb:
      database: testing
  cache:
    type: redis
    
logging:
  level:
    com.santandertecnologia: DEBUG
```

## 🤝 Contribución

### **Code Style**
- Usar **Google Java Format**
- Seguir **Clean Code** principles  
- **100% test coverage** en nueva funcionalidad
- **Documentation** en español

### **Testing Guidelines**
1. **Test pyramid**: Más unitarios, menos E2E
2. **Test names**: Descriptivos en inglés  
3. **AAA pattern**: Estructura clara siempre
4. **Independent tests**: Sin dependencias entre tests
5. **Fast feedback**: Tests rápidos y confiables

## 📚 Referencias

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

**Desarrollado con ❤️ para demostrar las mejores prácticas de testing en Spring Boot**
