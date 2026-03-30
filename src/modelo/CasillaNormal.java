package modelo;

/**
 * Casilla normal: no tiene ningún efecto especial.
 */
public class CasillaNormal extends Casilla {

    public CasillaNormal(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        return "";
    }
}
