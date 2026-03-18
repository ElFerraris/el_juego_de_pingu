package modelo;

import java.util.Random;

/**
 * Representa un evento aleatorio que ocurre en una casilla interrogante.
 * Otorga objetos al jugador según el tipo de evento.
 */
public class Evento {

    private String tipoEvento;

    public Evento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    /**
     * Aplica el efecto del evento sobre el jugador.
     */
    public void aplicarEfecto(Jugador jugador) {
        System.out.println("Evento aleatorio: " + tipoEvento);

        switch (tipoEvento) {
            case "Pez":
                jugador.getInventario().agregarObjeto("Pez");
                break;
            case "BolaNieve":
                Random r = new Random();
                int cantidad = r.nextInt(3) + 1;
                for (int i = 0; i < cantidad; i++) { // FIX: era i <= cantidad (off-by-one)
                    jugador.getInventario().agregarObjeto("BolaNieve");
                }
                break;
            case "DadoRapido":
                jugador.getInventario().agregarObjeto("DadoRapido");
                break;
            case "DadoLento":
                jugador.getInventario().agregarObjeto("DadoLento");
                break;
        }
    }

    public String getTipoEvento() {
        return tipoEvento;
    }
}
