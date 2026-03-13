
package el_juego_de_pingu;
import java.util.ArrayList;
import java.util.Random;

public class CasillaInterrogante extends Casilla {
	
	ArrayList<Evento> eventosOcurridos = new ArrayList<Evento>();
    private Random random = new Random();

    
	public CasillaInterrogante(int posicion, String tipo) {
		super(posicion, tipo);
	}
	
    
    @Override
    public void activarEfecto(Jugador jugador) {
    	Evento evento = generarEventoAleatorio();
    	evento.aplicarEfecto(jugador);
    	eventosOcurridos.add(evento);
    }
    
    
    
    private Evento generarEventoAleatorio() {
    	double probabilidad = random.nextDouble(); //Valor entre 0.0 y 1.0

        if (probabilidad < 0.10) { //10% de probabilidad
            return new Evento("DadoRapido");
        } else if (probabilidad < 0.40) { //30% de probabilidad
            return new Evento("Pez");
        } else if (probabilidad < 0.70) { //30% de probabilidad
            return new Evento("BolaNieve");
        } else { //30% de probabilidad
            return new Evento("DadoLento");
        }
    }
    
    
}