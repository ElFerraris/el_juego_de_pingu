package el_juego_de_pingu;

public class CasillaAgujero extends Casilla{

	
	private int posicionAgujeroAnterior;
	
	public CasillaAgujero(int posicion, String tipo, int posicionAgujeroAnterior) {
		super(posicion, tipo);
		this.posicionAgujeroAnterior = posicionAgujeroAnterior;
	}
	
	
	
	@Override
	public void activarEfecto(Jugador jugador) {
		
	}
	
	private void enviarAgujeroAnterior(Jugador jugador) {
		
	}
	
	
	
}
