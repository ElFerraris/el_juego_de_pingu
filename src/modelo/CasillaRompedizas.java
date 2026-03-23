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
    public void activarEfecto(Jugador jugador, controlador.Juego juego) {
        calcularPenalizacion(jugador, juego);
    }

    private void calcularPenalizacion(Jugador jugador, controlador.Juego juego) {
        int total = jugador.getInventario().getCantidad("Total");
        String msg;

        if (total == 0) {
            msg = jugador.getNombre() + " pasa sin problema por el hielo rompedizo.";
        } else if (total <= 5) {
            jugador.setTurnosBloqueados(jugador.getTurnosBloqueados() + 1);
            msg = "¡HIELO ROMPEDIZO! " + jugador.getNombre() + " pierde un turno.";
        } else {
            jugador.setPosicion(0);
            msg = "¡HIELO ROMPEDIZO! " + jugador.getNombre() + " cae al agua y vuelve al principio.";
        }
        if (juego != null) juego.setLogMessage(msg);
        System.out.println(msg);
    }
}
