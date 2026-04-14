package modelo;

/**
 * Casilla rompedizas: penaliza al jugador según la cantidad de objetos.
 * - 0 objetos: sin penalización
 * - 1-5 objetos: pierde un turno
 * - Más de 5 objetos: vuelve al principio
 */
public class CasillaRompedizas extends Casilla {

    public CasillaRompedizas(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public String activarEfecto(Jugador jugador) {
        return calcularPenalizacion(jugador);
    }

    private String calcularPenalizacion(Jugador jugador) {
        int total = jugador.getInventario().getCantidad("Total");

        if (total == 0) {
            return jugador.getNombre() + " pasa sin penalización por las casillas rompedizas.";
        } else if (total <= 5) {
            jugador.setTurnosBloqueados(jugador.getTurnosBloqueados() + 1);
            return "El hielo cruje por el peso... " + jugador.getNombre() + " pierde un turno equilibrándose.";
        } else {
            jugador.setPosicion(0);
            return "¡CRACK! Demasiado peso. El hielo se rompe y " + jugador.getNombre() + " se cae al agua volviendo al inicio.";
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/CasillaRompediza.png";
    }
}
