package el_juego_de_pingu;

public class DadoLento extends Dado{



	public DadoLento(String nombre, String tipo) {
		super(nombre, tipo, 1, 3);
	}
	
	/*
	public int tirar() {

    int numero = (int)(Math.random() * (3 + 1) + 1) ;
	
	return numero;

	}

	@Override
	public void usar(Jugador jugador) {

		int numerodecassilas = this.tirar(); ;
		
		jugador.moverFicha(numerodecassilas);
		
	}
	
	*/
}
