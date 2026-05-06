package modelo;

/**
 * Dado especial diseñado para cubrir grandes distancias rápidamente.
 * 
 * <p>
 * Su rango de valores oscila entre [5, 10], ideal para adelantar oponentes
 * o alejarse de zonas peligrosas.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class DadoRapido extends Dado {

    /**
     * Constructor del dado rápido.
     * 
     * @param nombre Nombre descriptivo.
     * @param tipo   Identificador de tipo.
     */
    public DadoRapido(String nombre, String tipo) {
        super(nombre, tipo, 5, 10);
    }
}
