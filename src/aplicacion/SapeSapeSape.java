package aplicacion;
import java.util.Scanner;

import controlador.Juego;
import modelo.Jugador;

/**
 * Punto de entrada del juego "El Juego de Pingu".
 * Gestiona el flujo principal: configurar, jugar y reiniciar.
 */
/*
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean jugarOtraVez = true;
        
        while (jugarOtraVez) {			
            Juego partida = new Juego();
            System.out.println("=== BIENVENIDO A EL JUEGO DE PINGU ===");
            partida.gestionarAccesoJugador(sc);
            System.out.println("1. Nueva Partida");
            System.out.println("2. Cargar Partida Guardada");
            
            String modo = sc.nextLine();
            if (modo.equals("2")) {
                // USAR EL MÉTODO QUE CREAMOS ANTES
                partida.cargarPartida(sc); 
            } else {
            // 1. Configurar la partida (seed + jugadores)
            partida.configurarPartida(sc);
            partida.iniciarPartida();
            }
            // 2. Bucle principal del juego
            boolean hayGanador = false;

            while (!hayGanador) {
                partida.ejecutarTurno(sc);

                // Comprobar ganador (devuelve el jugador ganador o null)
                Jugador ganador = partida.comprobarGanador();
                if (ganador != null) {
                    System.out.println("\n¡ENHORABUENA " + ganador.getNombre() + "! ¡HAS LLEGADO AL IGLÚ FINAL!");
                    hayGanador = true;
                } else {
                    boolean haTerminadoRonda = partida.cambiarTurno();
                    if (haTerminadoRonda) {
                        System.out.println("\n--- ESTADO DE LA PARTIDA ---");
                    }
                }
            }

            // 3. Menú de salida / reinicio
            System.out.println("¿Qué deseas hacer ahora?");
            System.out.println("1. Jugar una nueva partida");
            System.out.println("2. Salir del juego");
            System.out.print("Opción: ");

            String eleccion = sc.nextLine();
            if (eleccion.equals("2")) {
                jugarOtraVez = false;
                System.out.println("\nGracias por jugar. ¡Hasta la próxima!");
            } else {
                System.out.println("\nReiniciando el mundo de los pingüinos...\n");
            }
        }
        											// partida.getBaseDatos().mostrarRankingMasPartidas(); Recuerdalo Dani
        sc.close();
    }
}
*/