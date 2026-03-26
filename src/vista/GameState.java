package vista;

import controlador.Juego;

/**
 * Singleton que mantiene el estado global del juego entre pantallas JavaFX.
 */
public class GameState {

    private static GameState instancia;

    private Juego juego;
    private String usuarioLogueado;

    private GameState() {
        this.juego = new Juego();
    }

    public static GameState getInstance() {
        if (instancia == null) {
            instancia = new GameState();
        }
        return instancia;
    }

    /** Resetea la partida (nueva instancia de Juego). */
    public void resetJuego() {
        this.juego = new Juego();
    }

    public Juego getJuego() {
        return juego;
    }

    public String getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public void setUsuarioLogueado(String nombre) {
        this.usuarioLogueado = nombre;
    }
}
