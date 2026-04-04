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
    - El botón "Jugar Partida" ahora se deshabilita automáticamente si no hay ninguna partida seleccionada, eliminando el popup de advertencia anterior.
