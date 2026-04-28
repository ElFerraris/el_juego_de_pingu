# 🐧 REGLAS Y LÓGICA DEL PROYECTO (GUÍA PARA IA)

Este archivo es de **lectura obligatoria** para cualquier IA que trabaje en este proyecto. Contiene las restricciones técnicas, la lógica del juego y el contexto del entorno de desarrollo.

---

## 🚫 COSAS PROHIBIDAS (NORMAS ESTRICTAS)

1.  **Cero falta de Javadoc**: Todos los métodos (públicos/protegidos) y clases **DEBEN** tener Javadoc completo con `@param`, `@return` y descripciones profesionales. La cobertura debe ser siempre del 100%.
2.  **Sin espacios en Assets**: Prohibido usar espacios en nombres de archivos o carpetas de recursos. Usa siempre guion bajo (`_`).
3.  **No saltarse el NavigationController**: Toda navegación entre pantallas debe pasar por `NavigationController`. No crees Stages o Scenes manualmente de forma aislada.
4.  **No saltarse el GameContext**: El estado global (usuario actual, semilla, jugadores) vive en `GameContext` (Singleton). No pases estos datos por constructores de controladores de forma redundante.
5.  **Prohibido ignorar Oracle**: La persistencia es estrictamente con **Oracle SQL**. Las llamadas deben seguir el formato de procedimientos almacenados: `{call nombre_proc(?,?)}` o funciones `{? = call nombre_func(?,?)}`.
6.  **No modificar FXML a ciegas**: Si cambias un ID en el FXML, actualiza inmediatamente el `@FXML` correspondiente en el controlador.

---

## 🏗️ ENTORNO DE DESARROLLO

*   **Editor**: Antigravity (IA Agentic).
*   **Compilación/IDE**: **Eclipse**.
    *   *Nota para la IA*: Asegúrate de que las rutas de los recursos sean compatibles con el classpath de Eclipse (`/assets/...`, `/vista/...`).
*   **Java**: Versión 21.
*   **Framework**: JavaFX (Modular).

---

## 🕹️ LÓGICA CENTRAL DEL JUEGO

### 1. El Tablero
*   Consta de **50 casillas** (0 a 49).
*   Se genera mediante una **Seed** (semilla). La semilla determina la posición de los peligros.
*   **Tipos de Casilla**:
    *   `NORMAL`: Sin efectos.
    *   `AGUJERO`: Te manda al agujero anterior (o al inicio).
    *   `TRINEO`: Te impulsa al siguiente trineo.
    *   `OSO`: Requiere un `Pez` para sobornarlo o vuelves al inicio.
    *   `INTERROGANTE`: Caja de suministros (da objetos aleatorios).
    *   `ROMPEDIZA`: Se rompe si llevas mucho peso en el inventario.

### 2. Jugadores y Turnos
*   **Pinguino**: Jugador humano.
*   **Foca (CPU)**: Controlada por la clase `Cpu.java`. Tiene lógica de priorización de objetos.
*   **Ciclo de Turno**:
    1.  Tirar dado (Normal 1-6, Rápido 3-8, Lento 1-3).
    2.  Movimiento animado paso a paso.
    3.  Comprobar **Guerra**: Si dos jugadores caen en la misma casilla, se restan sus bolas de nieve. El que pierde retrocede la diferencia.
    4.  Aplicar efecto de la casilla de destino.

### 3. Inventario
*   `BolaNieve`: Munición para la Guerra.
*   `Pez`: Para sobornar osos.
*   `Dados Especiales`: Para manipular el movimiento.

---

## 💾 PERSISTENCIA (BBDD)
*   Toda partida se registra al inicio.
*   Se guardan las "Participaciones" (estado individual de cada pinguino en esa partida).
*   Usa `BBDD.java` como única interfaz de persistencia.

---
*Este documento debe ser consultado antes de realizar cualquier cambio estructural o de lógica.*
