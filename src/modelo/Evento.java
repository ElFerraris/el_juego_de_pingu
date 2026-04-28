package modelo;

import modelo.CasillaTrineo;
import java.util.Random;

/**
 * Representa un suceso fortuito activado por las casillas de suministro.
 * 
 * <p>
 * Los eventos encapsulan recompensas directas (objetos) o beneficios tácticos
 * (transporte en moto de nieve) que alteran el estado del jugador o su posición
 * en el tablero.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Evento {

    /** Identificador del tipo de suceso o recompensa. */
    private String tipoEvento;

    /**
     * Constructor para un Evento.
     * 
     * @param tipoEvento Cadena que identifica la naturaleza del evento.
     */
    public Evento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    /**
     * Ejecuta las consecuencias del evento sobre el jugador y el tablero.
     * 
     * <p>
     * Gestiona la lógica de entrega de suministros o el desplazamiento especial
     * mediante la Moto de Nieve, buscando la interacción más coherente según el
     * estado del tablero.
     * </p>
     * 
     * @param jugador El {@link Jugador} que recibe los efectos.
     * @param tablero El {@link Tablero} actual para cálculos de posición (ej. Moto
     *                de Nieve).
     * @return Un mensaje descriptivo y amigable para el usuario sobre el resultado
     *         del evento.
     */
    public String aplicarEfecto(Jugador jugador, Tablero tablero) {
        switch (tipoEvento) {
            case "Pez":
                jugador.getInventario().agregarObjeto("Pez");
                return "¡Encontraste un Sabroso Pez! \uD83D\uDC1F";
            case "BolaNieve":
                Random r = new Random();
                int cantidad = r.nextInt(3) + 1;
                for (int i = 0; i < cantidad; i++)
                    jugador.getInventario().agregarObjeto("BolaNieve");
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

    /**
     * Obtiene el tipo de evento registrado.
     * 
     * @return Cadena con el tipo.
     */
    public String getTipoEvento() {
        return tipoEvento;
    }
}
