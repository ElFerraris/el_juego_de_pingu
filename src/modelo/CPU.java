package modelo;

import java.util.ArrayList;

/**
 * Representa la Foca (CPU) que juega automáticamente.
 * Hereda de Jugador porque la foca ES un tipo de jugador.
 */
public class CPU extends Jugador {

    public CPU(String id, String nombre) {
        super(id, nombre, "Rojo");
    }

    /**
     * La foca decide qué dado usar automáticamente.
     * Prioriza el dado rápido para avanzar lo máximo posible.
     */
    public void decidirAccion() {
        System.out.println(this.getNombre() + " está decidiendo qué dado usar...");

        if (getInventario().tieneObjetoEspecifico("Rapido")) {
            System.out.println("La foca decide usar un Dado Rápido para avanzar mucho.");
            getInventario().usarDadoEspecifico("Rapido", this);
        } else {
            int resultado = this.tirarDado();
            System.out.println("La foca lanza el dado normal y saca un " + resultado);
        }
    }

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
