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
                if (jugador.getInventario().agregarObjeto("Pez")) {
                    return "+1 PEZ";
                } else {
                    return "PEZ: MOCHILA LLENA";
                }
            case "BolaNieve":
                Random r = new Random();
                int cantidadDeseada = r.nextInt(3) + 1;
                int agregados = jugador.getInventario().agregarObjetos("BolaNieve", cantidadDeseada);
                if (agregados > 0) {
                    return "+" + agregados + " BOLAS DE NIEVE";
                } else {
                    return "BOLAS: MOCHILA LLENA";
                }
            case "DadoRapido":
                if (jugador.getInventario().agregarObjeto("DadoRapido")) {
                    return "+1 DADO RAPIDO";
                } else {
                    return "DADO: MOCHILA LLENA";
                }
            case "DadoLento":
                if (jugador.getInventario().agregarObjeto("DadoLento")) {
                    return "+1 DADO LENTO";
                } else {
                    return "DADO: MOCHILA LLENA";
                }
            case "MotoNeu":
                for (int i = jugador.getPosicion() + 1; i < Tablero.TAMANYO_TABLERO; i++) {
                    Casilla c = tablero.getCasilla(i);
                    if (c instanceof CasillaTrineo) {
                        jugador.setPosicion(i);
                        return "MOTO DE NIEVE: CASILLA " + i;
                    }
                }
                return "MOTO DE NIEVE: SIN DESTINO";
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
