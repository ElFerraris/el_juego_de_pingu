package modelo;

/**
 * Casilla que representa un encuentro con un Oso polar.
 * 
 * <p>
 * Si el jugador dispone de un pez en su inventario, puede sobornar al oso y
 * continuar su camino. En caso contrario, el jugador huye asustado hasta el
 * inicio.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaOso extends Casilla {

    /**
     * Constructor de la casilla del oso.
     * 
     * @param posicion Índice en el tablero.
     * @param tipo     Identificador de tipo.
     */
    public CasillaOso(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        if (jugador.sobornarOso()) {
            return "OSO: SOBORNADO";
        } else {
            jugador.setPosicion(0);
            return "OSO: AL INICIO";
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarOso.png";
    }
}
