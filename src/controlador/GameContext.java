package controlador;

import modelo.Jugador;
import java.util.ArrayList;
import java.util.List;

/**
 * GameContext
 * 
 * Esta clase guarda los datos importantes que necesitan compartir las
 * diferentes pantallas
 * (como el usuario que ha entrado, la semilla del tablero, etc.).
 * 
 * Se usa el patrón Singleton para que solo haya una copia de estos datos en
 * todo el programa.
 */
public class GameContext {

    /** La única instancia permitida de esta clase. */
    private static GameContext instance;

    /**
     * Clase para definir qué tipo de acción requiere una confirmación
     * por parte del usuario en los diálogos emergentes.
     */
    public static class ActionConfirmType {
        public static final int LOGOUT = 0;
        public static final int QUIT = 1;
        public static final int NONE = -1;
    }

    /** Almacena la acción que está pendiente de confirmación. */
    private int actionToConfirm = ActionConfirmType.NONE;

    /** El usuario (Jugador) que ha iniciado sesión actualmente en el sistema. */
    private Jugador currentUser;

    /**
     * Lista de jugadores que participarán en la partida que se está configurando.
     */
    private List<Jugador> configuredPlayers = new ArrayList<>();

    /**
     * Código de generación (Seed) del tablero. Permite recrear el mismo mapa
     * aleatorio en diferentes sesiones.
     */
    private String seed;

    /**
     * Nombre asignado a la partida actual para su almacenamiento en la Base de
     * Datos.
     */
    private String gameName;

    /**
     * Identificador de la partida en la BD.
     * Un valor de -1 indica que es una partida nueva (no cargada).
     */
    private int idPartidaCargar = -1;

    /** Almacena el turno en el que se guardó la partida para poder restaurarlo. */
    private int turnoModoFuga = 0;

    /**
     * Constructor privado.
     * <p>
     * Al ser privado, impide que otras clases creen nuevas instancias usando 'new
     * GameContext()',
     * obligándolas a pasar por {@link #getInstance()}.
     * </p>
     */
    private GameContext() {
    }

    /**
     * Obtiene la instancia única de GameContext.
     * 
     * <p>
     * Si la instancia aún no existe, la crea. Si ya existe, devuelve la existente.
     * Se ha eliminado la sincronización para simplificar el flujo según requisitos
     * del proyecto.
     * </p>
     * 
     * @return La instancia única de GameContext.
     */
    public static GameContext getInstance() {
        if (instance == null) {
            instance = new GameContext();
        }
        return instance;
    }

    /**
     * Reinicia todos los datos del contexto a sus valores por defecto.
     * 
     * <p>
     * Es fundamental llamar a este método al cerrar la sesión (Logout) para
     * asegurar que el siguiente usuario no vea datos del anterior.
     * </p>
     */
    public void reset() {
        currentUser = null;
        configuredPlayers.clear();
        seed = null;
        gameName = null;
        idPartidaCargar = -1;
        turnoModoFuga = 0;
        actionToConfirm = ActionConfirmType.NONE;
        System.out.println("► GameContext: Datos reiniciados.");
    }

    // --- MÉTODOS DE ACCESO (GETTERS Y SETTERS) ---
    // Permiten a los controladores de las vistas leer y modificar el estado global.

    /** @return El usuario actual logueado. */
    public Jugador getCurrentUser() {
        return currentUser;
    }

    /** @param currentUser Define el usuario que acaba de hacer login. */
    public void setCurrentUser(Jugador currentUser) {
        this.currentUser = currentUser;
    }

    /** @return La lista de jugadores configurados para la partida. */
    public List<Jugador> getConfiguredPlayers() {
        return configuredPlayers;
    }

    /** @param players Establece la lista de competidores. */
    public void setConfiguredPlayers(List<Jugador> players) {
        this.configuredPlayers = players;
    }

    /** @return El código de generación del mapa actual. */
    public String getSeed() {
        return seed;
    }

    /** @param seed Establece el código para generar el tablero. */
    public void setSeed(String seed) {
        this.seed = seed;
    }

    /** @return El nombre de la partida actual. */
    public String getGameName() {
        return gameName;
    }

    /** @param gameName Define el nombre de la partida (ej: "Partida de Prueba"). */
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    /** @return El ID de la partida en la base de datos. */
    public int getIdPartidaCargar() {
        return idPartidaCargar;
    }

    /** @param id Establece el ID para cargar una partida existente. */
    public void setIdPartidaCargar(int id) {
        this.idPartidaCargar = id;
    }

    /**
     * @return True si estamos cargando una partida guardada, false si es una
     *         partida nueva.
     */
    public boolean isPartidaCargada() {
        return idPartidaCargar != -1;
    }

    /** @return El turno recuperado de la base de datos. */
    public int getTurnoCargado() {
        return turnoModoFuga;
    }

    /** @param t Establece el turno desde el cual se debe reanudar el juego. */
    public void setTurnoCargado(int t) {
        this.turnoModoFuga = t;
    }

    /** @return La acción que requiere confirmación actualmente. */
    public int getActionToConfirm() {
        return actionToConfirm;
    }

    /** @param action Define qué acción debe validar el diálogo de confirmación. */
    public void setActionToConfirm(int action) {
        this.actionToConfirm = action;
    }
}
