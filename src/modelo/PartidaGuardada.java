package modelo;

/**
 * Objeto para transportar y mostrar los datos de una partida guardada en la interfaz.
 */
public class PartidaGuardada {
    private int idPartida;
    private String seed;
    private String horaPartida;

    public PartidaGuardada(int idPartida, String seed, String horaPartida) {
        this.idPartida = idPartida;
        this.seed = seed;
        this.horaPartida = horaPartida;
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

    @Override
    public String toString() {
        return "Partida #" + idPartida + "   [Modo: " + (seed.matches("[a-zA-Z]+") ? seed : "Aleatoria")  + "]   »   " + horaPartida;
    }
}
