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
    public void activarEfecto(Jugador jugador) {
        calcularPenalizacion(jugador);
    }

    private void calcularPenalizacion(Jugador jugador) {
        int total = jugador.getInventario().getCantidad("Total");

        if (total == 0) {
            System.out.println(jugador.getNombre() + " pasa sin penalización.");
        } else if (total <= 5) {
            jugador.setTurnosBloqueados(jugador.getTurnosBloqueados() + 1);
            System.out.println(jugador.getNombre() + " pierde un turno.");
        } else {
            jugador.setPosicion(0);
            System.out.println(jugador.getNombre() + " ha caído y se va al principio.");
        }
    }
}
