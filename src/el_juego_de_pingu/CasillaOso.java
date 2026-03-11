package el_juego_de_pingu;

public class CasillaOso extends Casilla{

	
	
	public CasillaOso(int posicion, String tipo) {
		super(posicion, tipo);
	}
	
	
	@Override
	public void activarEfecto(Jugador jugador) {
		if(jugador.sobornarOso()) {
			System.out.println(jugador.getNombre() + " ha sobornado al oso!");
		} else {
			returnInicio(jugador);
			System.out.println(jugador.getNombre() + " ha vuelto al inicio");
		}
	}
	
	private void returnInicio(Jugador jugador) {
		jugador.setPosicion(0);
	}
	
	
}