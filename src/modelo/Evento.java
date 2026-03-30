package modelo;

import modelo.CasillaTrineo;
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

    public String aplicarEfecto(Jugador jugador, Tablero tablero) {
        switch (tipoEvento) {
            case "Pez":
                jugador.getInventario().agregarObjeto("Pez");
                return "¡Encontraste un Sabroso Pez! \uD83D\uDC1F";
            case "BolaNieve":
                Random r = new Random();
                int cantidad = r.nextInt(3) + 1;
                for (int i = 0; i < cantidad; i++) jugador.getInventario().agregarObjeto("BolaNieve");
                return "¡Has recogido " + cantidad + " Bolas de Nieve! \u2744\uFE0F";
            case "DadoRapido":
                jugador.getInventario().agregarObjeto("DadoRapido");
                return "¡Encontraste un Dado Rápido! \uD83C\uDFB2\u26A1";
            case "DadoLento":
                jugador.getInventario().agregarObjeto("DadoLento");
                return "¡Encontraste un Dado Lento! \uD83C\uDFB2\uD83D\uDC22";
            case "MotoNeu":
                for (int i = jugador.getPosicion() + 1; i < Tablero.TAMANYO_TABLERO; i++) {
                    Casilla c = tablero.getCasilla(i);
                    if (c instanceof CasillaTrineo) {
                        jugador.setPosicion(i);
                        return "¡BRRUUUM! Una Moto de Nieve te lleva hasta el Trineo en la casilla " + i + "!";
                    }
                }
                return "Una Moto de Nieve... pero no hay más trineos adelante.";
        }
        return "No hay objeto.";
    }

    public String getTipoEvento() {
        return tipoEvento;
    }
}
