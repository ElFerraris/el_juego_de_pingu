package modelo;

/**
 * Clase abstracta que representa un dado especial.
 * Los dados tienen un rango [valorMin, valorMax] y se usan para avanzar casillas.
 */
public abstract class Dado extends Objeto {

    private int valorMin;
    private int valorMax;

    public Dado(String nombre, String tipo, int valorMin, int valorMax) {
        super(nombre, tipo);
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

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
