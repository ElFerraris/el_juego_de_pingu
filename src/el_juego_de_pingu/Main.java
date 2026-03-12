package el_juego_de_pingu;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
        Juego partida = new Juego();
        
        System.out.println("=== BIENVENIDO A EL JUEGO DE PINGU ===");
        
        int tipo_de_tablero = 0;
        while (tipo_de_tablero != 1 && tipo_de_tablero != 2) {
            System.out.println("1 Para generar seed aleatoria");
            System.out.println("2 Para introducir una seed valida");
            try {
                tipo_de_tablero = Integer.parseInt(sc.nextLine());
                
                if (tipo_de_tablero == 1) {
                    partida.getTablero().generarSeedAleatorioa();
                } else if (tipo_de_tablero == 2) {
                    System.out.print("Introduce la seed: ");
                    String seed = sc.nextLine();
                    partida.getTablero().introducirSeed(seed);
                } else {
                    System.out.println("Error: Por favor, elige 1 o 2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un número (1 o 2).");
            }
        }

        
        // 1. Elegir cantidad de jugadores
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

        // 2. Crear y añadir los jugadores dinámicamente
        for (int i = 1; i <= numJugadores; i++) {
            System.out.print("Introduce el nombre del Jugador " + i + ": ");
            String nombre = sc.nextLine();
            System.out.print("Introduce el color Rojo, Azul, Verde, Amarillo, Naranja, Morado, Rosa");
            String color = sc.nextLine();
            Jugador nuevoJugador = new Jugador("P" + i, nombre, color);
            
            // Añadirlos a la lista de la partida
            partida.jugadores.add(nuevoJugador);
            
            // Regalo de bienvenida (opcional, como tenías antes para pruebas)
            if (i == 1) {
                nuevoJugador.getInventario().agregarObjeto("Pez");
                nuevoJugador.getInventario().agregarObjeto("Pez");
                nuevoJugador.getInventario().agregarObjeto("Pez");
            }
        }
        
        //Foca CPU
        CPU foca = new CPU("CPU01", "Foca Loca");
        partida.jugadores.add(foca);
        
        // 3. Añadir jugadores a la partida (incluyendo la CPU que ya está en Juego)
        partida.iniciarPartida(); 
        
        boolean hayGanador = false;
        
        
        
        
        while (!hayGanador) {
            // Obtenemos el jugador actual a través del índice del turno
            Jugador jugadorActual = partida.jugadores.get(partida.getTurnoActual());
            
            
            System.out.println("------------------------------------");
            System.out.println("TURNO DE: " + jugadorActual.getNombre());
            if(!jugadorActual.estaBloqueado()) {
            	
            int posAntes = jugadorActual.getPosicion(); // Guardamos donde estaba
            
            System.out.println("Posición actual: " + jugadorActual.getPosicion());
            
            if (jugadorActual instanceof CPU) {
                System.out.println("La foca está pensando su movimiento...");
                try { Thread.sleep(1500); } catch (InterruptedException e) {} // Pausa dramática
                
                // La foca usa su propia lógica (decidir si usa dados o no)
                ((CPU) jugadorActual).decidirAccion();
                
                int posDespues = jugadorActual.getPosicion(); // Miramos donde ha llegado

                // COMPROBAR QUIÉN HA SIDO ADELANTADO (Regla de la mitad del inventario)
                for (Jugador p : partida.jugadores) {
                    if (!(p instanceof CPU)) {
                        // Si el jugador humano estaba entre la casilla de origen y la de destino
                        if (p.getPosicion() > posAntes && p.getPosicion() < posDespues) {
                            ((CPU) jugadorActual).atacarJugador(p);
                        }
                    }
                }
                
            } else {
            
            
            System.out.println("Presiona ENTER para tirar el dado...");
            sc.nextLine();
            
            // Lógica de Dados Especiales
            if (jugadorActual.getInventario().tieneObjeto("Dados")) {
                int nRapidos = 0;
                int nLentos = 0;

                for (Objeto d : jugadorActual.getInventario().getDadosLista()) {
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
                            jugadorActual.getInventario().usarDadoEspecifico("Rapido", jugadorActual);
                        } else {
                            jugadorActual.tirarDado();
                        }
                        break;
                    case "3":
                        if (nLentos > 0) {
                            jugadorActual.getInventario().usarDadoEspecifico("Lento", jugadorActual);
                        } else {
                            jugadorActual.tirarDado();
                        }
                        break;
                    default:
                        jugadorActual.tirarDado();
                        break;
                }
            } else {
                int resultado = jugadorActual.tirarDado();
                System.out.println("Has sacado un " + resultado + ". \n");
            }
            
            
            }
            
            System.out.println("Nueva posición: " + jugadorActual.getPosicion());
            
            partida.comprobarGuerra(jugadorActual);
            
            partida.getTablero().aplicarEfectoCasilla(jugadorActual);

            }
            else {
            	System.out.println("Esta BLOQUEADO no se puede mover ");
            	jugadorActual.setTurnosBloqueados(jugadorActual.getTurnosBloqueados() -1);
            	System.out.println("Turnos restantes de bloqueo: " + jugadorActual.getTurnosBloqueados());
            }
            
            // 5. Comprobar Ganador
            if (partida.comprobarGanador()) {
                System.out.println("\n¡ENHORABUENA " + jugadorActual.getNombre() + "! HAS LLEGADO AL IGLÚ FINAL.");
                hayGanador = true;
            } else {
            	boolean haTerminadoRonda = partida.cambiarTurno();
            	if (haTerminadoRonda) {
                    System.out.println("\n--- ESTADO DE LA PARTIDA ---");
                    }else {System.out.println();}
            	}
        }
        sc.close();
    }
}