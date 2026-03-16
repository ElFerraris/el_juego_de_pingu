package el_juego_de_pingu;

public class CPU extends Jugador{

	
	public CPU(String id, String nombre) {
        super(id, nombre, "Rojo"); // Llama al constructor de Jugador
    }
	
	public void decidirAccion() {
	    System.out.println(this.getNombre() + " está decidiendo qué dado usar...");

	    // 1. Intentar usar el Dado Rápido primero
	    if (getInventario().tieneObjetoEspecifico("Rapido")) { 
	        System.out.println("La foca decide usar un Dado Rápido para avanzar mucho.");
	        getInventario().usarDadoEspecifico("Rapido", this);
	    } 
	    // 2. Si no tiene dados especiales, usa el normal de 1-6
	    else {
	        int resultado = this.tirarDado();
	        System.out.println("La foca lanza el dado normal y saca un " + resultado);
	    }
	    //El Dado Lento no se utiliza por que va a ganar
	}

	public void atacarJugador(Jugador objetivo) {
	    System.out.println("¡LA FOCA " + this.getNombre() + " HA PASADO POR ENCIMA DE " + objetivo.getNombre() + "!");
	    System.out.println("¡Le ha robado la mitad de su inventario con un golpe de aleta!");
	    
	    // Llamamos al método que reduce el inventario
	    objetivo.getInventario().serAtracado();
	    
	    System.out.println("Inventario de " + objetivo.getNombre() + " reducido.");
	}

	public void moverFichaIA() {

	}

	public void seleccionarObjetivo() {

	}
	
	
	
}