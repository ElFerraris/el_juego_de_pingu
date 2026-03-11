package el_juego_de_pingu;

public class CasillaRompedizas extends Casilla {

	
	public CasillaRompedizas(int posicion, String tipo) {
		super(posicion, tipo);
	}
	
	
	@Override
	public void activarEfecto(Jugador jugador) {
		calcularPenalizacion(jugador);
	}
	
	private void calcularPenalizacion(Jugador jugador) {
		if (jugador.getInventario().getCuantidad("Total") > 5) {
			jugador.setPosicion(0);
			System.out.println(jugador.getNombre() + " ha caido i se va al principio.");
		} else if (jugador.getInventario().getCuantidad("Total") <= 5) {
			jugador.setTurnosBloqueados(jugador.getTurnosBloqueados() + 1);
			System.out.println(jugador.getNombre() + " pierde un turno.");
		} else {
			System.out.println(jugador.getNombre() + " passa sense penalització");
		}
	}
	
	
	
}