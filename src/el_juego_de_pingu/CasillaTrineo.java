package el_juego_de_pingu;

public class CasillaTrineo extends Casilla {

	
	private int posicionSiguienteTrineo;
	
	
	public CasillaTrineo(int posicion, String tipo, int posicionSiguienteTrineo) {
		super(posicion, tipo);
		this.posicionSiguienteTrineo = posicionSiguienteTrineo;
	}
	
	
	
	
	
	public int getPosicionSiguienteTrineo() {
		return posicionSiguienteTrineo;
	}





	@Override
	public void activarEfecto(Jugador jugador) {
		
	}
	
	private void avanzarSiguienteTrineo(Jugador jugador) {
		
	}
	
	
}
