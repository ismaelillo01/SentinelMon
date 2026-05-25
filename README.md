# Manual de desarrollo de plugins — SentinelMon

Guía para crear plugins funcionales compatibles con SentinelMon.

---

## Índice

1. [Arquitectura del sistema de plugins](#arquitectura-del-sistema-de-plugins)
2. [Estructura de carpetas (obligatoria)](#estructura-de-carpetas-obligatoria)
3. [Requisitos mínimos](#requisitos-mínimos)
4. [Paso a paso: crear un plugin](#paso-a-paso-crear-un-plugin)
5. [Interfaz SentinelExtension](#interfaz-sentinelextension)
6. [MANIFEST.MF — campos obligatorios](#manifestmf--campos-obligatorios)
7. [POM mínimo del plugin](#pom-mínimo-del-plugin)
8. [Ejemplo completo: plugin "hello-world"](#ejemplo-completo-plugin-hello-world)
9. [Compilar y empaquetar](#compilar-y-empaquetar)
10. [Instalar el plugin](#instalar-el-plugin)
11. [Errores comunes](#errores-comunes)

---

## Arquitectura del sistema de plugins

SentinelMon usa **PF4J** (Plugin Framework for Java) versión **3.15.0** como motor de plugins.

El flujo es:

```
JAR del plugin
    ↓
PF4J lee el MANIFEST.MF del JAR
    ↓
Busca clases anotadas con @Extension que implementen SentinelExtension
    ↓
SentinelPluginManager las carga, las arranca, y las registra en la BD
    ↓
El Dashboard muestra el componente UI del plugin
```

El módulo **sentinel-api** contiene la interfaz `SentinelExtension` que todo plugin debe implementar. Tu plugin solo necesita depender de este módulo y de PF4J.

---

## Estructura de carpetas (obligatoria)

> **⚠️ IMPORTANTE**: PF4J espera que cada plugin esté en su propia subcarpeta dentro de `plugins/`. Si el JAR se coloca directamente en `plugins/` sin subcarpeta, PF4J no lo detectará correctamente.

```
SentinelMon/
├── plugins/                          ← carpeta raíz de plugins
│   ├── mi-plugin/                    ← subcarpeta con el id del plugin
│   │   └── mi-plugin-1.0.0.jar      ← JAR del plugin
│   ├── otro-plugin/
│   │   └── otro-plugin-2.1.0.jar
│   └── ...
├── core/
├── sentinel-api/
├── sentinel-ui/
└── pom.xml
```

### Reglas de la estructura

| Regla | Descripción |
|-------|-------------|
| **Subcarpeta obligatoria** | El nombre de la subcarpeta debe coincidir con el `Plugin-Id` del MANIFEST.MF |
| **Un JAR por subcarpeta** | SentinelMon solo coge el primer `.jar` que encuentra en cada subcarpeta |
| **No poner JARs sueltos** | Un JAR directamente en `plugins/` (sin subcarpeta) será ignorado |

Cuando se instala un plugin desde la tienda (botón "Instalar JAR"), SentinelMon crea automáticamente la subcarpeta `plugins/<plugin-id>/` y copia el JAR ahí dentro.

---

## Requisitos mínimos

Para que un plugin funcione necesitas **exactamente 3 cosas**:

1. **Una clase Plugin** que extienda `org.pf4j.Plugin`
2. **Una clase Extension** anotada con `@Extension` que implemente `SentinelExtension`
3. **Un MANIFEST.MF** con los metadatos del plugin (al menos `Plugin-Id` y `Plugin-Version`)

Dependencias necesarias (solo en compilación, `provided`):

| Dependencia | Versión | Scope |
|-------------|---------|-------|
| `org.pf4j:pf4j` | 3.15.0 | provided |
| `org.example:sentinel-api` | 1.0-SNAPSHOT | provided |
| `org.openjfx:javafx-controls` | 21.0.2 | provided |

> Las dependencias son `provided` porque SentinelMon ya las tiene cargadas en memoria. El JAR del plugin no debe incluirlas.

---

## Paso a paso: crear un plugin

### 1. Crear proyecto Maven

Crea un proyecto Maven nuevo fuera del repositorio de SentinelMon (o donde quieras):

```
mi-plugin/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/miplugin/
│       │       ├── MiPlugin.java           ← clase Plugin (PF4J)
│       │       └── MiExtension.java        ← clase Extension (tu lógica)
│       └── resources/
│           └── META-INF/
│               └── MANIFEST.MF             ← metadatos del plugin (se genera con Maven)
└── pom.xml
```

### 2. Crear la clase Plugin

Esta clase es el punto de entrada de PF4J. Puede estar vacía si no necesitas lógica de arranque/parada a nivel de plugin:

```java
package com.miplugin;

import org.pf4j.Plugin;

public class MiPlugin extends Plugin {
    // PF4J necesita esta clase como punto de entrada
    // no hace falta nada más aquí
}
```

### 3. Crear la clase Extension

Esta es la clase importante. Debe:
- Estar anotada con `@Extension`
- Implementar `SentinelExtension`

```java
package com.miplugin;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.api.SentinelExtension;
import org.pf4j.Extension;

@Extension
public class MiExtension implements SentinelExtension {

    @Override
    public String getName() {
        return "Mi Plugin";
    }

    @Override
    public String getDescription() {
        return "Descripción de lo que hace mi plugin";
    }

    @Override
    public void start() {
        // se llama cuando el plugin se activa
    }

    @Override
    public void stop() {
        // se llama cuando el plugin se desactiva
        // liberar recursos aquí
    }

    @Override
    public Node getUiComponent() {
        // devuelve el componente JavaFX que se mostrará en el dashboard
        VBox caja = new VBox(10);
        Label titulo = new Label("Mi Plugin");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label contenido = new Label("Esto viene del plugin");
        contenido.setStyle("-fx-text-fill: #8b92b3; -fx-font-size: 13px;");
        caja.getChildren().addAll(titulo, contenido);
        return caja;
    }
}
```

### 4. Configurar el POM y el MANIFEST

Ver secciones siguientes.

### 5. Compilar y empaquetar

```bash
mvn clean package
```

### 6. Instalar

Desde la tienda de SentinelMon → botón "Instalar JAR" → seleccionar el `.jar` generado en `target/`.

---

## Interfaz SentinelExtension

Todos los plugins deben implementar esta interfaz definida en el módulo `sentinel-api`:

```java
public interface SentinelExtension extends ExtensionPoint {
    String getName();           // nombre visible del plugin
    String getDescription();    // descripción corta
    void start();               // llamado al activar el plugin
    void stop();                // llamado al desactivar el plugin
    Node getUiComponent();      // componente JavaFX para el dashboard
}
```

| Método | Cuándo se llama | Qué devolver |
|--------|----------------|--------------|
| `getName()` | Al listar extensiones | Nombre corto, ej: `"Alertas"` |
| `getDescription()` | En la tienda | Texto descriptivo |
| `start()` | Al cargar el plugin | Iniciar hilos, conexiones, etc. |
| `stop()` | Al cerrar la app o desinstalar | Liberar recursos |
| `getUiComponent()` | Al construir el dashboard | Cualquier `javafx.scene.Node` o `null` si no tiene UI |

### Sobre getUiComponent()

- El `Node` devuelto se añade al contenedor `pluginsContainer` del dashboard (un `VBox` al final del scroll)
- Si tu plugin no tiene interfaz gráfica, devuelve `null`
- Puedes devolver cualquier nodo de JavaFX: `Label`, `VBox`, `HBox`, `Button`, etc.
- Se recomienda usar los estilos CSS de SentinelMon para mantener coherencia visual (ver clases como `card`, `metric-label`, `metric-value-small`, etc.)

---

## MANIFEST.MF — campos obligatorios

PF4J lee los metadatos del plugin desde el `MANIFEST.MF` dentro del JAR. Estos son los campos:

| Campo | Obligatorio | Descripción | Ejemplo |
|-------|:-----------:|-------------|---------|
| `Plugin-Id` | ✅ | Identificador único (sin espacios, kebab-case) | `mi-plugin` |
| `Plugin-Version` | ✅ | Versión del plugin | `1.0.0` |
| `Plugin-Class` | ✅ | Clase que extiende `org.pf4j.Plugin` | `com.miplugin.MiPlugin` |
| `Plugin-Description` | ❌ | Descripción (se muestra en la tienda) | `Plugin de alertas` |
| `Plugin-Provider` | ❌ | Autor o proveedor | `Tu Nombre` |

> ** Sin `Plugin-Id` el plugin NO se cargará.** SentinelMon comprueba que este campo exista antes de instalar.

El MANIFEST.MF se genera automáticamente con el plugin `maven-jar-plugin` en el POM (ver siguiente sección).

---

## POM mínimo del plugin

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.miplugin</groupId>
    <artifactId>mi-plugin</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <pf4j.version>3.15.0</pf4j.version>
        <javafx.version>21.0.2</javafx.version>
    </properties>

    <dependencies>
        <!-- API de SentinelMon (interfaz SentinelExtension) -->
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>sentinel-api</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>

        <!-- PF4J -->
        <dependency>
            <groupId>org.pf4j</groupId>
            <artifactId>pf4j</artifactId>
            <version>${pf4j.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- JavaFX (para crear componentes UI) -->
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Genera el MANIFEST.MF con los metadatos del plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Plugin-Id>mi-plugin</Plugin-Id>
                            <Plugin-Version>1.0.0</Plugin-Version>
                            <Plugin-Class>com.miplugin.MiPlugin</Plugin-Class>
                            <Plugin-Description>Descripción de mi plugin</Plugin-Description>
                            <Plugin-Provider>Tu Nombre</Plugin-Provider>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Notas sobre el POM

- **`scope: provided`** en las 3 dependencias — esto es clave. SentinelMon ya tiene estas librerías cargadas. Si las empaquetas dentro del JAR habrá conflictos de clases.
- **`sentinel-api` debe estar instalada** en tu repositorio local de Maven. Para eso, ejecuta `mvn install` en el proyecto SentinelMon antes de compilar tu plugin.
- **`Plugin-Id` y `artifactId`** deberían coincidir para mantener consistencia.

---

## Ejemplo completo: plugin "hello-world"

### Estructura del proyecto

```
hello-world/
├── src/main/java/com/helloworld/
│   ├── HelloWorldPlugin.java
│   └── HelloWorldExtension.java
└── pom.xml
```

### HelloWorldPlugin.java

```java
package com.helloworld;

import org.pf4j.Plugin;

public class HelloWorldPlugin extends Plugin {
    // punto de entrada para PF4J
}
```

### HelloWorldExtension.java

```java
package com.helloworld;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.api.SentinelExtension;
import org.pf4j.Extension;

@Extension
public class HelloWorldExtension implements SentinelExtension {

    @Override
    public String getName() {
        return "Hello World";
    }

    @Override
    public String getDescription() {
        return "Plugin de ejemplo que muestra un saludo";
    }

    @Override
    public void start() {
        System.out.println("Hello World plugin arrancado");
    }

    @Override
    public void stop() {
        System.out.println("Hello World plugin parado");
    }

    @Override
    public Node getUiComponent() {
        VBox card = new VBox(10);
        card.setStyle(
            "-fx-background-color: #262940;" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: #343954;" +
            "-fx-border-radius: 10;" +
            "-fx-padding: 20;"
        );

        Label titulo = new Label("HELLO WORLD");
        titulo.setStyle("-fx-text-fill: #8b92b3; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label mensaje = new Label("¡Hola desde el plugin!");
        mensaje.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label("Este es un plugin de ejemplo funcional");
        info.setStyle("-fx-text-fill: #8b92b3; -fx-font-size: 12px;");

        card.getChildren().addAll(titulo, mensaje, info);
        return card;
    }
}
```

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.helloworld</groupId>
    <artifactId>hello-world</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>sentinel-api</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.pf4j</groupId>
            <artifactId>pf4j</artifactId>
            <version>3.15.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>21.0.2</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Plugin-Id>hello-world</Plugin-Id>
                            <Plugin-Version>1.0.0</Plugin-Version>
                            <Plugin-Class>com.helloworld.HelloWorldPlugin</Plugin-Class>
                            <Plugin-Description>Plugin de ejemplo</Plugin-Description>
                            <Plugin-Provider>SentinelMon Dev</Plugin-Provider>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## Compilar y empaquetar

### Paso previo: instalar sentinel-api en tu Maven local

Desde la raíz de SentinelMon, ejecuta:

```bash
mvn install -pl sentinel-api
```

Esto instala `sentinel-api-1.0-SNAPSHOT.jar` en tu repositorio local de Maven (`~/.m2/repository/`), y permite que tu plugin lo use como dependencia `provided`.

### Compilar el plugin

Desde la carpeta del plugin:

```bash
mvn clean package
```

El JAR resultante estará en `target/hello-world-1.0.0.jar`.

### Verificar el MANIFEST

Puedes comprobar que el MANIFEST se generó bien:

```bash
jar tf target/hello-world-1.0.0.jar META-INF/MANIFEST.MF
jar xf target/hello-world-1.0.0.jar META-INF/MANIFEST.MF
cat META-INF/MANIFEST.MF
```

Deberías ver algo como:

```
Manifest-Version: 1.0
Plugin-Id: hello-world
Plugin-Version: 1.0.0
Plugin-Class: com.helloworld.HelloWorldPlugin
Plugin-Description: Plugin de ejemplo
Plugin-Provider: SentinelMon Dev
```

---

## Instalar el plugin

Hay dos formas:

### Desde la tienda (recomendado)

1. Abre SentinelMon
2. Haz clic en **MÓDULOS / APPS**
3. Haz clic en **+ Instalar JAR**
4. Selecciona el `.jar` de `target/`
5. Si todo va bien, verás "El plugin se ha instalado"

SentinelMon automáticamente:
- Crea la subcarpeta `plugins/hello-world/`
- Copia el JAR ahí dentro
- Lo carga con PF4J
- Lo guarda en la base de datos

### Manual (para desarrollo)

1. Crea la subcarpeta con el nombre exacto del `Plugin-Id`:
   ```
   plugins/hello-world/
   ```
2. Copia el JAR dentro:
   ```
   plugins/hello-world/hello-world-1.0.0.jar
   ```
3. Reinicia SentinelMon

> ** El nombre de la subcarpeta DEBE coincidir con el `Plugin-Id` del MANIFEST.MF.**

---

## Errores comunes

| Error | Causa | Solución |
|-------|-------|----------|
| `Cannot find the manifest path` | El JAR está directamente en `plugins/` sin subcarpeta | Mover el JAR a `plugins/<plugin-id>/mi-plugin.jar` |
| `El JAR no tiene Plugin-Id` | Falta `Plugin-Id` en el MANIFEST.MF | Configurar `maven-jar-plugin` con `manifestEntries` en el POM |
| `No se ha podido cargar el plugin` | La clase `Plugin-Class` no existe o no extiende `Plugin` | Verificar que el nombre en el MANIFEST coincide con la clase real |
| El plugin se instala pero no aparece en el dashboard | `getUiComponent()` devuelve `null` | Devolver un `Node` válido de JavaFX |
| `ClassNotFoundException` para `SentinelExtension` | `sentinel-api` no está como dependencia | Añadir `sentinel-api` con scope `provided` al POM |
| Conflictos de clases al cargar | Las dependencias no son `provided` | Cambiar scope a `provided` para pf4j, sentinel-api y javafx |
| El plugin no se detecta al reiniciar | No está guardado en la BD y la subcarpeta no coincide | Instalar desde la tienda o verificar nombre de subcarpeta |

---

## Resumen rápido 

- Crear clase que extienda `org.pf4j.Plugin`
- Crear clase con `@Extension` que implemente `SentinelExtension`
- Implementar los 5 métodos de la interfaz
- Configurar `pom.xml` con dependencias `provided`
- Configurar `maven-jar-plugin` con los `manifestEntries` (al menos `Plugin-Id`, `Plugin-Version`, `Plugin-Class`)
- Ejecutar `mvn install -pl sentinel-api` en SentinelMon (una vez)
- Compilar con `mvn clean package`
- Instalar desde la tienda o colocar en `plugins/<plugin-id>/`
