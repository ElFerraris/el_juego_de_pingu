# Informe Técnico: El Juego de Pingu

## 1. Introducción y Objetivos

### 1.1 Descripción del Proyecto
"El Juego de Pingu" es una aplicación lúdica digital desarrollada íntegramente en Java, utilizando la biblioteca **JavaFX** para su interfaz gráfica. El proyecto consiste en un juego de mesa interactivo, inspirado en mecánicas clásicas como "Escaleras y Serpientes", pero ambientado en un entorno ártico dinámico. 

En este ecosistema, los jugadores asumen el papel de pingüinos que deben recorrer un tablero de más de 50 casillas, superando obstáculos naturales y gestionando recursos para alcanzar la meta antes que sus oponentes o la astuta CPU (representada por una Foca).

### 1.2 Contexto y Lore
La narrativa del juego sitúa al usuario en una carrera por la supervivencia y el dominio del hielo. El diseño visual y funcional busca sumergir al jugador en un ambiente donde cada casilla puede representar una ventaja competitiva o un peligro inminente, fomentando la toma de decisiones estratégicas mediante el uso de inventarios y dados especiales.

### 1.3 Objetivos del Proyecto
El desarrollo de este software persigue diversos objetivos, tanto pedagógicos como técnicos:

*   **Implementación de Patrones de Arquitectura:** Utilizar de manera estricta el patrón **Modelo-Vista-Controlador (MVC)** para garantizar un código modular, escalable y fácil de mantener.
*   **Desarrollo de Interfaces Dinámicas:** Crear una experiencia de usuario (UX) fluida y visualmente atractiva mediante JavaFX, CSS y gestión de eventos en tiempo real.
*   **Persistencia de Datos Robusta:** Diseñar e integrar una base de datos SQL para el registro de usuarios, estadísticas de partidas y la capacidad de reanudación de sesiones de juego.
*   **Lógica de Inteligencia Artificial:** Desarrollar un sistema de toma de decisiones para un jugador no humano (Foca), permitiendo una experiencia competitiva incluso en modo individual.
*   **Modelado UML:** Documentar la estructura interna del sistema mediante diagramas de clases y casos de uso para reflejar fielmente la arquitectura del software.

## 2. Análisis de Requisitos

Para garantizar el éxito del desarrollo, se han definido los requisitos mínimos que el sistema debe cumplir, dividiéndolos en funcionales y no funcionales.

### 2.1 Requisitos Funcionales (RF)
Los requisitos funcionales definen las acciones específicas que el usuario y el sistema pueden realizar:

*   **RF1 - Gestión de Usuarios:** El sistema debe permitir el registro e inicio de sesión de jugadores, validando sus credenciales contra la base de datos.
*   **RF2 - Mecánicas de Movimiento:** El sistema debe procesar el desplazamiento de los pingüinos por el tablero en función del resultado de los dados (estándar o especiales).
*   **RF3 - Gestión de Inventario:** Los jugadores deben poder recolectar y utilizar objetos (peces, bolas de nieve, dados) para influir en la partida.
*   **RF4 - Eventos de Casilla:** El motor del juego debe ejecutar acciones automáticas al caer en casillas especiales (retrocesos por agujeros, avances por trineos, ataques del oso).
*   **RF5 - Inteligencia Artificial (Foca):** El sistema debe gestionar el turno de un jugador automático con lógica de ataque y movimiento optimizado.
*   **RF6 - Persistencia de Partida:** El usuario debe ser capaz de guardar el estado actual de una partida (posiciones, inventarios, turnos) para retomarla posteriormente.
*   **RF7 - Control de Turnos:** El sistema debe alternar correctamente el control entre los jugadores humanos y la CPU, detectando automáticamente la condición de victoria.

### 2.2 Requisitos No Funcionales (RNF)
Estos requisitos especifican criterios de calidad y restricciones técnicas:

*   **RNF1 - Arquitectura MVC:** El código debe estar estructurado siguiendo la separación de responsabilidades para facilitar el mantenimiento.
*   **RNF2 - Interfaz Gráfica (GUI):** La aplicación debe ofrecer una experiencia visual fluida con una resolución fija (ej. 1800x900) y tiempos de respuesta mínimos.
*   **RNF3 - Seguridad de Datos:** La información sensible almacenada en la base de datos debe estar protegida (encriptación).
*   **RNF4 - Robustez:** El sistema debe gestionar excepciones (errores de red, fallos en la base de datos) sin interrumpir la experiencia del usuario de forma crítica.
*   **RNF5 - Compatibilidad:** La aplicación debe ser ejecutable en entornos con Java 21 y JavaFX 21 o superior.

## 3. Diseño de la Arquitectura

La arquitectura de "El Juego de Pingu" se basa en principios de diseño orientado a objetos y una clara separación de responsabilidades para garantizar la escalabilidad y mantenibilidad del software.

