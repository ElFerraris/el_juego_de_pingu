package controlador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import modelo.*;
import datos.BBDD;

/**
 * Clase principal que controla la lógica del juego.
 * Gestiona turnos, jugadores, tablero y reglas del juego.
 */
public class Juego {

    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnoActual;
    private boolean partidaFinalizada;
    private Jugador ganador;
    private BBDD baseDatos; // Composición en vez de herencia

    public Juego() {
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<>();
        this.partidaFinalizada = false;
        this.baseDatos = new BBDD();
    }

    // ==================== CONFIGURACIÓN ====================

    /**
     * Configura la partida: genera el tablero y crea los jugadores.
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
                "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"
            ));
            
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
                    for (String c : coloresDisponibles) {
                        if (c.equalsIgnoreCase(colorElegido)) {
                            colorEncontrado = c;
                            break;
                        }
                    }

                    if (colorEncontrado != null) {
                        colorElegido = colorEncontrado;
                        coloresDisponibles.remove(colorEncontrado);
                        colorValido = true;
                    } else {
                        System.out.println("❌ Color no válido o ya elegido.");
                    }
                }

                agregarJugador(new Jugador(1 + i, nombre, colorElegido));
            }
        
        
        
        
        /*
        // Elegir cantidad de jugadores
        int numJugadores = 0;
        while (numJugadores < 2 || numJugadores > 4) {
            System.out.print("¿Cuántos pingüinos van a jugar? (2-4): ");
            try {
                numJugadores = Integer.parseInt(sc.nextLine());
                if (numJugadores < 2 || numJugadores > 4) {
                    System.out.println("Error: El juego es para un mínimo de 2 y un máximo de 4 jugadores.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduce un número válido.");
            }
        }

         // 1. Definimos la lista de colores disponibles
        ArrayList<String> coloresDisponibles = new ArrayList<>(Arrays.asList(
            "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"
        ));
        
        
        
        
        // Crear jugadores
        for (int i = 1; i <= numJugadores; i++) {
            System.out.print("Introduce el nombre del Jugador " + i + ": ");
            String nombre = sc.nextLine();

            String colorElegido = "";
            boolean colorValido = false;

            // 2. Bucle de validación de color
            while (!colorValido) {
                System.out.println("Colores disponibles: " + coloresDisponibles);
                System.out.print("Introduce el color para " + nombre + ": ");
                colorElegido = sc.nextLine();

                // Buscamos si el color existe en la lista (ignorando mayúsculas)
                String colorEncontrado = null;
                for (String c : coloresDisponibles) {
                    if (c.equalsIgnoreCase(colorElegido)) {
                        colorEncontrado = c;
                        break;
                    }
                }

                if (colorEncontrado != null) {
                    // Si existe, lo asignamos y lo BORRAMOS de la lista para el siguiente
                    colorElegido = colorEncontrado; 
                    coloresDisponibles.remove(colorEncontrado);
                    colorValido = true;
                } else {
                    System.out.println("❌ Error: El color '" + colorElegido + "' no está disponible o ya fue elegido.");
                }
            }

            // Agregamos al jugador con su color único asegurado
            agregarJugador(new Jugador(1 + i, nombre, colorElegido));
        }
        */
        

