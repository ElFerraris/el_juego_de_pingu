    package modelo;

/**
 * Objeto Pez: se usa para sobornar al oso o distraer a la foca.
 */
public class Pez extends Objeto {

    public Pez(String nombre, String tipo) {
        super(nombre, tipo);
    }

    public boolean sobornar() {
        System.out.println("Has usado un pez para sobornar al animal.");
        return true;
    }

    @Override
    public void usar(Jugador jugador) {
        sobornar();
    }
}
