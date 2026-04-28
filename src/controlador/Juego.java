package controlador;

import java.util.ArrayList;
import modelo.*;
import datos.BBDD;

/**
 * Motor principal de la lógica del juego "El Juego de Pingu".
 * 
 * <p>
 * En la versión JavaFX, esta clase actúa principalmente como un contenedor de
 * estado (DTO) para la partida actual, facilitando la comunicación con la
 * base de datos Oracle y manteniendo la integridad de los datos de los
 * jugadores y el tablero.
 * </p>
 * 
 * @author BadLabs©️
 * @version 2.0
 */
public class Juego {

    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnoActual;
    private boolean partidaFinalizada;
    private Jugador ganador;
    private String nombrePartida;
    private BBDD baseDatos;

    /**
     * Constructor por defecto de la clase Juego.
     * Inicializa un tablero nuevo, la lista de jugadores vacía y el gestor de BBDD.
     */
    public Juego() {
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<>();
        this.partidaFinalizada = false;
        this.baseDatos = new BBDD();
    }

    // ==================== GESTIÓN DE JUGADORES ====================

    /**
     * Añade un nuevo jugador a la lista de participantes.
     * 
     * @param jugador El pingüino o foca a añadir.
     */
    public void agregarJugador(Jugador jugador) {
        if (jugador != null) {
            this.jugadores.add(jugador);
        }
    }

    // ==================== GETTERS Y SETTERS ====================

    /**
     * Obtiene la lista completa de jugadores de la partida.
     * 
     * @return ArrayList con todos los jugadores.
     */
    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    /**
     * Obtiene un jugador específico según su posición en la lista.
     * 
     * @param indice El índice del jugador deseado.
     * @return El objeto {@link Jugador} correspondiente.
     */
    public Jugador getJugador(int indice) {
        if (indice >= 0 && indice < jugadores.size()) {
            return jugadores.get(indice);
        }
        return null;
    }

    /**
     * Devuelve el número total de participantes.
     * 
     * @return Cantidad de jugadores.
     */
    public int getNumeroJugadores() {
        return jugadores.size();
    }

    /**
     * Obtiene el índice del jugador que tiene el turno actual.
     * 
     * @return Índice del turno activo.
     */
    public int getTurnoActual() {
        return turnoActual;
    }

    /**
     * Establece el turno actual manualmente (útil para cargas de partida).
     * 
     * @param turnoActual El nuevo índice de turno.
     */
    public void setTurnoActual(int turnoActual) {
        this.turnoActual = turnoActual;
    }

    /**
     * Obtiene el tablero asociado a esta partida.
     * 
     * @return El objeto {@link Tablero}.
     */
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Asigna un tablero a la partida.
     * 
     * @param tablero El nuevo tablero.
     */
    public void setTablero(Tablero tablero) {
        this.tablero = tablero;
    }

    /**
     * Indica si la partida ha llegado a su fin.
     * 
     * @return {@code true} si hay un ganador, {@code false} en caso contrario.
     */
    public boolean isPartidaFinalizada() {
        return partidaFinalizada;
    }

    /**
     * Marca el estado de finalización de la partida.
     * 
     * @param partidaFinalizada {@code true} para finalizar.
     */
    public void setPartidaFinalizada(boolean partidaFinalizada) {
        this.partidaFinalizada = partidaFinalizada;
    }

    /**
     * Obtiene el jugador que ha ganado la partida.
     * 
     * @return El {@link Jugador} ganador o {@code null} si aún no hay.
     */
    public Jugador getGanador() {
        return ganador;
    }

    /**
     * Establece el ganador de la partida.
     * 
     * @param ganador El jugador que ha llegado a la meta.
     */
    public void setGanador(Jugador ganador) {
        this.ganador = ganador;
    }

    /**
     * Obtiene el gestor de acceso a la base de datos.
     * 
     * @return La instancia de {@link BBDD}.
     */
    public BBDD getBaseDatos() {
        return baseDatos;
    }

    /**
     * Obtiene el nombre identificativo de la partida.
     * 
     * @return Nombre de la partida.
     */
    public String getNombrePartida() {
        return nombrePartida;
    }

    /**
     * Establece un nombre para la partida.
     * 
     * @param nombrePartida El nuevo nombre.
     */
    public void setNombrePartida(String nombrePartida) {
        this.nombrePartida = nombrePartida;
    }
}
