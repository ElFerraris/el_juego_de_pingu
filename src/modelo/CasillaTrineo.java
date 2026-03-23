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
    public void activarEfecto(Jugador jugador) {
        avanzarSiguienteTrineo(jugador);
    }

    public void avanzarSiguienteTrineo(Jugador jugador) {
        System.out.println(jugador.getNombre() + " toma el trineo y avanza a la casilla " + posicionSiguienteTrineo + "!");
        jugador.setPosicion(posicionSiguienteTrineo);
    }
}