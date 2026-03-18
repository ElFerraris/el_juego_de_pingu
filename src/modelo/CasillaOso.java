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
    public void activarEfecto(Jugador jugador) {
        if (jugador.sobornarOso()) {
            System.out.println(jugador.getNombre() + " ha sobornado al oso!");
        } else {
            volverAlInicio(jugador);
            System.out.println(jugador.getNombre() + " ha vuelto al inicio.");
        }
    }

    private void volverAlInicio(Jugador jugador) {
        jugador.setPosicion(0);
    }
}
