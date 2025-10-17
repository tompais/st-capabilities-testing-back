# Guía de Calidad de Código - Spotless + Checkstyle

## Herramientas Integradas

Este proyecto utiliza **Spotless** y **Checkstyle** de forma complementaria:

- **Spotless**: Maneja el formateo visual del código (Google Java Format) - se ejecuta en fase `validate`
- **Checkstyle**: Valida reglas de calidad y estilo de código - se ejecuta en fase `verify`
- **Sin conflictos**: Cada herramienta tiene su propósito específico

## Reglas Activas

### 🔍 Detección de Código No Utilizado

- ✅ Imports no utilizados
- ✅ Variables locales no utilizadas
- ✅ Parámetros de métodos no utilizados
- ✅ Campos privados no utilizados
- ✅ Métodos privados no utilizados
- ✅ Variables catch no utilizadas

### 🔒 Uso Obligatorio de `final`

- ✅ **Parámetros de métodos deben ser `final`**
- ✅ **Variables locales deben ser `final`** cuando no cambian
- ⚠️ Excepciones: DTOs, Entidades, clases con Lombok

### 💡 Preferencia de `var`

- ℹ️ Sugiere usar `var` cuando el tipo es obvio del lado derecho
- Ejemplo: `var user = new User()` en lugar de `User user = new User()`

### 🎯 Otras Reglas de Calidad

- Evitar imports con `*`
- Detectar bloques vacíos
- Detectar comparaciones simplificables
- Un statement por línea
- Detectar `equals()` y `hashCode()` mal implementados

## Comandos Maven

### Verificar formateo con Spotless (sin modificar)

```bash
mvn spotless:check
```

### Formatear código automáticamente con Spotless

```bash
mvn spotless:apply
```

### Ejecutar Checkstyle manualmente

```bash
mvn checkstyle:check
```

### Ver reporte HTML de Checkstyle

```bash
mvn checkstyle:checkstyle
# El reporte estará en: target/site/checkstyle.html
```

### Build completo (incluye todas las validaciones)

```bash
mvn clean install
# Ejecuta: Spotless → Compilación → Tests → Checkstyle → JaCoCo
```

## Comportamiento del Build

### ✅ Spotless (fase `validate`)

- **FALLA el build** si el código no está formateado correctamente
- Solución: `mvn spotless:apply` para auto-formatear

### ⚠️ Checkstyle (fase `verify`)

- **NO falla el build**, solo muestra warnings en consola
- Los warnings aparecen al final del build
- Ideal para ir mejorando gradualmente el código

## ¿Qué hacer cuando aparecen warnings de Checkstyle?

### Ejemplo 1: Parámetro sin `final`

```java
// ⚠️ WARNING: Parameter 'name' should be declared final
public void processUser(String name) {
    // ...
}

// ✅ CORRECTO
public void processUser(final String name) {
    // ...
}
```

### Ejemplo 2: Variable local sin `final`

```java
// ⚠️ WARNING: Variable 'result' should be declared final
public void example() {
    String result = "test";
    System.out.println(result);
}

// ✅ CORRECTO
public void example() {
    final String result = "test";
    System.out.println(result);
}
```

### Ejemplo 3: Preferir `var`

```java
// ⚠️ PUEDE MEJORARSE
List<String> names = new ArrayList<>();
User user = new User();

// ✅ MEJOR
var names = new ArrayList<String>();
var user = new User();
```

### Ejemplo 4: Variable no utilizada

```java
// ⚠️ WARNING: Unused local variable 'unused'
public void example() {
    final String unused = "test";
    System.out.println("Hello");
}

// ✅ CORRECTO
public void example() {
    System.out.println("Hello");
}
```

## Supresiones Automáticas

Checkstyle NO aplicará reglas estrictas en:

- **DTOs**: `src/*/java/**/dto/**/*.java`
- **Entidades**: `src/*/java/**/entity/**/*.java`
- **Configuraciones**: `*Config.java`
- **Utils de Test**: `*Utils.java`
- **Tests**: `*Test.java`

## Flujo de Trabajo Recomendado

### Durante el desarrollo:

1. Escribe tu código normalmente
2. Antes de commit, ejecuta: `mvn spotless:apply`
3. Ejecuta: `mvn clean install`
4. Revisa los warnings de Checkstyle (si los hay)
5. Opcionalmente corrige los warnings

### En CI/CD:

```bash
mvn clean install
# Spotless fallará si el código no está formateado
# Checkstyle mostrará warnings pero no fallará el build
```

## Hacer que Checkstyle falle el build (opcional)

Si quieres que Checkstyle falle el build en CI/CD, edita `pom.xml`:

```xml
<configuration>
    <failsOnError>true</failsOnError>
    <failOnViolation>true</failOnViolation>
    <violationSeverity>warning</violationSeverity>
</configuration>
```

## Tips

1. **Antes de commit**: Ejecuta `mvn spotless:apply`
2. **En tu IDE**: Configura save actions para agregar `final` automáticamente
3. **Ver todos los warnings**: `mvn checkstyle:checkstyle` genera reporte HTML detallado
4. **Ignorar warnings específicos**: Usa `@SuppressWarnings("checkstyle:RuleName")` cuando sea necesario

## Configuración del IDE

### IntelliJ IDEA

1. **Formateo**: Settings → Editor → Code Style → Java → Import Scheme → Google Style
2. **Inspections**: Settings → Editor → Inspections → Java
    - Habilitar "Local variable or parameter can be final"
    - Habilitar "Unused declaration"
3. **Save Actions**: Instalar plugin "Save Actions" para auto-agregar `final`

### Eclipse

1. **Formateo**: Preferences → Java → Code Style → Formatter → Import Google Style
2. **Warnings**: Preferences → Java → Compiler → Errors/Warnings
    - "Local variable is never read" → Warning
    - "Parameter is never read" → Warning
