package modelo;

/**
 * Casilla de penalización que desplaza al jugador hacia atrás.
 * 
 * <p>
 * Representa un obstáculo que, al ser activado, obliga al jugador a retroceder
 * hasta la ubicación de un agujero previo o, en su defecto, al inicio del
 * tablero.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaAgujero extends Casilla {

    /** Posición de destino en caso de retroceso. */
    private int posicionAgujeroAnterior;

    /**
     * Constructor para la Casilla Agujero.
     * 
     * @param posicion                Índice en el tablero.
     * @param tipo                    Nombre descriptivo del tipo.
     * @param posicionAgujeroAnterior Índice de destino del retroceso.
     */
    public CasillaAgujero(int posicion, String tipo, int posicionAgujeroAnterior) {
        super(posicion, tipo);
        this.posicionAgujeroAnterior = posicionAgujeroAnterior;
    }

    /**
     * Obtiene la posición del agujero previo en la cadena de conexión.
     * 
     * @return Índice de destino.
     */
    public int getPosicionAgujeroAnterior() {
        return posicionAgujeroAnterior;
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        jugador.setPosicion(posicionAgujeroAnterior);
        return "AGUJERO: CASILLA " + posicionAgujeroAnterior;
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarAgujero.png";
    }
}
