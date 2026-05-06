package modelo;

/**
 * Objeto de combate utilizado en los enfrentamientos entre jugadores.
 * 
 * <p>
 * Posee una potencia fija que determina el impacto en la resolución de
 * una {@link controlador.Guerra} de bolas de nieve.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class BolaNieve extends Objeto {

    /** Valor de fuerza de la bola de nieve para los cálculos de guerra. */
    private int potencia = 3;

    /**
     * Constructor para la Bola de Nieve.
     * 
     * @param nombre Nombre descriptivo.
     * @param tipo   Categoría del objeto.
     */
    public BolaNieve(String nombre, String tipo) {
        super(nombre, tipo);
    }

    /**
     * Obtiene el valor de potencia de este proyectil.
     * 
     * @return Entero con la potencia.
     */
    public int getPotencia() {
        return potencia;
    }

    @Override
    public void usar(Jugador jugadorQueLaUsa) {
        System.out.println(jugadorQueLaUsa.getNombre() + " se prepara para lanzar una bola.");
    }
}
