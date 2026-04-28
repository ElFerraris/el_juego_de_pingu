package modelo;

/**
 * Representa a un pingüino, la entidad controlada por un jugador humano.
 * 
 * <p>
 * Los pingüinos son los protagonistas del juego que compiten por llegar a la
 * meta.
 * A diferencia de la {@link Foca}, sus acciones son decididas íntegramente por
 * el usuario
 * a través de la interfaz de usuario.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Pinguino extends Jugador {

    /**
     * Constructor para el Pingüino.
     * 
     * @param id     Identificador único.
     * @param nombre Nombre del jugador.
     * @param color  Color elegido para la representación visual.
     */
    public Pinguino(int id, String nombre, String color) {
        super(id, nombre, color);
    }
}
