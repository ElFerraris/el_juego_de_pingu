# El Juego de Pingu

## Descripción del Proyecto
Este proyecto pertenece al ciclo de Desarrollo de Aplicaciones Web 1.º de ILERNA. "El Juego de Pingu" es un juego de mesa digital desarrollado en Java con JavaFX, inspirado en el clásico juego de Escaleras y Serpientes.

El objetivo principal es aplicar los conocimientos adquiridos en desarrollo Java, bases de datos, análisis y diseño de interfaces, y diagramas UML para construir un juego completamente funcional y dinámico.

## Tecnologías y Herramientas
* **Lenguaje:** Java (con JavaFX para la interfaz gráfica).
* **Bases de Datos:** Gestión y persistencia de datos (inventarios, turnos y estado de la partida) de forma encriptada.
* **Control de Versiones:** GitHub.
* **Documentación:** JavaDoc y UML (Diagramas de Clases y Casos de Uso).

## Mecánicas del Juego
Los jugadores controlan pingüinos que deben avanzar por un tablero de al menos 50 casillas. Durante la partida, los jugadores se enfrentarán a obstáculos, gestionarán su inventario y podrán competir contra otros jugadores o contra una CPU.

### Casillas Especiales
* **Pingüino:** Ficha del jugador con su identificador.
* **Oso:** Si el jugador es atacado, regresa al inicio del juego.
* **Agujero en el hielo:** Envía al jugador a la casilla anterior.
* **Trineo:** Permite avanzar hasta el siguiente trineo del tablero.
* **Casilla de interrogante:** Activa un evento aleatorio.
* **Suelo fragmentado:** Penaliza al jugador dependiendo de la cantidad de objetos en su inventario (desde perder un turno hasta volver al inicio).

### Inventario
Cada jugador puede almacenar:
* Máximo 3 dados.
* Máximo 2 peces (útiles para sobornar al oso y evitar penalizaciones).
* Máximo 6 bolas de nieve (para hacer retroceder a los rivales o ganar batallas en la misma casilla).

### Modos de Juego y Funcionalidades
* **Multijugador:** Partidas de 2 a 4 jugadores.
* **IA (Foca):** La CPU está controlada por una inteligencia artificial representada por una foca, cuyo objetivo es ganar y entorpecer a los jugadores robándoles inventario o golpeándolos.
* **Guardado de Partida:** El juego registra la posición, el estado del tablero y el inventario en la base de datos para poder retomar la partida.

## Estructura de Tareas
1. **Plan de Digitalización:** Desarrollo de un plan basado en el *lore* del juego.
2. **Análisis de Requisitos:** Documento formal con requisitos funcionales, no funcionales y tipos de usuarios.
3. **Diagramas UML:** Creación de diagramas de Clases y de Casos de Uso.
4. **Desarrollo del Juego:** Implementación en Java de todas las mecánicas, gestión de turnos y eventos aleatorios.
5. **Videotutorial en Inglés:** Breve explicación en video de los objetivos, mecánicas y reglas del juego.
