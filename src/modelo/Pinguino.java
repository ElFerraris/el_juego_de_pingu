package modelo;

/**
 * Representa un jugador humano (un pingüino).
 * Extiende de Jugador para diferenciar su jugabilidad de la foca.
 */
public class Pinguino extends Jugador {

    public Pinguino(int id, String nombre, String color) {
        super(id, nombre, color);
    }
}
