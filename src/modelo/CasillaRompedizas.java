package modelo;

/**
 * Casilla de suelo inestable que penaliza el exceso de carga en el inventario.
 * 
 * <p>
 * El efecto varía según la cantidad total de objetos acumulados:
 * <ul>
 * <li>0 objetos: Tránsito seguro.</li>
 * <li>1-5 objetos: El jugador pierde un turno equilibrándose.</li>
 * <li>Más de 5 objetos: El hielo se rompe y el jugador regresa al inicio.</li>
 * </ul>
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaRompedizas extends Casilla {

    /**
     * Constructor para la Casilla Rompediza.
     * 
     * @param posicion Índice en el tablero.
     * @param tipo     Identificador de tipo.
     */
    public CasillaRompedizas(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        return calcularPenalizacion(jugador);
    }

    private String calcularPenalizacion(Jugador jugador) {
        int total = jugador.getInventario().getCantidad("Total");

        if (total == 0) {
            return jugador.getNombre() + " pasa sin penalización por las casillas rompedizas.";
        } else if (total <= 5) {
            jugador.setTurnosBloqueados(jugador.getTurnosBloqueados() + 1);
            return "El hielo cruje por el peso... " + jugador.getNombre() + " pierde un turno equilibrándose.";
        } else {
            jugador.setPosicion(0);
            return "¡CRACK! Demasiado peso. El hielo se rompe y " + jugador.getNombre()
                    + " se cae al agua volviendo al inicio.";
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarRompediza.png";
    }
}
