package el_juego_de_pingu;

public class DadoRapido extends Dado{

	
	public int tirar() {

	int numero = (int)(Math.random() * (10 - 5 + 1)) + 5;
	
	return numero;

	}
	

	@Override
	public void usar(Jugador jugador) {

		this.tirar();
		
		
	}
	
}
