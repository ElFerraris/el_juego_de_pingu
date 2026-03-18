package controlador;

import modelo.Jugador;

/**
 * Gestiona las guerras de bolas de nieve entre dos jugadores.
 */
public class Guerra {

    /**
     * Inicia una guerra de bolas de nieve. El que más bolas tiene gana,
     * y el perdedor retrocede la diferencia de casillas.
     * Ambos jugadores gastan todas sus bolas al final.
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
     * Elimina todas las bolas de nieve del inventario del jugador.
     */
    private void limpiarBolas(Jugador jugador) {
        while (jugador.getInventario().getCantidad("BolaNieve") > 0) {
            jugador.getInventario().eliminarObjeto("BolaNieve");
        }
    }
}
