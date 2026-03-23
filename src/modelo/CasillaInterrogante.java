package modelo;

import java.util.ArrayList;
import java.util.Random;

/**
 * Casilla interrogante: genera un evento aleatorio que otorga objetos al jugador.
 */
public class CasillaInterrogante extends Casilla {

    private ArrayList<Evento> eventosOcurridos = new ArrayList<>();
    private Random random = new Random();

    public CasillaInterrogante(int posicion, String tipo) {
        super(posicion, tipo);
    }

    @Override
    public void activarEfecto(Jugador jugador, controlador.Juego juego) {
        Evento evento = generarEventoAleatorio();
        evento.aplicarEfecto(jugador);
        eventosOcurridos.add(evento);
        String item = evento.getTipoEvento();
        String msg = "¡INTERROGANTE! " + jugador.getNombre() + " consigue un(a) " + item + "!";
        if (juego != null) juego.setLogMessage(msg);
        System.out.println(msg);
    }

    private Evento generarEventoAleatorio() {
        double probabilidad = random.nextDouble();

        if (probabilidad < 0.10) {
            return new Evento("DadoRapido");
        } else if (probabilidad < 0.40) {
            return new Evento("Pez");
        } else if (probabilidad < 0.70) {
            return new Evento("BolaNieve");
        } else {
            return new Evento("DadoLento");
        }
    }
}
