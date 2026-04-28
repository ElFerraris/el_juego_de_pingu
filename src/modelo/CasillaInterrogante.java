package modelo;

import java.util.ArrayList;
import java.util.Random;

/**
 * Casilla de evento aleatorio (Caja de suministros).
 * 
 * <p>
 * Al aterrizar en esta casilla, se selecciona un {@link Evento} al azar
 * basado en una tabla de probabilidades. Los eventos suelen otorgar objetos
 * útiles para el jugador (bolas de nieve, peces o dados especiales).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CasillaInterrogante extends Casilla {

    private ArrayList<Evento> eventosOcurridos = new ArrayList<>();
    private Random random = new Random();
    private Tablero tablero;

    /**
     * Constructor de la casilla interrogante.
     * 
     * @param posicion Índice en el tablero.
     * @param tipo     Identificador de tipo.
     * @param tablero  Referencia al tablero para el cálculo de eventos.
     */
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
            return new Evento("MotoNeu"); // 10%
        } else if (probabilidad < 0.20) {
            return new Evento("DadoRapido"); // 10% (0.10 a 0.20)
        } else if (probabilidad < 0.50) {
            return new Evento("Pez"); // 30% (0.20 a 0.50)
        } else if (probabilidad < 0.80) {
            return new Evento("BolaNieve"); // 30% (0.50 a 0.80)
        } else {
            return new Evento("DadoLento"); // 20% (0.80 a 1.00)
        }
    }

    @Override
    public String getSpritePath() {
        return "/assets/tablero/casillas/PilarInterrogante.png";
    }
}
