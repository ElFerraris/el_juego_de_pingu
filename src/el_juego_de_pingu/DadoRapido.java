package el_juego_de_pingu;

public class DadoRapido extends Dado{



	public DadoRapido(String nombre, String tipo) {
        super(nombre, tipo, 5, 10); // Definimos aquí sus límites 
    }


/*
	public int tirar() {

	int numero = (int)(Math.random() * (10 - 5 + 1)) + 5;
	
	return numero;

	}
	

	@Override
	public void usar(Jugador jugador) {

		int numerodecassilas = this.tirar();
		
			jugador.moverFicha(numerodecassilas);
		
	}
	*/
	
}
