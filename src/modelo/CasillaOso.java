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
    public String activarEfecto(Jugador jugador) {
        if (jugador.sobornarOso()) {
            return "¡Un Oso salvaje! " + jugador.getNombre() + " le da un pez y el oso le deja en paz.";
        } else {
            jugador.setPosicion(0);
            return "¡Un Oso salvaje! " + jugador.getNombre() + " no tiene peces y huye asustado al inicio.";
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/CasillaOso.png";
    }
}