### 3.1 Modelo-Vista-Controlador (MVC)
Se ha implementado el patrón **MVC**, el cual divide la aplicación en tres capas interconectadas:

1.  **Modelo (Model):** Gestiona la lógica de negocio, el estado del juego (jugadores, tablero, inventario) y las reglas de las casillas. Es independiente de la interfaz de usuario.
2.  **Vista (View):** Define la apariencia visual mediante archivos **FXML** y hojas de estilo **CSS**. Se encarga únicamente de la presentación de los datos al usuario.
3.  **Controlador (Controller):** Actúa como intermediario. Captura las interacciones del usuario en la vista y actualiza el modelo, o viceversa, reflejando los cambios del modelo en la interfaz.

### 3.2 Estructura de Paquetes
El proyecto se organiza en los siguientes paquetes principales:

*   **`aplicacion`:** Contiene la clase principal (`Main`) que inicia la máquina virtual de JavaFX y carga la escena inicial.
*   **`modelo`:** Alberga las entidades fundamentales como `Jugador`, `Foca`, `Tablero`, `Casilla` e `Inventario`.
*   **`controlador`:** Contiene la lógica que gestiona el flujo de las pantallas y la interacción con el usuario (ej. `ControladorLogin`, `ControladorJuego`).
*   **`vista`:** Almacena los archivos `.fxml` y recursos `.css` que definen el diseño visual.
*   **`datos`:** Se encarga de la capa de persistencia, gestionando la conexión a la base de datos y las operaciones CRUD (Create, Read, Update, Delete).
*   **`util`:** Clases auxiliares para tareas transversales como la encriptación de datos, gestión de sonidos o utilidades de red.
*   **`assets`:** Recursos multimedia como imágenes de los pingüinos, la foca, iconos de inventario y efectos de sonido.

### 3.3 Diagrama de Clases UML (Resumen)
El sistema utiliza una estructura de herencia donde `Foca` y `JugadorHumano` (si aplica) heredan de una clase base `Jugador`. El `Tablero` se compone de una colección de `Casilla`, cada una con su propio comportamiento especializado. Esta estructura permite añadir nuevos tipos de obstáculos o personajes con un impacto mínimo en el código existente.

## 4. Implementación de la Lógica del Juego (Modelo)

El núcleo de la aplicación reside en el paquete `modelo`, donde se definen las reglas que gobiernan el comportamiento de los elementos del juego.

### 4.1 Representación del Tablero
El tablero no es solo una imagen, sino una estructura de datos lógica compuesta por una colección de objetos `Casilla`. Cada casilla posee un índice y, en muchos casos, un comportamiento asociado mediante el patrón **Strategy** o mediante polimorfismo, permitiendo que el motor de juego ejecute acciones específicas dependiendo de dónde aterrice el jugador.

### 4.2 Entidades Principales
*   **Clase `Jugador`:** Clase base que almacena la posición, el nombre, el color de la ficha y el estado del inventario.
*   **Clase `Foca` (IA):** Hereda de `Jugador` y sobreescribe métodos de decisión. Utiliza una lógica heurística simple para decidir cuándo usar dados especiales (ej. "Dado Rápido") para maximizar su ventaja o entorpecer a los pingüinos.
*   **Clase `Inventario`:** Gestiona el límite de objetos (máx. 3 dados, 2 peces, 6 bolas de nieve). Incluye métodos para validar la disponibilidad de recursos antes de permitir una acción.

### 4.3 Mecánicas y Eventos Especiales
La lógica de colisiones y eventos se activa tras cada movimiento:
*   **El Oso:** Implementa una lógica de "soborno". Si el jugador tiene un pez, puede evitar el ataque; de lo contrario, regresa a la casilla inicial.
*   **Agujero en el Hielo:** Aplica una penalización de retroceso inmediato a la casilla anterior.
*   **Trineo:** Funciona como un sistema de "atajo", teletransportando al jugador a una posición avanzada predefinida.
*   **Suelo Fragmentado:** Una mecánica compleja que penaliza al jugador en función de su carga (objetos en inventario), pudiendo provocar desde la pérdida de un turno hasta el reinicio de la posición.

### 4.4 Sistema de Dados
El juego implementa un generador de números aleatorios para los dados estándar (1-6). Además, existen **Dados Especiales** que permiten alterar las probabilidades o el rango de movimiento, añadiendo una capa estratégica a la gestión del inventario.

## 5. Interfaz de Usuario y Experiencia de Usuario (Vista y Controlador)

El diseño visual de "El Juego de Pingu" ha sido desarrollado buscando un equilibrio entre estética profesional y funcionalidad intuitiva, aprovechando las capacidades multimedia de **JavaFX**.

