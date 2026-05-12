# 🐧 El Juego del Pingüino - v1.0.0 "Icebreaker" ❄️

¡Bienvenido a la versión oficial de lanzamiento de **El Juego del Pingüino**! Tras intensas jornadas de desarrollo, nos enorgullece presentar la versión 1.0.0, cargada de novedades y optimizaciones críticas para la mejor experiencia de juego.

---

## 🚀 Novedades Principales

### 🏗️ Arquitectura de Assets Optimizada
Hemos migrado todo el sistema de recursos fuera del código fuente. Esto permite:
- **Tiempos de carga reducidos**: Acceso directo al sistema de archivos.
- **Personalización sencilla**: Ahora puedes ver (y si te atreves, modificar) las imágenes y sonidos directamente en la carpeta `/assets`.
- **Ejecutables más ligeros**: El núcleo del juego es más compacto y eficiente.

### 🎮 Gameplay y Experiencia
- **Tablero Isométrico Dinámico**: Renderizado con efectos de caída y rebote.
- **Música Dinámica**: La banda sonora evoluciona e intensifica su ritmo a medida que avanzas en la partida.
- **IA "Foca Loca"**: Un oponente virtual que no te lo pondrá nada fácil.
- **Sistema de Persistencia**: Guarda tus partidas en la base de datos y retómalas cuando quieras.

---

## 🛠️ Instrucciones de Instalación y Ejecución

Para jugar a esta versión, sigue estos pasos:

1. **Descarga el archivo comprimido** de la release.
2. **Extrae todo el contenido** en una sola carpeta. Es **CRÍTICO** que la carpeta `assets/` y la carpeta `lib/` se mantengan en el mismo directorio que el ejecutable.
3. Asegúrate de tener instalado **Java 17 o superior**.
4. Haz doble clic en `ejecutar.bat` para iniciar la aventura.

> [!IMPORTANT]
> Si mueves el archivo `.jar` o el ejecutable fuera de la carpeta raíz, el juego no podrá encontrar las imágenes ni los sonidos. ¡Mantén a los pingüinos cerca de sus recursos!

---

## 👥 Créditos - BadLabs Team
Este proyecto ha sido posible gracias al esfuerzo de:
- **Desarrollo Core**: Andrei Style & BadLabs Crew.
- **Diseño Artístico**: Assets isométricos y visuales premium.
- **Ingeniería de Audio**: Sistema de capas dinámicas.

---

## 🐞 Correcciones de última hora
- Solucionado el error de inicialización del `boot layer` relacionado con el descriptor de módulos.
- Corregida la carga de fuentes en sistemas Windows.
- Optimizado el escalado de la intro de video para pantallas de alta resolución.

---
*BadLabs© 2026 - Programación Avanzada*
