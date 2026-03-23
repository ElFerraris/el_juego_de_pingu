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
    public void activarEfecto(Jugador jugador, controlador.Juego juego) {
        enviarAgujeroAnterior(jugador, juego);
    }

    private void enviarAgujeroAnterior(Jugador jugador, controlador.Juego juego) {
        String msg = "¡AGUJERO! " + jugador.getNombre() + " cae y retrocede a la casilla " + posicionAgujeroAnterior + ".";
        if (juego != null) juego.setLogMessage(msg);
        System.out.println(msg);
        jugador.setPosicion(posicionAgujeroAnterior);
    }
}