### 5.1 Tecnologías de Interfaz
*   **Archivos FXML:** Se han utilizado archivos XML declarativos para definir la estructura jerárquica de las interfaces. Esto permite separar el diseño visual de la lógica de control, facilitando el uso de herramientas como **Scene Builder**.
*   **Hojas de Estilo CSS:** El estilo visual (colores árticos, gradientes, fuentes personalizadas y efectos de cristal/glassmorphism) se gestiona de forma centralizada mediante archivos `.css`, permitiendo una identidad visual coherente en todas las escenas.

### 5.2 Gestión de Escenas y Navegación
La aplicación utiliza un sistema de navegación fluido para transicionar entre los diferentes estados del programa:
1.  **Pantalla de Login:** Acceso de usuarios con validación de credenciales.
2.  **Configuración de Partida:** Selección de jugadores humanos y ajuste de parámetros de la CPU.
3.  **Tablero de Juego:** Interfaz principal donde se desarrolla la partida, con paneles dedicados para el inventario, el registro de eventos y los controles de turno.

### 5.3 Animaciones y Feedback Visual
Para mejorar la experiencia de usuario (UX), se han implementado diversos elementos dinámicos:
*   **Simulación de Dados:** El lanzamiento de dados incluye una secuencia animada (GIFs o transiciones rápidas) para generar expectación antes de mostrar el resultado final.
*   **Movimiento de Fichas:** Los pingüinos y la foca no se teletransportan; se desplazan suavemente entre casillas mediante transiciones de coordenadas, facilitando al jugador el seguimiento visual de la partida.
*   **Micro-interacciones:** Los botones y elementos del inventario cuentan con efectos de iluminación y cambio de escala al pasar el cursor (*hover*), proporcionando feedback inmediato sobre la interactividad de cada componente.

## 6. Persistencia de Datos

Para garantizar que el progreso de los usuarios no se pierda, el sistema integra una capa de persistencia basada en una base de datos relacional accesible mediante **JDBC (Java Database Connectivity)**.

### 6.1 Modelo de Base de Datos
La estructura de datos se ha normalizado para evitar redundancias y garantizar la integridad de la información. Las tablas principales incluyen:
*   **Usuarios:** Almacena el perfil de los jugadores, incluyendo credenciales y estadísticas globales.
*   **Partidas:** Registra el estado general de una sesión activa (fecha, turno actual, estado del tablero).
*   **Inventarios:** Detalla los objetos que posee cada jugador en una partida específica.

### 6.2 Gestión de la Conexión
La comunicación con la base de datos se realiza de forma segura a través de una clase de utilidad en el paquete `datos`. Se utilizan **PreparedStatement** para todas las consultas SQL, protegiendo así la aplicación contra ataques de inyección SQL y mejorando el rendimiento de las consultas recurrentes.

### 6.3 Seguridad y Encriptación
Siguiendo los requisitos de seguridad planteados, el sistema no almacena información sensible (como contraseñas) en texto plano. Se ha implementado un sistema de encriptación (ej. SHA-256 o AES) en la capa de datos/utilidades para asegurar que la información de los usuarios permanezca privada incluso en caso de acceso no autorizado a la base de datos física.

### 6.4 Guardado y Carga Dinámica
El juego permite realizar guardados en caliente. Al cerrar la aplicación o pulsar el botón de guardado, el sistema serializa el estado de los objetos del `modelo` (posiciones, ítems de inventario) y los vuelca en la base de datos mediante transiciones SQL atómicas. Al retomar la partida, se realiza el proceso inverso: el sistema reconstruye las instancias de `Jugador` y `Tablero` basándose en los últimos registros almacenados.

## 7. Inteligencia Artificial (La Foca)

El juego incluye un oponente automático controlado por el sistema, personificado por una foca gris. A diferencia de los jugadores humanos, la foca opera bajo un motor de toma de decisiones automatizado que busca equilibrar la dificultad y la competitividad.

### 7.1 Lógica de Decisión (Heurística)
La IA no se limita a realizar lanzamientos de dados aleatorios. Se ha implementado una lógica de decisión que evalúa el estado de su inventario antes de cada acción:
*   **Priorización de Objetos:** Si la foca posee un "Dado Rápido", el algoritmo priorizará su uso para maximizar el avance y alejarse de los jugadores o alcanzar la meta.
*   **Gestión de Recursos:** La foca es capaz de interactuar con los mismos objetos de inventario que los jugadores, lo que requiere que el sistema valide constantemente sus recursos disponibles antes de ejecutar una acción automatizada.

