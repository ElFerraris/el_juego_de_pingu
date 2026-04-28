package controlador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import modelo.*;
import datos.BBDD;

/**
 * Motor principal de la lógica del juego "El Juego de Pingu".
 * 
 * <p>
 * Esta clase actúa como controlador central del estado de la partida. Gestiona
 * la
 * inicialización del tablero, la creación y registro de jugadores, el ciclo de
 * turnos, las reglas de colisión (Guerra), y la persistencia de datos (guardado
 * y carga) mediante la base de datos.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Juego {

    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnoActual;
    private boolean partidaFinalizada;
    private Jugador ganador;
    private String nombrePartida;
    private BBDD baseDatos; // Composición en vez de herencia

    /**
     * Constructor por defecto de la clase Juego.
     * Inicializa un tablero nuevo, la lista de jugadores vacía y el gestor de BBDD.
     */
    public Juego() {
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<>();
        this.partidaFinalizada = false;
        this.baseDatos = new BBDD();
    }

    // ==================== CONFIGURACIÓN ====================

    /**
     * Configura la partida inicial solicitando datos por consola.
     * 
     * <p>
     * Permite elegir el tipo de tablero (semilla), el número de jugadores,
     * sus nombres y colores, asegurando que no haya duplicados.
     * </p>
     * 
     * @param sc Objeto Scanner para la entrada de datos por consola.
     */
    public void configurarPartida(Scanner sc) {
        System.out.println("=== BIENVENIDO A EL JUEGO DE PINGU ===");

        // Configurar seed del tablero
        int tipoDeTablero = 0;
        while (tipoDeTablero != 1 && tipoDeTablero != 2) {
            System.out.println("1 Para generar seed aleatoria");
            System.out.println("2 Para introducir una seed válida");
            try {
                tipoDeTablero = Integer.parseInt(sc.nextLine());

                if (tipoDeTablero == 1) {
                    tablero.generarSeedAleatoria();
                } else if (tipoDeTablero == 2) {
                    System.out.print("Introduce la seed: ");
                    String seed = sc.nextLine();
                    tablero.introducirSeed(seed);
                } else {
                    System.out.println("Error: Por favor, elige 1 o 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un número (1 o 2).");
            }
        }

        ArrayList<String> coloresDisponibles = new ArrayList<>(Arrays.asList(
                "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"));

        // NUEVO: Lista para rastrear nombres ya elegidos
        ArrayList<String> nombresUsados = new ArrayList<>();

        // Elegir cantidad de jugadores
        int numJugadores = 0;
        while (numJugadores < 2 || numJugadores > 4) {
            System.out.print("¿Cuántos pingüinos van a jugar? (2-4): ");
            try {
                numJugadores = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduce un número válido.");
            }
        }

        // Crear jugadores
        for (int i = 1; i <= numJugadores; i++) {
            String nombre = "";
            boolean nombreValido = false;

            // --- BUCLE PARA EL NOMBRE ---
            while (!nombreValido) {
                System.out.print("Introduce el nombre del Jugador " + i + ": ");
                nombre = sc.nextLine().trim();

                if (nombre.isEmpty()) {
                    System.out.println("❌ El nombre no puede estar vacío.");
                } else if (nombresUsados.contains(nombre)) {
                    System.out.println("❌ Error: El nombre '" + nombre + "' ya está en uso. Elige otro.");
                } else {
                    nombresUsados.add(nombre); // Lo registramos
                    nombreValido = true;
                }
            }

            // --- BUCLE PARA EL COLOR ---
            String colorElegido = "";
            boolean colorValido = false;
            while (!colorValido) {
                System.out.println("Colores disponibles: " + coloresDisponibles);
                System.out.print("Introduce el color para " + nombre + ": ");
                colorElegido = sc.nextLine();

                String colorEncontrado = null;
                int index = 0;

                // Recorremos mientras no hayamos encontrado nada y queden elementos
                while (colorEncontrado == null && index < coloresDisponibles.size()) {
                    String c = coloresDisponibles.get(index);
                    if (c.equalsIgnoreCase(colorElegido)) {
                        colorEncontrado = c;
                    }
                    index++;
                }

                if (colorEncontrado != null) {
                    colorElegido = colorEncontrado;
                    coloresDisponibles.remove(colorEncontrado);
                    colorValido = true;
                } else {
                    System.out.println("❌ Color no válido o ya elegido.");
                }
            }

            agregarJugador(new Pinguino(1 + i, nombre, colorElegido));
        }

        /*
         * // Elegir cantidad de jugadores
         * int numJugadores = 0;
         * while (numJugadores < 2 || numJugadores > 4) {
         * System.out.print("¿Cuántos pingüinos van a jugar? (2-4): ");
         * try {
         * numJugadores = Integer.parseInt(sc.nextLine());
         * if (numJugadores < 2 || numJugadores > 4) {
         * System.out.
         * println("Error: El juego es para un mínimo de 2 y un máximo de 4 jugadores."
         * );
         * }
         * } catch (NumberFormatException e) {
         * System.out.println("Por favor, introduce un número válido.");
         * }
         * }
         * 
         * // 1. Definimos la lista de colores disponibles
         * ArrayList<String> coloresDisponibles = new ArrayList<>(Arrays.asList(
         * "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"
         * ));
         * 
         * 
         * 
         * 
         * // Crear jugadores
         * for (int i = 1; i <= numJugadores; i++) {
         * System.out.print("Introduce el nombre del Jugador " + i + ": ");
         * String nombre = sc.nextLine();
         * 
         * String colorElegido = "";
         * boolean colorValido = false;
         * 
         * // 2. Bucle de validación de color
         * while (!colorValido) {
         * System.out.println("Colores disponibles: " + coloresDisponibles);
         * System.out.print("Introduce el color para " + nombre + ": ");
         * colorElegido = sc.nextLine();
         * 
         * // Buscamos si el color existe en la lista (ignorando mayúsculas)
         * String colorEncontrado = null;
         * for (int j = 0; j < coloresDisponibles.size() && colorEncontrado == null;
         * j++) {
         * String c = coloresDisponibles.get(j);
         * if (c.equalsIgnoreCase(colorElegido)) {
         * colorEncontrado = c;
         * }
         * }
         * 
         * if (colorEncontrado != null) {
         * // Si existe, lo asignamos y lo BORRAMOS de la lista para el siguiente
         * colorElegido = colorEncontrado;
         * coloresDisponibles.remove(colorEncontrado);
         * colorValido = true;
         * } else {
         * System.out.println("❌ Error: El color '" + colorElegido +
         * "' no está disponible o ya fue elegido.");
         * }
         * }
         * 
         * // Agregamos al jugador con su color único asegurado
         * agregarJugador(new Pinguino(1 + i, nombre, colorElegido));
         * }
         */

        // Añadir la Foca CPU
        Foca foca = new Foca(0, "Foca Loca");
        agregarJugador(foca);
    }

    /**
     * Inicia formalmente la partida en el sistema.
     * 
     * <p>
     * Registra a todos los jugadores en la base de datos (si no existen),
     * crea una nueva entrada de partida en la BBDD y registra las participaciones.
     * </p>
     */
    public void iniciarPartida() {
        System.out.println("--- Iniciando El Juego de Pingu ---");
        this.turnoActual = 0;

        // 1. Primero registramos/comprobamos a los jugadores uno por uno
        for (Jugador j : jugadores) {
            // Obtenemos el ID de Oracle
            int idAsignado = this.baseDatos.registrarJugadorSiNoExiste(j);

            if (idAsignado != -1) {
                // Suponiendo que añades el atributo idBD a tu clase Jugador
                j.setId(idAsignado);
            }
        }

        // Simplemente llamamos al método de nuestro objeto baseDatos.
        // Él se encarga de conectar, insertar y desconectar.
        boolean exito = false;
        if (this.baseDatos.guardarNuevaPartida(this) == 0) {
            exito = false;
        } else {
            exito = true;
            this.getTablero().setIdPartida(this.baseDatos.guardarNuevaPartida(this));
        }
        for (Jugador j : jugadores) {
            this.baseDatos.insertarParticipacion(this.getTablero().getIdPartida(), j.getId(), j.getColor());
        }

        if (exito) {
            System.out.println("► Registro en Oracle completado con éxito.");
        } else {
            System.out.println("⚠️ No se pudo registrar la partida en la base de datos.");
        }

    }

    // ==================== TURNOS ====================

    /**
     * Gestiona la ejecución completa del turno para el jugador activo.
     * 
     * <p>
     * Incluye el lanzamiento del dado, el movimiento, la comprobación de guerras
     * y la aplicación de los efectos de la casilla de destino.
     * </p>
     * 
     * @param sc Scanner para interactuar con el jugador humano.
     */
    public void ejecutarTurno(Scanner sc) {
        Jugador jugadorActual = jugadores.get(turnoActual);

        System.out.println("------------------------------------");
        System.out.println("TURNO DE: " + jugadorActual.getNombre());

        if (!jugadorActual.estaBloqueado()) {
            int posAntes = jugadorActual.getPosicion();
            System.out.println("Posición actual: " + jugadorActual.getPosicion());

            if (jugadorActual instanceof Foca) {
                ejecutarTurnoCPU((Foca) jugadorActual, posAntes);
            } else {
                ejecutarTurnoHumano(jugadorActual, sc);
            }

            System.out.println("Nueva posición: " + jugadorActual.getPosicion());
            comprobarGuerra(jugadorActual);
            tablero.aplicarEfectoCasilla(jugadorActual);

        } else {
            System.out.println("Está BLOQUEADO, no se puede mover.");
            jugadorActual.setTurnosBloqueados(jugadorActual.getTurnosBloqueados() - 1);
            System.out.println("Turnos restantes de bloqueo: " + jugadorActual.getTurnosBloqueados());
        }
    }

    /**
     * Lógica interna del turno controlado por la Inteligencia Artificial (Foca).
     * 
     * @param foca     La instancia de la Foca que está jugando.
     * @param posAntes Posición del jugador antes de iniciar el movimiento.
     */
    private void ejecutarTurnoCPU(Foca foca, int posAntes) {
        System.out.println("La foca está pensando su movimiento...");
        foca.decidirAccion();

        int posDespues = foca.getPosicion();

        // Comprobar si la foca ha adelantado a algún jugador humano
        for (Jugador p : jugadores) {
            if (!(p instanceof Foca)) {
                if (p.getPosicion() > posAntes && p.getPosicion() < posDespues) {
                    foca.atacarJugador(p);
                }
            }
        }
    }

    /**
     * Lógica interna del turno para un jugador humano.
     * Gestiona la interacción con los dados especiales si están disponibles.
     * 
     * @param jugador El jugador humano que realiza el turno.
     * @param sc      Scanner para recibir la entrada del usuario.
     */
    private void ejecutarTurnoHumano(Jugador jugador, Scanner sc) {
        System.out.println("Presiona ENTER para tirar el dado...");
        String variable = "";
        variable = sc.nextLine();
        if (variable.equals("Guardar")) {
            guardarPartida();
        }
        if (jugador.getInventario().tieneObjeto("Dados")) {
            int nRapidos = 0;
            int nLentos = 0;

            for (Objeto d : jugador.getInventario().getListaDados()) {
                if (d instanceof DadoRapido)
                    nRapidos++;
                if (d instanceof DadoLento)
                    nLentos++;
            }

            System.out.println("\n--- TUS DADOS ESPECIALES ---");
            System.out.println("[R] Rápidos: " + nRapidos + " | [L] Lentos: " + nLentos);
            System.out.println("1. Dado normal | 2. Dado Rápido | 3. Dado Lento");

            String opcion = sc.nextLine();
            switch (opcion) {
                case "2":
                    if (nRapidos > 0) {
                        jugador.getInventario().usarDadoEspecifico("Rapido", jugador);
                    } else {
                        jugador.tirarDado();
                    }
                    break;
                case "3":
                    if (nLentos > 0) {
                        jugador.getInventario().usarDadoEspecifico("Lento", jugador);
                    } else {
                        jugador.tirarDado();
                    }
                    break;
                default:
                    jugador.tirarDado();
                    break;
            }
        } else {
            int resultado = jugador.tirarDado();
            System.out.println("Has sacado un " + resultado + ".\n");
        }
    }

    // ==================== GESTIÓN DEL JUEGO ====================

    /**
     * Cambia el turno al siguiente jugador disponible.
     * 
     * <p>
     * Si un jugador está bajo un efecto de bloqueo, se le descuenta un turno
     * y se salta al siguiente. Detecta cuándo se completa una ronda (todos han
     * jugado).
     * </p>
     * 
     * @return true si el cambio de turno ha supuesto completar una ronda completa.
     */
    public boolean cambiarTurno() {
        boolean finRonda = false;
        int intentos = 0;
        int maxIntentos = jugadores.size();

        do {
            if (turnoActual == jugadores.size() - 1) {
                finRonda = true;
            }

            turnoActual = (turnoActual + 1) % jugadores.size();
            Jugador j = jugadores.get(turnoActual);

            if (j.estaBloqueado()) {
                System.out
                        .println(j.getNombre() + " está bloqueado. Turnos restantes: " + (j.getTurnosBloqueados() - 1));
                j.setTurnosBloqueados(j.getTurnosBloqueados() - 1);
                intentos++;
            } else {
                System.out.println("Es el turno de: " + j.getNombre());
                return finRonda;
            }
        } while (intentos < maxIntentos);

        // Si todos están bloqueados, simplemente avanza
        System.out.println("Todos los jugadores están bloqueados. Avanzando ronda...");
        return true;
    }

    /**
     * Verifica si algún jugador ha alcanzado o superado la casilla final.
     * 
     * <p>
     * Si hay ganador, marca la partida como finalizada y guarda el estado final
     * en la base de datos.
     * </p>
     * 
     * @return El objeto {@link Jugador} que ha ganado, o {@code null} si nadie ha
     *         llegado a la meta.
     */
    public Jugador comprobarGanador() {
        for (Jugador j : jugadores) {
            if (j.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
                System.out.println("¡Victoria! El ganador es: " + j.getNombre());
                partidaFinalizada = true;
                ganador = j;
                guardarPartida();
                return j;
            }
        }
        return null;
    }

    /**
     * Detecta y resuelve colisiones entre jugadores en la misma casilla.
     * 
     * <p>
     * Si un jugador cae donde ya hay otro, se inicia un combate (Guerra) o una
     * interacción especial si uno de ellos es la Foca (CPU).
     * </p>
     * 
     * @param jugadorActual El jugador que acaba de realizar el movimiento.
     */
    public void comprobarGuerra(Jugador jugadorActual) {
        boolean guerraEncontrada = false;
        for (int i = 0; i < jugadores.size() && !guerraEncontrada; i++) {
            Jugador oponente = jugadores.get(i);
            if (!oponente.equals(jugadorActual)
                    && oponente.getPosicion() == jugadorActual.getPosicion()
                    && jugadorActual.getPosicion() != 0) {

                System.out.println("\n¡COINCIDENCIA EN CASILLA " + jugadorActual.getPosicion() + "!");

                if (jugadorActual instanceof Foca || oponente instanceof Foca) {
                    Jugador humano = (jugadorActual instanceof Foca) ? oponente : jugadorActual;

                    System.out.println("¡La Foca te ha visto!");

                    if (humano.getInventario().tieneObjeto("Pez")) {
                        System.out.println(
                                humano.getNombre() + " usa un Pez para distraer a la foca. ¡Foca bloqueada 2 turnos!");
                        humano.getInventario().usarObjeto("Pez", humano);

                        if (jugadorActual instanceof Foca)
                            jugadorActual.setTurnosBloqueados(2);
                        else
                            oponente.setTurnosBloqueados(2);

                    } else {
                        System.out
                                .println("¡ZAS! Golpe de cola. " + humano.getNombre() + " vuelve al agujero anterior.");
                        // TODO: Implementar lógica de enviar al agujero anterior
                    }
                } else {
                    System.out.println("¡Guerra de bolas de nieve entre " + jugadorActual.getNombre() + " y "
                            + oponente.getNombre() + "!");
                    Guerra combate = new Guerra();
                    combate.iniciarGuerra(jugadorActual, oponente);
                }
                guerraEncontrada = true;
            }
        }
    }

    // ==================== GETTERS Y SETTERS ====================

    /**
     * Añade un nuevo jugador a la lista de participantes.
     * 
     * @param jugador El pingüino o foca a añadir.
     */
    public void agregarJugador(Jugador jugador) {
        this.jugadores.add(jugador);
    }

    /**
     * Obtiene la lista completa de jugadores de la partida.
     * 
     * @return ArrayList con todos los jugadores.
     */
    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    /**
     * Obtiene un jugador específico según su posición en la lista.
     * 
     * @param indice El índice del jugador deseado.
     * @return El objeto {@link Jugador} correspondiente.
     */
    public Jugador getJugador(int indice) {
        return jugadores.get(indice);
    }

    /**
     * Devuelve el número total de participantes.
     * 
     * @return Cantidad de jugadores.
     */
    public int getNumeroJugadores() {
        return jugadores.size();
    }

    /**
     * Obtiene el índice del jugador que tiene el turno actual.
     * 
     * @return Índice del turno activo.
     */
    public int getTurnoActual() {
        return turnoActual;
    }

    /**
     * Establece el turno actual manualmente (útil para cargas de partida).
     * 
     * @param turnoActual El nuevo índice de turno.
     */
    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    /**
     * Obtiene el tablero asociado a esta partida.
     * 
     * @return El objeto {@link Tablero}.
     */
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Asigna un tablero a la partida.
     * 
     * @param tablero El nuevo tablero.
     */
    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    /**
     * Indica si la partida ha llegado a su fin.
     * 
     * @return {@code true} si hay un ganador, {@code false} en caso contrario.
     */
    public boolean isPartidaFinalizada() {
        return partidaFinalizada;
    }

    /**
     * Obtiene el jugador que ha ganado la partida.
     * 
     * @return El {@link Jugador} ganador o {@code null} si aún no hay.
     */
    public Jugador getGanador() {
        return ganador;
    }

    /**
     * Obtiene el gestor de acceso a la base de datos.
     * 
     * @return La instancia de {@link BBDD}.
     */
    public BBDD getBaseDatos() {
        return baseDatos;
    }

    /**
     * Obtiene el nombre identificativo de la partida.
     * 
     * @return Nombre de la partida.
     */
    public String getNombrePartida() {
        return nombrePartida;
    }

    /**
     * Establece un nombre para la partida.
     * 
     * @param nombrePartida El nuevo nombre.
     */
    public void setNombrePartida(String nombrePartida) {
        this.nombrePartida = nombrePartida;
    }

    /**
     * Guarda el estado actual de la partida en la base de datos.
     * Actualiza tanto la información general del juego como la posición y
     * estado de cada participante.
     */
    public void guardarPartida() {
        // TODO: Implementar guardado de partida con BBDD
        int turnoParaBD = this.turnoActual + 1;
        baseDatos.actualizarEstadoPartida(this.getTablero().getIdPartida(), this);
        for (Jugador j : jugadores) {
            this.baseDatos.actualizarParticipacion(this.getTablero().getIdPartida(), j);
        }

    }

    /**
     * Recupera una partida guardada desde la base de datos.
     * 
     * @param sc Scanner para leer el ID de la partida introducido por el usuario.
     */
    public void cargarPartida(Scanner sc) {
        getBaseDatos().mostrarPartidasPendientes();
        System.out.print("Introduce el ID de la partida que deseas retomar: ");
        try {
            int idCargar = Integer.parseInt(sc.nextLine());

            boolean ok = this.baseDatos.cargarDatosPartida(idCargar, this);

            if (ok) {
                System.out.println("► Partida " + idCargar + " cargada con éxito.");
                System.out.println("Seed del tablero: " + this.tablero.getSeed());
                System.out.println("Jugadores detectados: " + this.jugadores.size());

            } else {
                System.out.println("⚠️ No se encontró la partida o hubo un error en la base de datos.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: El ID debe ser un número.");
        }
    }

    /**
     * Gestiona el sistema de autenticación y registro de usuarios por consola.
     * 
     * @param sc Scanner para la entrada de credenciales.
     * @return El objeto {@link Jugador} autenticado, o {@code null} si el usuario
     *         elige salir.
     */
    public Jugador gestionarAccesoJugador(Scanner sc) {
        while (true) {
            System.out.println("\n=== ACCESO AL IGLÚ ===");
            System.out.println("1. Iniciar Sesión");
            System.out.println("2. Registrar Nuevo Pingüino");
            System.out.println("3. Salir");
            System.out.print("Selecciona una opción: ");

            String opcion = sc.nextLine();

            if (opcion.equals("1")) {
                // Lógica de Login
                System.out.print("Nickname: ");
                String user = sc.nextLine();
                System.out.print("Password: ");
                String pass = sc.nextLine();

                if (this.baseDatos.loginJugador(user, pass)) {
                    int id = this.baseDatos.registrarJugadorSiNoExiste(new Pinguino(0, user, "")); // Pillamos el ID
                    return new Pinguino(id, user, ""); // Éxito
                }

            } else if (opcion.equals("2")) {
                // Lógica de Registro
                System.out.print("Elige tu Nickname: ");
                String user = sc.nextLine();
                System.out.print("Crea tu Password: ");
                String pass = sc.nextLine();

                int nuevoId = this.baseDatos.registrarNuevoJugador(user, pass, false);

                if (nuevoId != -1) {
                    System.out.println("¡Registro completado! Ya puedes jugar.");
                    return new Pinguino(nuevoId, user, "");
                } else {
                    System.out.println("Ese nombre ya existe. Prueba con otro.");
                }

            } else if (opcion.equals("3")) {
                return null; // El usuario canceló
            }
        }
    }

    /**
     * Solicita credenciales al usuario en un bucle hasta que el login es correcto.
     * 
     * @param sc El escáner para leer la entrada.
     * @return El nombre del jugador si se autentica con éxito, null si falla tras
     *         los intentos.
     */
    /*
     * public String autenticarJugadorConBucle(Scanner sc) {
     * int intentos = 0;
     * int maxIntentos = 5;
     * boolean autenticado = false;
     * String user = "";
     * 
     * while (!autenticado && intentos < maxIntentos) {
     * System.out.println("\n--- INICIO DE SESIÓN (Intento " + (intentos + 1) + "/"
     * + maxIntentos + ") ---");
     * System.out.print("Nickname: ");
     * user = sc.nextLine();
     * 
     * System.out.print("Password: ");
     * String pass = sc.nextLine();
     * 
     * // Llamamos al método boolean que ya tenemos en BBDD
     * if (this.baseDatos.loginJugador(user, pass)) {
     * autenticado = true;
     * System.out.println("¡Acceso concedido! Bienvenido, " + user);
     * } else {
     * intentos++;
     * if (intentos < maxIntentos) {
     * System.out.println("Error: Credenciales incorrectas. Prueba de nuevo.");
     * } else {
     * System.out.println("Has agotado los " + maxIntentos + " intentos.");
     * return null; // Devolvemos null para indicar que no se pudo entrar
     * }
     * }
     * }
     * return user;
     * }
     */

}
