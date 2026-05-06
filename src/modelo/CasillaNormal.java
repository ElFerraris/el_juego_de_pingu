package modelo;

/**
 * Casilla estándar que no aplica ninguna penalización ni beneficio.
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaNormal extends Casilla {

    /**
     * Constructor de la casilla normal.
     * 
     * @param posicion Índice en el tablero.
     * @param tipo     Identificador de tipo.
     */
    public CasillaNormal(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        return "";
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarNormal.png";
    }
}
