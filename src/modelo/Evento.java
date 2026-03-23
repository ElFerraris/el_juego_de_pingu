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

    /**
     * Aplica el efecto del evento sobre el jugador.
     */
    public void aplicarEfecto(Jugador jugador, Tablero tablero) {
        System.out.println("Evento aleatorio: " + tipoEvento);

        switch (tipoEvento) {
            case "Pez":
                jugador.getInventario().agregarObjeto("Pez");
                break;
            case "BolaNieve":
                Random r = new Random();
                int cantidad = r.nextInt(3) + 1;
                for (int i = 0; i < cantidad; i++) { 
                    jugador.getInventario().agregarObjeto("BolaNieve");
                }
                break;
            case "DadoRapido":
                jugador.getInventario().agregarObjeto("DadoRapido");
                break;
            case "DadoLento":
                jugador.getInventario().agregarObjeto("DadoLento");
                break;
            case "MotoNeu":
            	// Lógica para ir al siguiente trineo
                boolean encontrado = false;
                // Empezamos a buscar desde la posición siguiente a la del jugador
                for (int i = jugador.getPosicion() + 1; i < Tablero.TAMANYO_TABLERO; i++) {
                    Casilla c = tablero.getCasilla(i);
                    if (c instanceof CasillaTrineo) {
                        System.out.println("¡La Moto de Nieve te lleva al trineo en la posición " + i + "!");
                        jugador.setPosicion(i);
                        encontrado = true;
                        break; 
                    }
                }
                if (!encontrado) {
                    System.out.println("No hay más trineos adelante, la moto se queda donde está.");
                }
                break;
        }
    }

    public String getTipoEvento() {
        return tipoEvento;
    }
}