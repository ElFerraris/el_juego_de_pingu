package modelo;

/**
 * Casilla oso: si el jugador tiene un pez, soborna al oso.
 * Si no, vuelve al inicio.
 */
public class CasillaOso extends Casilla {

    public CasillaOso(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public void activarEfecto(Jugador jugador, controlador.Juego juego) {
        if (jugador.sobornarOso()) {
            String msg = jugador.getNombre() + " usó un Pez y ha sobornado al oso polar.";
            if (juego != null) juego.setLogMessage(msg);
            System.out.println(msg);
        } else {
            volverAlInicio(jugador);
            String msg = "¡Oh no! El oso ha atrapado a " + jugador.getNombre() + " y vuelve al inicio.";
            if (juego != null) juego.setLogMessage(msg);
            System.out.println(msg);
        }
    }

    private void volverAlInicio(Jugador jugador) {
        jugador.setPosicion(0);
    }
}
