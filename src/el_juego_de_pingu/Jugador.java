package el_juego_de_pingu;

import java.util.Arrays;
import java.util.List;

public class Jugador extends Juego{
	
	private String id;
    private String nombre;
    private int posicion;
    private Inventario inventario;
    private int turnosBloqueados;
    private String color;
    
    
    public Jugador(String id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.posicion = 0;
        this.inventario = new Inventario();
        this.turnosBloqueados = 0;
        
        this.color = validarColor(color);
    }


	public int getPosicion() {
		return posicion;
	}


	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
	
	
    
    
    public int tirarDado() {
    	
    		int numero = (int)(Math.random() * 6) + 1;
    		
    		this.moverFicha(numero);
    		
            return numero;
            
    	
    }
	

    public int getTurnosBloqueados() {
		return turnosBloqueados;
	}


	public void setTurnosBloqueados(int turnosBloqueados) {
		this.turnosBloqueados = turnosBloqueados;
	}


	public Inventario getInventario() {
		return inventario;
	}


	public void moverFicha(int numeroDeCasillasQueAvanza) {
    
    	int posicionsiguiente = this.posicion + numeroDeCasillasQueAvanza; 
    	if(posicionsiguiente < 50) {
    		this.posicion = posicionsiguiente;	
    		
    	}
    	else {
    		this.posicion = 50;
    	}
    }


	public String getNombre() {
		return nombre;
	}
	
	public void retroceder(int casillas) {
	    this.posicion -= casillas;
	    if (this.posicion < 0) this.posicion = 0; // Para no salir del tablero
	    System.out.println(this.nombre + " ha retrocedido a la casilla " + this.posicion);
	}


    
    
    public boolean sobornarOso() {
    
    	if(this.inventario.tieneObjeto("Pez")) {
    		this.inventario.eliminarObjeto("Pez");
    		return true;
    	}else {
    		return false;
    	}
    	
    	
    }
    
    
    public boolean estaBloqueado() {
    	if(turnosBloqueados == 0) {
    		return false;
    	}else {return true;}
    	
    }
    
    
    private String validarColor(String colorRecibido) {
        // Lista de colores permitidos (puedes ajustarlos a tu gusto)
        List<String> coloresValidos = Arrays.asList(
            "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"
        );

        // Validamos ignorando mayúsculas/minúsculas
        for (String c : coloresValidos) {
            if (c.equalsIgnoreCase(colorRecibido)) {
                return c; // Retorna el color con el formato correcto
            }
        }

        // Si el color no es válido, asignamos uno por defecto (ej. Gris)
        System.out.println("Color no válido. Asignando Rojo por defecto.");
        return "Rojo";
    }
    
    
    
    
    
}