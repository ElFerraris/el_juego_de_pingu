package modelo;

import java.util.List;

/**
 * Objeto para transportar y mostrar los datos de una partida guardada en la interfaz.
 */
public class PartidaGuardada {
    private int idPartida;
    private String seed;
    private String horaPartida;
    private String nombrePartida;
    private List<String> coloresJugadores;

    public PartidaGuardada(int idPartida, String seed, String horaPartida, String nombrePartida, List<String> coloresJugadores) {
        this.idPartida = idPartida;
        this.seed = seed;
        this.horaPartida = horaPartida;
        this.nombrePartida = nombrePartida;
        this.coloresJugadores = coloresJugadores;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public String getSeed() {
        return seed;
    }

    public String getHoraPartida() {
        return horaPartida;
    }

    public String getNombrePartida() {
        return nombrePartida;
    }

    public List<String> getColoresJugadores() {
        return coloresJugadores;
    }

    @Override
    public String toString() {
        return "Partida #" + idPartida + "   [Modo: " + (seed.matches("[a-zA-Z]+") ? seed : "Aleatoria")  + "]   »   " + horaPartida;
    }
}
