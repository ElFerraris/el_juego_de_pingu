package modelo;

import java.util.List;

/**
 * Data Transfer Object (DTO) que representa el resumen de una partida
 * persistida.
 * 
 * <p>
 * Se utiliza para encapsular la información esencial de una partida guardada
 * (ID, semilla, fecha, nombres y colores) para su visualización en los menús
 * de carga de partida.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class PartidaGuardada {
    /** Identificador único en la base de datos. */
    private int idPartida;
    /** Semilla de generación del tablero. */
    private String seed;
    /** Marca de tiempo de la última actualización de la partida. */
    private String horaPartida;
    /** Nombre personalizado o identificativo de la partida. */
    private String nombrePartida;
    /** Lista de colores asociados a los jugadores para previsualización. */
    private List<String> coloresJugadores;

    /**
     * Constructor para un objeto de Partida Guardada.
     * 
     * @param idPartida        ID único.
     * @param seed             Semilla del tablero.
     * @param horaPartida      Fecha y hora formateada.
     * @param nombrePartida    Nombre del archivo o sesión.
     * @param coloresJugadores Lista de colores de los participantes.
     */
    public PartidaGuardada(int idPartida, String seed, String horaPartida, String nombrePartida,
            List<String> coloresJugadores) {
        this.idPartida = idPartida;
        this.seed = seed;
        this.horaPartida = horaPartida;
        this.nombrePartida = nombrePartida;
        this.coloresJugadores = coloresJugadores;
    }

    /**
     * Obtiene el identificador de la partida.
     * 
     * @return El ID entero.
     */
    public int getIdPartida() {
        return idPartida;
    }

    /**
     * Obtiene la semilla del tablero.
     * 
     * @return Cadena de la semilla.
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Obtiene la hora de la partida.
     * 
     * @return Cadena con la fecha y hora.
     */
    public String getHoraPartida() {
        return horaPartida;
    }

    /**
     * Obtiene el nombre asignado a la partida.
     * 
     * @return Cadena con el nombre.
     */
    public String getNombrePartida() {
        return nombrePartida;
    }

    /**
     * Obtiene la lista de colores de los jugadores.
     * 
     * @return Lista de cadenas de colores.
     */
    public List<String> getColoresJugadores() {
        return coloresJugadores;
    }

    /**
     * Representación textual amigable para los componentes de lista en la UI.
     * 
     * @return Cadena formateada con la información básica.
     */
    @Override
    public String toString() {
        return "Partida #" + idPartida + "   [Modo: " + (seed.matches("[a-zA-Z]+") ? seed : "Aleatoria") + "]   »   "
                + horaPartida;
    }
}