### 7.2 Comportamiento de Ataque y Colisiones
La foca tiene un rol antagonista dentro del tablero. Se han programado eventos específicos que se disparan cuando la foca colisiona con un pingüino o lo adelanta. Estos comportamientos incluyen:
*   **Robo de Inventario:** Al interactuar con un jugador humano, la foca puede sustraer recursos, dificultando el avance del oponente.
*   **Interacción con Obstáculos:** La IA está sujeta a las mismas reglas del tablero (osos, agujeros, trineos), lo que garantiza una partida justa y equilibrada donde los jugadores pueden predecir y reaccionar a los movimientos de la CPU.

### 7.3 Autonomía en el Flujo de Turnos
Técnicamente, el turno de la foca se ejecuta de manera asíncrona o mediante un retardo controlado en el controlador de juego. Esto evita que los movimientos de la CPU sean instantáneos, permitiendo que el jugador humano procese visualmente lo que está ocurriendo y manteniendo el ritmo de la partida.

## 8. Pruebas y Validación

Para asegurar la fiabilidad de "El Juego de Pingu", se ha llevado a cabo un proceso de validación exhaustivo centrado en la integridad de las mecánicas y la estabilidad de la interfaz.

### 8.1 Pruebas Unitarias y de Lógica
Se han realizado pruebas modulares sobre los componentes del `modelo` para verificar:
*   **Cálculo de Posiciones:** Validación de que los jugadores no sobrepasan los límites del tablero y que el retroceso por obstáculos (agujeros u osos) se calcula correctamente.
*   **Gestión de Inventario:** Comprobación de que no es posible añadir más objetos de los permitidos por el límite máximo y que el consumo de recursos (peces o dados) se refleja instantáneamente en el estado del jugador.
*   **Condiciones de Victoria:** Verificación de que el juego finaliza exactamente cuando un jugador alcanza la casilla meta, bloqueando acciones posteriores.

### 8.2 Pruebas de Integración y Persistencia
Se han validado los flujos de datos completos entre la aplicación y la base de datos:
*   **Ciclo de Guardado/Carga:** Se han simulado cierres inesperados de la aplicación para confirmar que el estado se recupera fielmente desde los registros SQL.
*   **Concurrencia de Usuarios:** Pruebas de inicio de sesión simultáneo para asegurar que el sistema de autenticación gestiona correctamente las sesiones individuales.

### 8.3 Gestión de Errores y Excepciones
La aplicación implementa un sistema robusto de captura de excepciones para evitar cierres forzados (*crashes*):
*   **Recursos no encontrados:** Gestión de errores en la carga de imágenes, fuentes o archivos FXML mediante mensajes en consola o alertas visuales.
*   **Fallos de Conexión:** Si la base de datos no está disponible, el sistema informa al usuario en la pantalla de login en lugar de quedar bloqueado.

## 9. Conclusiones y Mejoras Futuras

El desarrollo de "El Juego de Pingu" ha permitido consolidar conocimientos avanzados en programación orientada a objetos, gestión de interfaces gráficas y persistencia de datos.

### 9.1 Retos Técnicos Superados
Durante la ejecución del proyecto, se han enfrentado y resuelto diversos desafíos técnicos:
*   **Sincronización de la Lógica de Turnos:** Coordinar el movimiento visual con el estado lógico de los jugadores, especialmente durante la transición de turnos de la CPU, requirió un uso preciso de clases de animación de JavaFX.
*   **Integración del Patrón MVC:** Mantener una separación estricta entre la lógica de las casillas y su representación visual fue fundamental para evitar código acoplado.
*   **Seguridad de Datos:** La implementación de capas de validación en la persistencia garantiza una aplicación más profesional y segura para el usuario final.

### 9.2 Mejoras Futuras y Escalabilidad
El proyecto ha sido diseñado con una arquitectura modular que permite:
*   **Modo Multijugador Online:** Implementación de Sockets para permitir partidas en red.
*   **Expansión de Contenido:** Inclusión de nuevos tipos de casillas (ej. "Ventisca") y personajes con habilidades únicas.
*   **Sistema de Clasificación (Ranking):** Creación de una tabla de líderes global basada en las estadísticas almacenadas en la base de datos.

## 10. Anexos

### 10.1 Guía de Instalación y Ejecución
1.  **Requisitos:** Tener instalado el JDK 21 y el entorno de ejecución de JavaFX.
2.  **Configuración de BD:** Asegurarse de que el servidor de base de datos esté activo y las tablas creadas según el modelo.
3.  **Ejecución:** Ejecutar la clase `Main` desde el IDE o mediante el script `ejecutar.bat` proporcionado en la raíz del proyecto.

### 10.2 Referencias y Herramientas
*   **JavaFX:** Framework principal para la interfaz de usuario.
*   **Scene Builder:** Herramienta de diseño visual para FXML.
*   **JDBC:** Conectividad estándar para la base de datos SQL.

---
**Desarrollado por:** BadLabs©️  
**Versión:** 1.0  
**Fecha:** 5 de Mayo de 2026








