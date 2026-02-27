package el_juego_de_pingu;

public class Pez extends Objeto{
	
	
	
	
	
	public Pez(String nombre, String tipo) {
		super(nombre, tipo);
		// TODO Auto-generated constructor stub
	}




	public boolean sobornar() {
System.out.println("Has usado un pez para sobornar al animal.");
        return true;
	}
	

	@Override
	public void usar(Jugador jugador) {
		sobornar();
	}
	
}
