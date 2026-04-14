package modelo;

/**
 * Casilla trineo: avanza al jugador hasta el siguiente trineo.
 */
public class CasillaTrineo extends Casilla {

    private int posicionSiguienteTrineo;

    public CasillaTrineo(int posicion, String tipo, int posicionSiguienteTrineo) {
        super(posicion, tipo);
        this.posicionSiguienteTrineo = posicionSiguienteTrineo;
    }

    public int getPosicionSiguienteTrineo() {
        return posicionSiguienteTrineo;
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        return avanzarSiguienteTrineo(jugador);
    }

    public String avanzarSiguienteTrineo(Jugador jugador) {
        jugador.setPosicion(posicionSiguienteTrineo);
        return jugador.getNombre() + " toma el trineo y avanza a la casilla " + posicionSiguienteTrineo + "!";
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/CasillaTrineo.png";
    }
}
