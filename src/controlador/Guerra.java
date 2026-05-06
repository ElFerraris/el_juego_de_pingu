package controlador;

import modelo.Jugador;

/**
 * Gestiona la resolución de conflictos (Guerra de bolas de nieve) entre
 * jugadores.
 * 
 * <p>
 * Cuando dos jugadores coinciden en la misma casilla, se dispara una "Guerra".
 * La resolución se basa en la cantidad de bolas de nieve que posee cada
 * participante
 * en su {@link modelo.Inventario}. El ganador mantiene su posición, mientras
 * que
 * el perdedor es penalizado retrocediendo tantas casillas como la diferencia de
 * bolas.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Guerra {

    /**
     * Inicia y resuelve una guerra de bolas de nieve entre dos jugadores.
     * 
     * <p>
     * Se comparan las existencias de bolas de nieve. El jugador con menos bolas
     * retrocede la diferencia. En caso de empate, ambos permanecen en la casilla.
     * Al finalizar el combate, ambos jugadores agotan sus reservas de proyectiles.
     * </p>
     * 
     * @param j1 El primer jugador (normalmente el que llega a la casilla).
     * @param j2 El segundo jugador (el que ya ocupaba la casilla).
     */
    public void iniciarGuerra(Jugador j1, Jugador j2) {
        int bolasJ1 = j1.getInventario().getCantidad("BolaNieve");
        int bolasJ2 = j2.getInventario().getCantidad("BolaNieve");

        if (bolasJ1 > bolasJ2) {
            int diferencia = bolasJ1 - bolasJ2;
            System.out.println(j1.getNombre() + " gana. " + j2.getNombre() + " retrocede " + diferencia + " casillas.");
            j2.retroceder(diferencia);
        } else if (bolasJ2 > bolasJ1) {
            int diferencia = bolasJ2 - bolasJ1;
            System.out.println(j2.getNombre() + " gana. " + j1.getNombre() + " retrocede " + diferencia + " casillas.");
            j1.retroceder(diferencia);
        } else {
            System.out.println("Empate técnico: Nadie retrocede.");
        }

        // Ambos gastan todas sus bolas
        limpiarBolas(j1);
        limpiarBolas(j2);
    }

    /**
     * Vacía por completo el inventario de proyectiles de un jugador.
     * 
     * @param jugador El jugador al que se le retirarán las bolas de nieve.
     */
    private void limpiarBolas(Jugador jugador) {
        while (jugador.getInventario().getCantidad("BolaNieve") > 0) {
            jugador.getInventario().eliminarObjeto("BolaNieve");
        }
    }
}
