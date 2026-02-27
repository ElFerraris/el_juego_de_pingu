package el_juego_de_pingu;

public class Jugador extends Juego{
	
	private String id;
    private String nombre;
    private int posicion;
    private Inventario inventario;
    private int turnosBloqueados;
    
    
	public int getPosicion() {
		return posicion;
	}


	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
	
	
    
    /*
    public int tirarDado() {

    }
	*/

    public void moverFicha(int numeroDeCasillasQueAvanza) {
    
    	int posicionsiguiente = this.posicion + numeroDeCasillasQueAvanza; 
    	if(posicionsiguiente < 50) {
    		this.posicion = posicionsiguiente;	
    		
    	}
    	else {
    		this.posicion = 50;
    	}
    }
	
	
    public void atacarConBolasNieve(Jugador jugador) {
    	
    }


	public String getNombre() {
		return nombre;
	}
	
	public void retroceder(int casillas) {
	    this.posicion -= casillas;
	    if (this.posicion < 0) this.posicion = 0; // Para no salir del tablero
	    System.out.println(this.nombre + " ha retrocedido a la casilla " + this.posicion);
	}


    
    /*
    public boolean sobornarOso() {
    
    }

    public boolean estaBloqueado() {
    
    }
    */
    
    
    
    
}
