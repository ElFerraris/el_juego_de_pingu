package controlador;

import modelo.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 * GameContext (Patrón Singleton)
 * 
 * Este archivo es el "Cerebro Central" de la aplicación. 
 * Sirve para guardar datos mientras te mueves entre diferentes pantallas (escenas).
 * 
 * Al ser un Singleton, solo existe UNA instancia de esta clase en todo el programa.
 */
public class GameContext {
    private static GameContext instance;

    // Tipos de acción que pueden necesitar confirmación
    public enum ActionConfirmType { LOGOUT, QUIT }
    private ActionConfirmType actionToConfirm;

    // Usuario que ha hecho login actualmente
    private Jugador currentUser;
    
    // Lista de jugadores (pingüinos) configurados para la partida actual
    private List<Jugador> configuredPlayers = new ArrayList<>();
    
    // El "Seed" o código del tablero actual
    private String seed;
    
    // ID de la partida en caso de estar cargando una partida de la BD
    private int idPartidaCargar = -1;
    
    // Turno actual restaurado de una carga
    private int turnoModoFuga = 0;

    // Constructor privado para evitar que se creen copias con 'new'
    private GameContext() {}

    /**
     * Método para obtener la única instancia permitida del contexto.
     */
    public static synchronized GameContext getInstance() {
        if (instance == null) {
            instance = new GameContext();
        }
        return instance;
    }

    /**
     * Limpia los datos de la sesión actual (útil al cerrar sesión).
     */
    public void reset() {
        currentUser = null;
        configuredPlayers.clear();
        seed = null;
        idPartidaCargar = -1;
        turnoModoFuga = 0;
        actionToConfirm = null;
    }

    // --- GETTERS Y SETTERS ---
    // Permiten a las vistas leer y escribir los datos compartidos

    public Jugador getCurrentUser() { return currentUser; }
    public void setCurrentUser(Jugador currentUser) { this.currentUser = currentUser; }

    public List<Jugador> getConfiguredPlayers() { return configuredPlayers; }
    public void setConfiguredPlayers(List<Jugador> players) { this.configuredPlayers = players; }

    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }

    public int getIdPartidaCargar() { return idPartidaCargar; }
    public void setIdPartidaCargar(int id) { this.idPartidaCargar = id; }
    
    public boolean isPartidaCargada() { return idPartidaCargar != -1; }
    
    public int getTurnoCargado() { return turnoModoFuga; }
    public void setTurnoCargado(int t) { this.turnoModoFuga = t; }

    public ActionConfirmType getActionToConfirm() { return actionToConfirm; }
    public void setActionToConfirm(ActionConfirmType action) { this.actionToConfirm = action; }
}
