
package el_juego_de_pingu;
import java.util.ArrayList;

public class CasillaInterrogante extends Casilla {

    ArrayList<Evento> evento = new ArrayList<Evento>();

    
	public CasillaInterrogante(int posicion, String tipo) {
		super(posicion, tipo);
	}
	
    
    @Override
    public void activarEfecto(Jugador jugador) {
    	
    }
    /*
    private Evento generaEventoAleatorio() {
		
    }
    */
    
}