        // Añadir la Foca CPU
        CPU foca = new CPU(0, "Foca Loca");
        agregarJugador(foca);
    }

    /**
     * Inicia la partida poniendo el turno a 0.
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
        		if(this.baseDatos.guardarNuevaPartida(this) == 0) {exito = false;}
        		else {exito = true; this.getTablero().setIdPartida(this.baseDatos.guardarNuevaPartida(this));}
                for (Jugador j : jugadores) {
                    this.baseDatos.insertarParticipacion(this.getTablero().getIdPartida(),j.getId(),j.getColor());
                }	
        		

        if (exito) {
            System.out.println("► Registro en Oracle completado con éxito.");
        } else {
            System.out.println("⚠️ No se pudo registrar la partida en la base de datos.");
        }
        
    }

    // ==================== TURNOS ====================

    /**
     * Ejecuta el turno del jugador actual.
     */
    public void ejecutarTurno(Scanner sc) {
        Jugador jugadorActual = jugadores.get(turnoActual);

        System.out.println("------------------------------------");
        System.out.println("TURNO DE: " + jugadorActual.getNombre());

        if (!jugadorActual.estaBloqueado()) {
            int posAntes = jugadorActual.getPosicion();
            System.out.println("Posición actual: " + jugadorActual.getPosicion());

            if (jugadorActual instanceof CPU) {
                ejecutarTurnoCPU((CPU) jugadorActual, posAntes);
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
     * Lógica del turno de la CPU (Foca).
     */
    private void ejecutarTurnoCPU(CPU foca, int posAntes) {
        System.out.println("La foca está pensando su movimiento...");
        foca.decidirAccion();

        int posDespues = foca.getPosicion();

        // Comprobar si la foca ha adelantado a algún jugador humano
        for (Jugador p : jugadores) {
            if (!(p instanceof CPU)) {
                if (p.getPosicion() > posAntes && p.getPosicion() < posDespues) {
                    foca.atacarJugador(p);
                }
            }
        }
    }

    /**
     * Lógica del turno de un jugador humano.
     */
    private void ejecutarTurnoHumano(Jugador jugador, Scanner sc) {
        System.out.println("Presiona ENTER para tirar el dado...");
        String variable ="";
        variable = sc.nextLine();
        if(variable.equals("Guardar")) {guardarPartida();}
        if (jugador.getInventario().tieneObjeto("Dados")) {
            int nRapidos = 0;
            int nLentos = 0;

            for (Objeto d : jugador.getInventario().getListaDados()) {
                if (d instanceof DadoRapido) nRapidos++;
                if (d instanceof DadoLento) nLentos++;
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
     * Cambia al siguiente turno. Si un jugador está bloqueado, lo salta.
     * Usa un bucle con contador de seguridad para evitar StackOverflowError.
     * @return true si se ha completado una ronda completa.
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
                System.out.println(j.getNombre() + " está bloqueado. Turnos restantes: " + (j.getTurnosBloqueados() - 1));
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
     * Comprueba si algún jugador ha llegado a la meta.
     * @return el jugador ganador, o null si nadie ha ganado aún.
     */
    public Jugador comprobarGanador() {
        for (Jugador j : jugadores) {
            if (j.getPosicion() >= Tablero.TAMANYO_TABLERO) {
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
     * Comprueba si hay un conflicto en la casilla actual del jugador.
     */
    public void comprobarGuerra(Jugador jugadorActual) {
        for (Jugador oponente : jugadores) {
            if (!oponente.equals(jugadorActual)
                && oponente.getPosicion() == jugadorActual.getPosicion()
                && jugadorActual.getPosicion() != 0) {

                System.out.println("\n¡COINCIDENCIA EN CASILLA " + jugadorActual.getPosicion() + "!");

                if (jugadorActual instanceof CPU || oponente instanceof CPU) {
                    Jugador humano = (jugadorActual instanceof CPU) ? oponente : jugadorActual;

                    System.out.println("¡La Foca te ha visto!");

                    if (humano.getInventario().tieneObjeto("Pez")) {
                        System.out.println(humano.getNombre() + " usa un Pez para distraer a la foca. ¡Foca bloqueada 2 turnos!");
                        humano.getInventario().usarObjeto("Pez", humano);

                        if (jugadorActual instanceof CPU) jugadorActual.setTurnosBloqueados(2);
                        else oponente.setTurnosBloqueados(2);

                    } else {
                        System.out.println("¡ZAS! Golpe de cola. " + humano.getNombre() + " vuelve al agujero anterior.");
                        // TODO: Implementar lógica de enviar al agujero anterior
                    }
                } else {
                    System.out.println("¡Guerra de bolas de nieve entre " + jugadorActual.getNombre() + " y " + oponente.getNombre() + "!");
                    Guerra combate = new Guerra();
                    combate.iniciarGuerra(jugadorActual, oponente);
                }
                return;
            }
        }
    }
    
    
    

    // ==================== GETTERS Y SETTERS ====================

    public void agregarJugador(Jugador jugador) {
        this.jugadores.add(jugador);
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public Jugador getJugador(int indice) {
        return jugadores.get(indice);
    }

    public int getNumeroJugadores() {
        return jugadores.size();
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    public Tablero getTablero() {
        return tablero;
    }

    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    public boolean isPartidaFinalizada() {
        return partidaFinalizada;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public BBDD getBaseDatos() {
        return baseDatos;
    }

    
    public void guardarPartida() {
        // TODO: Implementar guardado de partida con BBDD
    	int turnoParaBD = this.turnoActual + 1;
    	baseDatos.actualizarEstadoPartida(this.getTablero().getIdPartida(),this);
        for (Jugador j : jugadores) {
            this.baseDatos.actualizarParticipacion(this.getTablero().getIdPartida(),j);
        }
    	
    }

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
     * Solicita credenciales al usuario en un bucle hasta que el login es correcto.
     * @param sc El escáner para leer la entrada.
     * @return El nombre del jugador si se autentica con éxito, null si falla tras los intentos.
     */
    public String autenticarJugadorConBucle(Scanner sc) {
        int intentos = 0;
        int maxIntentos = 5;
        boolean autenticado = false;
        String user = "";

        while (!autenticado && intentos < maxIntentos) {
            System.out.println("\n--- INICIO DE SESIÓN (Intento " + (intentos + 1) + "/" + maxIntentos + ") ---");
            System.out.print("Nickname: ");
            user = sc.nextLine();
            
            System.out.print("Password: ");
            String pass = sc.nextLine();

            // Llamamos al método boolean que ya tenemos en BBDD
            if (this.baseDatos.loginJugador(user, pass)) {
                autenticado = true;
                System.out.println("¡Acceso concedido! Bienvenido, " + user);
            } else {
                intentos++;
                if (intentos < maxIntentos) {
                    System.out.println("Error: Credenciales incorrectas. Prueba de nuevo.");
                } else {
                    System.out.println("Has agotado los " + maxIntentos + " intentos.");
                    return null; // Devolvemos null para indicar que no se pudo entrar
                }
            }
        }
        return user; 
    }
    
    
}
