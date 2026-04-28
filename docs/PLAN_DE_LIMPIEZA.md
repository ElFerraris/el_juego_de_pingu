# 🧹 Plan de Limpieza y Optimización del Proyecto

Este archivo detalla las tareas pendientes para profesionalizar la estructura del código y eliminar la deuda técnica acumulada durante la migración de consola a JavaFX.

---

## 📋 Tareas de Limpieza

### 1. Depuración de `Juego.java` (Prioridad: ALTA)
*   [x] Eliminar métodos que usan `Scanner` (ej. `configurarPartida`, `ejecutarTurnoHumano`).
*   [x] Borrar bloques masivos de código comentado.
*   [x] Resolver o eliminar `// TODO` obsoletos.
*   [x] Convertir la clase en un motor puro de lógica y DTO para la base de datos, sin interacción directa con el usuario.

### 2. Refactorización de `TableroController.java` (Prioridad: MEDIA-BAJA)
*   [x] Extraer la lógica de gestión de cámara a una clase `CameraController.java`.
*   [x] Mover la lógica de colisiones y guerra a un gestor de flujo de juego separado (`GameFlowManager.java`).
*   [x] Reducir el tamaño del archivo (de >2000 a ~1500 líneas) para mejorar la mantenibilidad.

### 3. Consolidación de Documentación (Prioridad: MEDIA)
*   [x] Mover todos los archivos de seguimiento (`progreso_documentacion.md`, `pulido.md`, `normas_proyecto.md`) a una carpeta `/docs`.
*   [x] Unificar criterios en `INSTRUCCIONES.md` y eliminar duplicados.

### 4. Estandarización de Logs (Prioridad: BAJA)
*   [ ] Reemplazar `System.out.println` de depuración por el sistema de log visual del juego o un Logger profesional.
*   [x] Eliminar mensajes de consola con símbolos especiales (`►`, `❌`) que ya no son necesarios en JavaFX.

### 5. Revisión de `BBDD.java` (Prioridad: MEDIA)
*   [x] Eliminar comentarios de "Funciones por implementar" que ya están resueltas.
*   [x] Revisar si hay métodos redundantes o que se pueden simplificar.

---

## ✅ Progreso
*   `[x]` Tarea 1: Limpieza de Juego.java
*   `[x]` Tarea 2: Refactorización de Controladores
*   `[x]` Tarea 3: Organización de Documentación
*   `[x]` Tarea 4: Limpieza de Prints
*   `[x]` Tarea 5: Pulido de BBDD.java
