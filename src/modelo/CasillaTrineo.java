package modelo;

/**
 * Casilla de beneficio que impulsa al jugador hacia adelante.
 * 
 * <p>
 * Representa un transporte rápido que, al ser activado, desplaza al jugador
 * hasta la ubicación del siguiente trineo en el tablero.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaTrineo extends Casilla {

    /** Posición de destino del impulso. */
    private int posicionSiguienteTrineo;

    /**
     * Constructor para la Casilla Trineo.
     * 
     * @param posicion                Índice en el tablero.
     * @param tipo                    Nombre descriptivo del tipo.
     * @param posicionSiguienteTrineo Índice de destino del impulso.
     */
    public CasillaTrineo(int posicion, String tipo, int posicionSiguienteTrineo) {
        super(posicion, tipo);
        this.posicionSiguienteTrineo = posicionSiguienteTrineo;
    }

    /**
     * Obtiene la posición del siguiente trineo en el tablero.
     * 
     * @return Índice de destino.
     */
    public int getPosicionSiguienteTrineo() {
        return posicionSiguienteTrineo;
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        return avanzarSiguienteTrineo(jugador);
    }

    public String avanzarSiguienteTrineo(Jugador jugador) {
        jugador.setPosicion(posicionSiguienteTrineo);
        return "TRINEO: CASILLA " + posicionSiguienteTrineo;
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarTrineo.png";
    }
}
