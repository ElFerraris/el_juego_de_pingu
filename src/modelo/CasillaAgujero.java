package modelo;

/**
 * Casilla agujero: envía al jugador al agujero anterior.
 * Si no hay agujero anterior, envía al inicio (posición 0).
 */
public class CasillaAgujero extends Casilla {

    private int posicionAgujeroAnterior;

    public CasillaAgujero(int posicion, String tipo, int posicionAgujeroAnterior) {
        super(posicion, tipo);
        this.posicionAgujeroAnterior = posicionAgujeroAnterior;
    }

    public int getPosicionAgujeroAnterior() {
        return posicionAgujeroAnterior;
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        jugador.setPosicion(posicionAgujeroAnterior);
        return jugador.getNombre() + " cae en un agujero y retrocede a la casilla " + posicionAgujeroAnterior + ".";
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarAgujero.png";
    }
}
