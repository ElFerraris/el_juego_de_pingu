package el_juego_de_pingu;

public class CPU extends Jugador{

	private String dificultad;
	
	public CPU(String id, String nombre, String dificultad) {
        super(id, nombre); // Llama al constructor de Jugador
        this.dificultad = dificultad;
    }
	
	public void decidirAccion() {
	    System.out.println(this.getNombre() + " está decidiendo qué dado usar...");

	    // 1. Intentar usar el Dado Rápido primero
	    if (getInventario().tieneObjetoEspecifico("Rapido")) { 
	        System.out.println("La foca decide usar un Dado Rápido para avanzar mucho.");
	        getInventario().usarDadoEspecifico("Rapido", this);
	    } 
	    // 2. Si no tiene rápido, intentar usar el Dado Lento
	    else if (getInventario().tieneObjetoEspecifico("Lento")) {
	        System.out.println("La foca no tiene dados rápidos, usa un Dado Lento.");
	        getInventario().usarDadoEspecifico("Lento", this);
	    } 
	    // 3. Si no tiene dados especiales, usa el normal de 1-6
	    else {
	        int resultado = this.tirarDado();
	        System.out.println("La foca lanza el dado normal y saca un " + resultado);
	    }
	}

	public void atacarJugador(Jugador jugador) {

	}

	public void moverFichaIA() {

	}

	public void seleccionarObjetivo() {

	}
	
}