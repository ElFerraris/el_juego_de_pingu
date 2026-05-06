package modelo;

import java.util.ArrayList;

/**
 * Representa a la Foca, el antagonista controlado por la CPU.
 * 
 * <p>
 * A diferencia de los jugadores humanos, la Foca posee comportamientos
 * automatizados de ataque y toma de decisiones. Su objetivo es dificultar
 * el avance de los pingüinos mediante colisiones y el uso estratégico de dados.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Foca extends Jugador {

    /**
     * Constructor para la Foca.
     * 
     * @param id     Identificador único.
     * @param nombre Nombre de la foca.
     */
    public Foca(int id, String nombre) {
        super(id, nombre, "Gris");
    }

    /**
     * Lógica simplificada de decisión de acción para la Foca.
     * 
     * <p>
     * Intenta utilizar dados especiales si están disponibles en su inventario
     * para maximizar su desplazamiento, o lanza un dado estándar en su defecto.
     * </p>
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
     * Ejecuta un ataque sobre un jugador objetivo.
     * 
     * <p>
     * Se activa cuando la foca colisiona o adelanta a un pingüino, resultando
     * en la pérdida de recursos del inventario del objetivo.
     * </p>
     * 
     * @param objetivo El {@link Jugador} que recibe el ataque.
     */
    public void atacarJugador(Jugador objetivo) {
        System.out.println("¡LA FOCA " + this.getNombre() + " HA PASADO POR ENCIMA DE " + objetivo.getNombre() + "!");
        System.out.println("¡Le ha robado la mitad de su inventario con un golpe de aleta!");
        objetivo.getInventario().serAtacado();
        System.out.println("Inventario de " + objetivo.getNombre() + " reducido.");
    }

    /**
     * Analiza una lista de jugadores para identificar al oponente más adelantado.
     * 
     * @param jugadores Lista de todos los participantes en la partida.
     * @return El {@link Jugador} humano que se encuentra en la posición más
     *         avanzada.
     */
    public Jugador seleccionarObjetivo(ArrayList<Jugador> jugadores) {
        Jugador objetivo = null;
        int maxPosicion = -1;

        for (Jugador j : jugadores) {
            if (!(j instanceof Foca) && j.getPosicion() > maxPosicion) {
                maxPosicion = j.getPosicion();
                objetivo = j;
            }
        }
        return objetivo;
    }
}
