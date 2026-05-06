package modelo;

/**
 * Dado especial diseñado para movimientos cortos y precisos.
 * 
 * <p>
 * Su rango de valores está limitado a [1, 3], permitiendo al jugador
 * aterrizar en casillas cercanas con mayor probabilidad.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class DadoLento extends Dado {

    /**
     * Constructor del dado lento.
     * 
     * @param nombre Nombre descriptivo.
     * @param tipo   Identificador de tipo.
     */
    public DadoLento(String nombre, String tipo) {
        super(nombre, tipo, 1, 3);
    }
}
