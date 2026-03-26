package modelo;

import java.util.ArrayList;

/**
 * Representa la Foca (CPU) que juega automáticamente.
 * Hereda de Jugador porque la foca ES un tipo de jugador.
 */
public class CPU extends Jugador {

    public CPU(int id, String nombre) {
        super(id, nombre, "Gris");
    }
    
    
    /**
     * Versión sin parámetros (compatibilidad con código legado y llamadas simples).
     * Simplemente tira el dado normal.
     */
    public void decidirAccion() {
        this.tirarDado();
    }

    public void decidirAccion(Tablero tablero, ArrayList<Jugador> jugadores) {
        int posActual = this.getPosicion();
        
        // Variables para decidir
        boolean usarRapido = false;
        boolean usarLento = false;
        
        // 1. EVALUAR AGRESIÓN (Prioridad Máxima)
        // Si hay un jugador a tiro de Dado Rápido (5-10), ¡ir a por él!
        for (Jugador j : jugadores) {
            if (!(j instanceof CPU)) {
                int distancia = j.getPosicion() - posActual;
                if (distancia >= 5 && distancia <= 10 && getInventario().tieneObjetoEspecifico("Rapido")) {
                    System.out.println("IA: Objetivo detectado. Activando motor rápido para embestir.");
                    usarRapido = true;
                    break;
                }
            }
        }

        // 2. EVALUAR PELIGRO (Si no hay agresión clara)
        if (!usarRapido) {
            for (int i = 1; i <= 6; i++) {
                int casillaDestino = posActual + i;
                if (casillaDestino < Tablero.TAMANYO_TABLERO) {
                    String tipo = tablero.getCasilla(casillaDestino).getTipo();
                    
                    // Si el Dado Normal nos metería en un Oso o Forat...
                    if (tipo.equals("Casilla OSO") || tipo.equals("Casilla AGUJERO")) {
                        if (getInventario().tieneObjetoEspecifico("Lento")) {
                            System.out.println("IA: Peligro detectado a corta distancia. Frenando con Dado Lento.");
                            usarLento = true;
                            break;
                        }
                    }
                }
            }
        }

        // 3. EJECUCIÓN
        if (usarRapido) {
            getInventario().usarDadoEspecifico("Rapido", this);
        } else if (usarLento) {
            getInventario().usarDadoEspecifico("Lento", this);
        } else {
            // Por defecto, si tiene dado rápido y no hay peligros cerca, lo usa para ganar
            if (getInventario().tieneObjetoEspecifico("Rapido")) {
                getInventario().usarDadoEspecifico("Rapido", this);
            } else {
                this.tirarDado();
            }
        }
        }
    
    
    

    /**
     * La foca decide qué dado usar automáticamente.
     * Prioriza el dado rápido para avanzar lo máximo posible.
     */
    /*
    public void decidirAccion() {
        System.out.println(this.getNombre() + " está decidiendo qué dado usar...");

        if (getInventario().tieneObjetoEspecifico("Rapido")) {
            System.out.println("La foca decide usar un Dado Rápido para avanzar mucho.");
            getInventario().usarDadoEspecifico("Rapido", this);
        } else {
            int resultado = this.tirarDado();
            System.out.println("La foca lanza el dado normal y saca un " + resultado);
        }
    }*/

    /**
     * La foca ataca a un jugador que ha adelantado, robándole la mitad del inventario.
     */
    public void atacarJugador(Jugador objetivo) {
        System.out.println("¡LA FOCA " + this.getNombre() + " HA PASADO POR ENCIMA DE " + objetivo.getNombre() + "!");
        System.out.println("¡Le ha robado la mitad de su inventario con un golpe de aleta!");
        objetivo.getInventario().serAtacado();
        System.out.println("Inventario de " + objetivo.getNombre() + " reducido.");
    }

    /**
     * Selecciona el jugador humano más adelantado como objetivo prioritario.
     */
    public Jugador seleccionarObjetivo(ArrayList<Jugador> jugadores) {
        Jugador objetivo = null;
        int maxPosicion = -1;

        for (Jugador j : jugadores) {
            if (!(j instanceof CPU) && j.getPosicion() > maxPosicion) {
                maxPosicion = j.getPosicion();
                objetivo = j;
            }
        }
        return objetivo;
    }
}
