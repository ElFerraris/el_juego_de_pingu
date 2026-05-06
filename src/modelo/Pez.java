package modelo;

/**
 * Objeto de recurso utilizado principalmente para interacciones pacíficas.
 * 
 * <p>
 * Su función principal es servir como moneda de cambio para sobornar
 * a los osos en las {@link CasillaOso}, evitando así penalizaciones severas.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Pez extends Objeto {

    /**
     * Constructor para el Pez.
     * 
     * @param nombre Nombre descriptivo.
     * @param tipo   Categoría del objeto.
     */
    public Pez(String nombre, String tipo) {
        super(nombre, tipo);
    }

    /**
     * Ejecuta la acción de entregar el pez a un animal.
     * 
     * @return {@code true} indicando que el soborno ha sido realizado.
     */
    public boolean sobornar() {
        System.out.println("Has usado un pez para sobornar al animal.");
        return true;
    }

    @Override
    public void usar(Jugador jugador) {
        sobornar();
    }
}
