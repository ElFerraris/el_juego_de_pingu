package controlador;

import modelo.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton to hold shared state during menu navigation.
 */
public class GameContext {
    private static GameContext instance;

    private Jugador currentUser;
    private List<Jugador> configuredPlayers = new ArrayList<>();
    private String seed;
    private int idPartidaCargar = -1;

    private GameContext() {}

    public static synchronized GameContext getInstance() {
        if (instance == null) {
            instance = new GameContext();
        }
        return instance;
    }

    public void reset() {
        currentUser = null;
        configuredPlayers.clear();
        seed = null;
        idPartidaCargar = -1;
    }

    // Getters and Setters
    public Jugador getCurrentUser() { return currentUser; }
    public void setCurrentUser(Jugador currentUser) { this.currentUser = currentUser; }

    public List<Jugador> getConfiguredPlayers() { return configuredPlayers; }
    public void setConfiguredPlayers(List<Jugador> players) { this.configuredPlayers = players; }

    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }

    public int getIdPartidaCargar() { return idPartidaCargar; }
    public void setIdPartidaCargar(int id) { this.idPartidaCargar = id; }
}
