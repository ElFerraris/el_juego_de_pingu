package modelo;

import java.util.Arrays;
import java.util.List;

/**
 * Representa un jugador del juego (base para pingüinos y la foca).
 */
public abstract class Jugador {

    private int id;
    private String nombre;
    private int posicion;
    private Inventario inventario;
    private int turnosBloqueados;
    private String color;

    public Jugador(int id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.posicion = 0;
        this.inventario = new Inventario();
        this.turnosBloqueados = 0;
        this.color = color;
    }

    public void setId(int id) {
		this.id = id;
	}

	public int getId() {
        return id;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    /**
     * Tira el dado normal (1-6) y mueve la ficha.
     * @return el número obtenido.
     */
    public int tirarDado() {
        int numero = (int) (Math.random() * 6) + 1;
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

    /**
     * Mueve la ficha del jugador hacia adelante.
     * No puede superar la casilla final del tablero.
     */
    public void moverFicha(int numeroDeCasillas) {
        int posicionSiguiente = this.posicion + numeroDeCasillas;
        if (posicionSiguiente < Tablero.TAMANYO_TABLERO) {
            this.posicion = posicionSiguiente;
        } else {
            this.posicion = Tablero.TAMANYO_TABLERO;
        }
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }

    /**
     * Retrocede un número de casillas. No baja de 0.
     */
    public void retroceder(int casillas) {
        this.posicion -= casillas;
        if (this.posicion < 0) this.posicion = 0;
        System.out.println(this.nombre + " ha retrocedido a la casilla " + this.posicion);
    }

    /**
     * Intenta sobornar al oso con un pez.
     * @return true si tenía pez y lo usó, false si no tenía.
     */
    public boolean sobornarOso() {
        if (this.inventario.tieneObjeto("Pez")) {
            this.inventario.eliminarObjeto("Pez");
            return true;
        }
        return false;
    }

    /**
     * Comprueba si el jugador está bloqueado (no puede jugar este turno).
     */
    public boolean estaBloqueado() {
        return turnosBloqueados > 0;
    }

    /**
     * Valida el color elegido. Si no es válido, asigna Rojo por defecto.
     */
    /*private String validarColor(String colorRecibido) {
        List<String> coloresValidos = Arrays.asList(
            "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"
        );

        for (String c : coloresValidos) {
            if (c.equalsIgnoreCase(colorRecibido)) {
                return c;
            }
        }

        System.out.println("Color no válido. Asignando Rojo por defecto.");
        return "Rojo";
    }*/
    
    
    
}
