# Restricciones del Proyecto (Normas Académicas)

Este documento detalla las prácticas de programación que **NO** están permitidas en este proyecto según las normas de los evaluadores. Se debe consultar este archivo antes de realizar cualquier cambio o añadir nuevas funcionalidades.

## 🚫 Prácticas Prohibidas

### 1. Multihilo (Concurrency)
*   **No usar** `Thread`, `Task`, `Runnable` o `ExecutorService`.
*   **No usar** `Platform.runLater()` (salvo excepciones críticas de JavaFX si no hay alternativa síncrona).
*   **No usar** la palabra clave `synchronized`.
*   **Razón**: El proyecto debe seguir un flujo de ejecución lineal y síncrono.

### 2. Saltos de Control y Tipos Específicos
*   **No usar** `continue`.
*   **No usar** `break` (excepto dentro de bloques `switch`).
*   **No usar** `return` dentro de métodos de tipo `void`.
*   **No usar** `enum` (tipos enumerados).
*   **Razón**: Se consideran construcciones que ocultan la lógica subyacente o rompen el flujo estructurado según los criterios académicos aplicados.

### 3. Alternativas Permitidas
*   En lugar de `continue`, envolver el cuerpo del bucle en un bloque `if`.
*   En lugar de `return` en métodos `void`, usar una estructura `if-else` para controlar el flujo o banderas booleanas.
*   En lugar de hilos para pausas, usar `PauseTransition` de JavaFX (que es una API de alto nivel).

---
*Última actualización: 2026-04-25*
