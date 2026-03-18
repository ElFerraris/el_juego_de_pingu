package modelo;

/**
 * Objeto BolaNieve: se usa en guerras para hacer retroceder al oponente.
 */
public class BolaNieve extends Objeto {

    private int potencia = 3;

    public BolaNieve(String nombre, String tipo) {
        super(nombre, tipo);
    }

    public int getPotencia() {
        return potencia;
    }

    @Override
    public void usar(Jugador jugadorQueLaUsa) {
        System.out.println(jugadorQueLaUsa.getNombre() + " se prepara para lanzar una bola.");
    }
}
