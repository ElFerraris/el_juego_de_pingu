# Registro de Pulido y Mejoras 🐧✨

Este archivo contiene el historial de pequeños ajustes visuales y funcionales solicitados para perfeccionar el juego.

- [x] **1. Rediseño del Icono Hamburguesa**
    - Líneas más gruesas (el doble que el borde).
    - Longitud reducida para no tocar los bordes del botón.
    - Puntas redondeadas según el boceto. 
- [x] **2. Eliminación de Guardado Redundante en Pausa**
    - Se ha quitado el forzado de guardado al salir del juego desde el menú de pausa, ya que el sistema ya guarda automáticamente tras cada movimiento del jugador.
- [x] **3. Animaciones en Menús Overlay**
    - Añadidas transiciones de fundido (Fade) y deslizamiento (Slide Up/Down) al abrir y cerrar el menú de pausa, opciones y confirmación.
    - El fondo oscuro (overlayPane) también aparece y desaparece con un fundido suave.
- [x] **4. Sonidos en Menús In-Game**
    - Añadidos sonidos de confirmación a los botones de navegar y aplicar cambios.
    - Añadidos sonidos de retroceso a los botones de "Volver" y "No" en el overlay.
- [x] **5. Validación en Cargar Partida**
    - El botón "Jugar Partida" ahora se deshabilita automáticamente si no hay ninguna partida seleccionada en la lista, eliminando el popup de advertencia anterior.
- [x] **6. GIF de Carga Global (Tongo Dancing)**
    - Implementación de `util.LoadingOverlay` para mostrar el GIF de carga en la esquina inferior derecha.
    - Integración en `LoadGameController` (al cargar lista y cargar partida), `PlayerConfigController` (al cargar nombres) y `SeedSelectionController` (al generar la partida).
    - El GIF utiliza hilos secundarios para no bloquear la interfaz y tiene animaciones de fundido suave.
- [x] **7. Ajuste de Resolución en Login**
    - Fijada la resolución del Login a **1200x700** tanto en `Main.java` como en `Login.fxml`.
    - Asegurada la transición a la resolución guardada en `settings.properties` al entrar en la Intro.
- [x] **8. Optimización de la Intro (Vídeo)**
    - Añadido sistema de **Failsafe**: si el vídeo no carga en 5 segundos, salta automáticamente al menú.
    - Añadido **manejo de errores** para detectar problemas de codecs y evitar pantallas en negro.
    - Mejorada la gestión de memoria eliminando el reproductor correctamente al finalizar.
