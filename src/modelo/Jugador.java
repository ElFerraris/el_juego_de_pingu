package modelo;

import java.util.Arrays;
import java.util.List;

/**
 * Representa la entidad base de un Jugador dentro del sistema.
 * 
 * <p>
 * Define los atributos y comportamientos comunes para cualquier participante de
 * la partida,
 * ya sea un {@link Pinguino} (humano) o una {@link Foca} (IA). Gestiona el
 * estado
 * de posición, inventario, bloqueos de turno y personalización visual (nombre y
 * color).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public abstract class Jugador {

    /** Identificador único del jugador en la base de datos. */
    private int id;
    /** Nombre legible del jugador. */
    private String nombre;
    /**
     * Índice de la casilla actual en el tablero (0 a
     * {@link Tablero#TAMANYO_TABLERO}).
     */
    private int posicion;
    /** Sistema de almacenamiento de objetos del jugador. */
    private Inventario inventario;
    /** Contador de turnos que el jugador debe esperar sin jugar. */
    private int turnosBloqueados;
    /** Referencia de color para la personalización estética. */
    private String color;

    /**
     * Constructor principal para un Jugador.
     * 
     * @param id     Identificador único.
     * @param nombre Nombre del jugador.
     * @param color  Color representativo.
     */
    public Jugador(int id, String nombre, String color) {
        this.id = id;
        this.nombre = nombre;
        this.posicion = 0;
        this.inventario = new Inventario();
        this.turnosBloqueados = 0;
        this.color = color;
    }

    /**
     * Actualiza el ID del jugador.
     * 
     * @param id Nuevo identificador.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el ID único del jugador.
     * 
     * @return El identificador entero.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene la posición actual en el tablero.
     * 
     * @return Índice de la casilla.
     */
    public int getPosicion() {
        return posicion;
    }

    /**
     * Establece manualmente la posición del jugador.
     * 
     * @param posicion Nuevo índice de casilla.
     */
    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    /**
     * Realiza una tirada de dado estándar (1-6) y desplaza la ficha.
     * 
     * @return El valor obtenido en la tirada.
     */
    public int tirarDado() {
        int numero = (int) (Math.random() * 6) + 1;
        this.moverFicha(numero);
        return numero;
    }

    /**
     * Obtiene el número de turnos que el jugador permanecerá bloqueado.
     * 
     * @return Cantidad de turnos de penalización.
     */
    public int getTurnosBloqueados() {
        return turnosBloqueados;
    }

    /**
     * Establece una penalización de turnos.
     * 
     * @param turnosBloqueados Número de turnos a bloquear.
     */
    public void setTurnosBloqueados(int turnosBloqueados) {
        this.turnosBloqueados = turnosBloqueados;
    }

    /**
     * Proporciona acceso al inventario del jugador.
     * 
     * @return La instancia de {@link Inventario}.
     */
    public Inventario getInventario() {
        return inventario;
    }

    /**
     * Desplaza la ficha un número determinado de casillas hacia adelante.
     * 
     * <p>
     * Controla que el jugador no sobrepase el límite máximo del tablero,
     * quedando en la casilla final en caso de excederse.
     * </p>
     * 
     * @param numeroDeCasillas Cantidad de pasos a avanzar.
     */
    public void moverFicha(int numeroDeCasillas) {
        int posicionSiguiente = this.posicion + numeroDeCasillas;
        if (posicionSiguiente < Tablero.TAMANYO_TABLERO) {
            this.posicion = posicionSiguiente;
        } else {
            this.posicion = Tablero.TAMANYO_TABLERO - 1;
        }
    }

    /**
     * Obtiene el nombre del jugador.
     * 
     * @return Cadena con el nombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene el color del jugador.
     * 
     * @return Cadena con el nombre o código de color.
     */
    public String getColor() {
        return color;
    }

    /**
     * Desplaza la ficha hacia atrás una cantidad de casillas.
     * 
     * <p>
     * Asegura que el jugador no retroceda más allá de la casilla inicial (0).
     * </p>
     * 
     * @param casillas Cantidad de pasos a retroceder.
     */
    public void retroceder(int casillas) {
        this.posicion -= casillas;
        if (this.posicion < 0)
            this.posicion = 0;
        System.out.println(this.nombre + " ha retrocedido a la casilla " + this.posicion);
    }

    /**
     * Intenta evitar la penalización de un oso entregando un pez del inventario.
     * 
     * @return {@code true} si el soborno fue exitoso (tenía un pez); {@code false}
     *         en caso contrario.
     */
    public boolean sobornarOso() {
        if (this.inventario.tieneObjeto("Pez")) {
            this.inventario.eliminarObjeto("Pez");
            return true;
        }
        return false;
    }

    /**
     * Determina si el jugador tiene prohibido realizar acciones en el turno actual.
     * 
     * @return {@code true} si el contador de turnos bloqueados es mayor a cero.
     */
    public boolean estaBloqueado() {
        return turnosBloqueados > 0;
    }

    /**
     * Valida el color elegido. Si no es válido, asigna Rojo por defecto.
     */
    /*
     * private String validarColor(String colorRecibido) {
     * List<String> coloresValidos = Arrays.asList(
     * "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"
     * );
     * 
     * for (String c : coloresValidos) {
     * if (c.equalsIgnoreCase(colorRecibido)) {
     * return c;
     * }
     * }
     * 
     * System.out.println("Color no válido. Asignando Rojo por defecto.");
     * return "Rojo";
     * }
     */

}
