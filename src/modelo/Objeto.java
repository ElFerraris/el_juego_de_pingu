package modelo;

/**
 * Representa la base para cualquier elemento interactuable del inventario.
 * 
 * <p>
 * Define la estructura común para los objetos que un jugador puede recolectar
 * y utilizar durante la partida, como recursos de supervivencia o herramientas
 * de desplazamiento.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public abstract class Objeto {

    /** Nombre legible del objeto. */
    private String nombre;
    /** Categoría del objeto para lógica de inventario. */
    private String tipo;

    /**
     * Constructor base para un objeto.
     * 
     * @param nombre Etiqueta identificativa.
     * @param tipo   Categoría técnica.
     */
    public Objeto(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    /**
     * Ejecuta la lógica principal asociada al uso del objeto.
     * 
     * @param jugador El {@link Jugador} que activa o se beneficia del objeto.
     */
    public abstract void usar(Jugador jugador);

    /**
     * Obtiene el nombre del objeto.
     * 
     * @return Cadena con el nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el tipo o categoría del objeto.
     * 
     * @return Cadena con el tipo.
     */
    public String getTipo() {
        return tipo;
    }
}
