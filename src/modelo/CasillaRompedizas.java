package modelo;

import java.util.Random;

/**
 * Casilla de suelo inestable que penaliza el exceso de carga en el inventario.
 * 
 * <p>
 * El efecto varía según la cantidad total de objetos acumulados:
 * <ul>
 * <li>0 objetos: Tránsito seguro.</li>
 * <li>1-5 objetos: El jugador pierde un turno equilibrándose.</li>
 * <li>Más de 5 objetos: El hielo se rompe y el jugador regresa al inicio.</li>
 * </ul>
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaRompedizas extends Casilla {

    /**
     * Constructor para la Casilla Rompediza.
     * 
     * @param posicion Índice en el tablero.
     * @param tipo     Identificador de tipo.
     */
    public CasillaRompedizas(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        String mensajeBase = calcularPenalizacion(jugador);

        // Si total == 0, pasamos sin penalización y no hay evento adicional
        if (jugador.getInventario().getCantidad("Total") == 0) {
            return mensajeBase;
        }

        // Evento adicional: Perder turno o perder objeto
        Random rand = new Random();
        Evento adicional;
        if (rand.nextBoolean()) {
            adicional = new Evento("PerderTurno");
        } else {
            adicional = new Evento("PerderObjeto");
        }

        // El tablero no es necesario para estos dos eventos específicos, pasamos null
        String mensajeAdicional = adicional.aplicarEfecto(jugador, null);

        return mensajeBase + " | " + mensajeAdicional;
    }

    private String calcularPenalizacion(Jugador jugador) {
        int total = jugador.getInventario().getCantidad("Total");

        if (total == 0) {
            return "PASO SEGURO";
        } else if (total <= 5) {
            jugador.setTurnosBloqueados(jugador.getTurnosBloqueados() + 1);
            return "PIERDE 1 TURNO POR PESO";
        } else {
            jugador.setPosicion(0);
            return "HIELO ROTO: AL INICIO";
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarRompediza.png";
    }
}
