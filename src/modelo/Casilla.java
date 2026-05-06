package modelo;

/**
 * Representa un componente básico del tablero de juego.
 * 
 * <p>
 * Es una clase abstracta que define la estructura mínima de cualquier casilla:
 * su posición en el tablero y su identificador de tipo. Las subclases deben
 * definir el comportamiento específico (efecto) y la representación visual
 * (sprite).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public abstract class Casilla {

    /** Índice de la casilla en el tablero. */
    private int posicion;
    /** Nombre descriptivo del tipo de casilla. */
    private String tipo;

    /**
     * Constructor base para una casilla.
     * 
     * @param posicion Índice entero en el tablero.
     * @param tipo     Cadena descriptiva del tipo.
     */
    public Casilla(int posicion, String tipo) {
        this.posicion = posicion;
        this.tipo = tipo;
    }

    /**
     * Define la acción o regla especial que se ejecuta cuando un jugador aterriza
     * en esta casilla.
     * 
     * @param jugador El {@link Jugador} que ha caído en la casilla.
     * @return Un mensaje explicativo del efecto aplicado para el registro de la
     *         partida.
     */
    public abstract String activarEfecto(Jugador jugador);

    /**
     * Obtiene la posición de la casilla.
     * 
     * @return El índice de la casilla.
     */
    public int getPosicion() {
        return posicion;
    }

    /**
     * Obtiene el nombre del tipo de casilla.
     * 
     * @return Cadena con el tipo.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Proporciona la ruta del archivo de imagen (asset) que representa visualmente
     * a la casilla.
     * 
     * @return Cadena con la ruta relativa al recurso.
     */
    public abstract String getSpritePath();
}
