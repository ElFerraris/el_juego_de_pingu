package el_juego_de_pingu;

public abstract class Objeto{
	
	protected String nombre;
	protected String tipo;
	
	
	public Objeto(String nombre, String tipo) {
		this.nombre = nombre;
		this.tipo = tipo;
	}


	/**
     * Método abstracto para que cada hijo (Pez, BolaNieve, Dado) 
     * implemente su propia lógica de uso.
     */
    public abstract void usar(Jugador jugador);
    
    // Getters básicos
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    
	
}
