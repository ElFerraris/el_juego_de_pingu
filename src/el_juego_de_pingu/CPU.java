package el_juego_de_pingu;

public class CPU extends Jugador{

	private String dificultad;
	
	public CPU(String id, String nombre, String dificultad) {
        super(id, nombre); // Llama al constructor de Jugador
        this.dificultad = dificultad;
    }
	
	public void decidirAccion() {

		if (getInventario().tieneObjeto("Dados")) {
            getInventario().usarObjeto("Dados", this);
        } else {
            this.tirarDado();
        }
		
	}

	public void atacarJugador(Jugador jugador) {

	}

	public void moverFichaIA() {

	}

	public void seleccionarObjetivo() {

	}
	
}