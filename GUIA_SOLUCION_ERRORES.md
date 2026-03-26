# Guía de Solución de Errores: JavaFX 21 + Oracle JDBC

Este documento resume los problemas resueltos durante la sesión de desarrollo y cómo solucionarlos en el futuro.

## 1. Imagen de Fondo en FXML
Para añadir una imagen de fondo de forma limpia:
- No la añadas directamente en el FXML. Usa un archivo **CSS**.
- En el archivo CSS:
  ```css
  .root {
      -fx-background-image: url("../assets/login/fondo_login.png");
      -fx-background-size: cover;
  }
  ```
- En el FXML, añade la propiedad `stylesheets="@tu_archivo.css"` al contenedor raíz.

## 2. Java Module System (JPMS)
Si usas Java 9 o superior (como Java 21), es mejor usar módulos:
- **Regla de oro**: Todas tus clases deben estar en **paquetes con nombre** (no en el "default package").
- Crea un archivo `module-info.java` en la carpeta `src`.
- Asegúrate de incluir `requires javafx.controls;`, `requires javafx.fxml;`, etc.
- Usa `opens [paquete] to javafx.fxml;` para que JavaFX pueda cargar tus controladores.

## 3. Error NoClassDefFoundError: Stage
Si te falta la clase `Stage` al arrancar:
- Es porque las librerías de JavaFX están en el **Classpath** en lugar del **Modulepath**.
- En Eclipse: **Properties -> Java Build Path -> Libraries**. Mueve todo de Classpath a Modulepath.

## 4. Error Oracle JDBC (ojdbc8)
Si el `module-info.java` no reconoce el driver de Oracle:
- El nombre del módulo suele derivarse del nombre del JAR.
- Prueba con: `requires com.oracle.database.jdbc;` o `requires ojdbc8;`.

## 5. Error QuantumRenderer: No suitable pipeline found
Este es el error más común en Windows. Se soluciona así:
- **Archivos faltantes**: Copia todos los archivos **.dll** de la carpeta `bin` de tu JavaFX SDK a la carpeta `lib` de tu proyecto.
- **Ruta Nativa**: En Eclipse, ve a las propiedades del proyecto -> **Java Build Path** -> **Libraries**. Despliega los JAR de JavaFX y en **Native library location**, elige tu carpeta `lib`.
- **Launcher**: Crea una clase `Launcher.java` que solo llame al `main` de tu clase principal sin extender `Application`. Esto soluciona muchos problemas de inicialización.

## 6. Git y GitHub (.gitignore)
- Ignora siempre la carpeta `bin/` (son archivos compilados).
- **No ignores** la carpeta `lib/` si contiene tus JARs y DLLs necesarios para ejecutar el proyecto.
- Si usas `*.jar` en el ignore, añade una excepción: `!lib/*.jar` y `!lib/*.dll`.
