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
    public void activarEfecto(Jugador jugador, controlador.Juego juego) {
        avanzarSiguienteTrineo(jugador, juego);
    }

    private void avanzarSiguienteTrineo(Jugador jugador, controlador.Juego juego) {
        String msg = "¡TRINEO! " + jugador.getNombre() + " monta el trineo y avanza a la casilla " + posicionSiguienteTrineo + "!";
        if (juego != null) juego.setLogMessage(msg);
        System.out.println(msg);
        jugador.setPosicion(posicionSiguienteTrineo);
    }
}
