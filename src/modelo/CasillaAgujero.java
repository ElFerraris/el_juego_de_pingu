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
    public void activarEfecto(Jugador jugador) {
        enviarAgujeroAnterior(jugador);
    }

    private void enviarAgujeroAnterior(Jugador jugador) {
        System.out.println(jugador.getNombre() + " cae en un agujero y retrocede a la casilla " + posicionAgujeroAnterior + ".");
        jugador.setPosicion(posicionAgujeroAnterior);
    }
}
