package modelo;
import java.util.ArrayList;
import java.util.Random;

/**
 * Casilla interrogante: genera un evento aleatorio que otorga objetos al jugador.
 */
public class CasillaInterrogante extends Casilla {

    private ArrayList<Evento> eventosOcurridos = new ArrayList<>();
    private Random random = new Random();
    private Tablero tablero;

    public CasillaInterrogante(int posicion, String tipo, Tablero tablero) {
        super(posicion, tipo);
        this.tablero = tablero;
    }

    public String activarEfecto(Jugador jugador) {
        Evento evento = generarEventoAleatorio();
        String result = evento.aplicarEfecto(jugador, tablero);
        eventosOcurridos.add(evento);
        return result;
    }

    private Evento generarEventoAleatorio() {
        double probabilidad = random.nextDouble();

        if (probabilidad < 0.10) {
            return new Evento("MotoNeu");      // 10%
        } else if (probabilidad < 0.20) {
            return new Evento("DadoRapido");   // 10% (0.10 a 0.20)
        } else if (probabilidad < 0.50) {
            return new Evento("Pez");          // 30% (0.20 a 0.50)
        } else if (probabilidad < 0.80) {
            return new Evento("BolaNieve");    // 30% (0.50 a 0.80)
        } else {
            return new Evento("DadoLento");    // 20% (0.80 a 1.00)
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/CasillaInterrogante.png";
    }
}
