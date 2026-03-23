package modelo;

/**
 * Clase abstracta que representa una casilla del tablero.
 * Cada subclase implementa su propio efecto al caer un jugador.
 */
public abstract class Casilla {

    private int posicion;
    private String tipo;

    public Casilla(int posicion, String tipo) {
        this.posicion = posicion;
        this.tipo = tipo;
    }

    /**
     * Activa el efecto de esta casilla sobre el jugador.
     * Cada subclase DEBE implementar su propia lógica.
     */
    public abstract void activarEfecto(Jugador jugador, controlador.Juego juego);

    public int getPosicion() {
        return posicion;
    }

    public String getTipo() {
        return tipo;
    }
}
