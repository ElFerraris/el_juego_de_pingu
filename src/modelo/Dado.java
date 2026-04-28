package modelo;

/**
 * Representa la base para dados con comportamientos no estándar.
 * 
 * <p>
 * A diferencia del dado convencional (1-6), los dados especiales operan en un
 * rango de valores configurable. Al ser usados, calculan un desplazamiento
 * aleatorio y mueven al jugador directamente.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public abstract class Dado extends Objeto {

    /** Límite inferior del valor aleatorio (inclusive). */
    private int valorMin;
    /** Límite superior del valor aleatorio (inclusive). */
    private int valorMax;

    /**
     * Constructor para un Dado especial.
     * 
     * @param nombre   Nombre del objeto.
     * @param tipo     Categoría del objeto.
     * @param valorMin Valor mínimo posible.
     * @param valorMax Valor máximo posible.
     */
    public Dado(String nombre, String tipo, int valorMin, int valorMax) {
        super(nombre, tipo);
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

    /**
     * Calcula un valor aleatorio dentro del rango configurado [valorMin, valorMax].
     * 
     * @return El resultado de la tirada.
     */
    public int tirar() {
        return (int) (Math.random() * (valorMax - valorMin + 1)) + valorMin;
    }

    @Override
    public void usar(Jugador jugador) {
        int pasos = tirar();
        System.out.println("Has sacado un " + pasos + " con el " + getNombre());
        jugador.moverFicha(pasos);
    }
}
