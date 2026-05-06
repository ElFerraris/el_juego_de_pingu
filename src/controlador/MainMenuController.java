package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import modelo.Jugador;
import javafx.application.Platform;

/**
 * Controlador para la vista del Menú Principal.
 * 
 * <p>
 * Esta clase se encarga de gestionar las interacciones del usuario en la
 * pantalla inicial
 * del juego después del login. Permite navegar hacia la configuración de
 * partida, cargar
 * partidas guardadas, acceder a opciones o cerrar la sesión.
 * </p>
 * 
 * <p>
 * En JavaFX, los controladores se enlazan con archivos .fxml mediante la
 * anotación {@code @FXML}.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class MainMenuController {

    /** Etiqueta que muestra el mensaje de bienvenida al jugador actual. */
    @FXML
    private Label welcomeLabel;

    /**
     * Método de inicialización automática de JavaFX.
     * 
     * <p>
     * Se ejecuta después de que el archivo FXML ha sido cargado y los campos
     * anotados
     * con {@code @FXML} han sido inyectados. Aquí preparamos la interfaz con los
     * datos
     * del usuario logueado.
     * </p>
     */
    @FXML
    public void initialize() {
        // Iniciar la música del menú
        util.SoundManager.playMenuMusic();

        // Obtenemos el usuario del contexto global (Singleton) para personalizar el
        // saludo
        Jugador current = GameContext.getInstance().getCurrentUser();

        // Mostramos su nombre
        welcomeLabel.setText("Bienvenido " + current.getNombre());
    }

    /**
     * Navega a la pantalla de configuración de jugadores.
     * 
     * @param event El evento de acción disparado por el botón "Nueva Partida".
     */
    @FXML
    private void showPlayerConfig(ActionEvent event) {
        // Usamos el NavigationController para cambiar de escena.
        // La dirección FORWARD suele implicar una animación de entrada/subida.
        NavigationController.navigateTo(event, "PlayerConfigView.fxml", NavigationController.Direction.LEFT);
    }

    /**
     * Navega a la pantalla de carga de partidas guardadas.
     * 
     * @param event El evento de acción disparado por el botón "Cargar Partida".
     */
    @FXML
    private void showLoadGame(ActionEvent event) {
        NavigationController.navigateTo(event, "LoadGameView.fxml", NavigationController.Direction.LEFT);
    }

    /**
     * Navega a la pantalla de opciones/ajustes del juego.
     * 
     * @param event El evento de acción disparado por el botón "Opciones".
     */
    @FXML
    private void showOptions(ActionEvent event) {
        NavigationController.navigateTo(event, "OptionsView.fxml", NavigationController.Direction.FORWARD);
    }

    /**
     * Navega a la vista unificada de estadísticas y rankings del sistema.
     * 
     * @param event El evento de acción disparado por el botón "Rankings".
     */
    @FXML
    private void showRankingsView(ActionEvent event) {
        NavigationController.navigateTo(event, "RankingsView.fxml", NavigationController.Direction.LEFT);
    }

    /**
     * Gestiona el cierre de sesión del usuario.
     * 
     * <p>
     * En lugar de cerrar directamente, marca el tipo de acción en el contexto
     * global
     * y abre un diálogo de confirmación.
     * </p>
     * 
     * @param event El evento de acción disparado por el botón "Cerrar Sesión".
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // Marcamos la intención de cerrar sesión para que el diálogo sepa qué confirmar
        GameContext.getInstance().setActionToConfirm(GameContext.ActionConfirmType.LOGOUT);
        NavigationController.navigateTo(event, "ConfirmDialogView.fxml", NavigationController.Direction.FORWARD);
    }

    /**
     * Gestiona la salida completa de la aplicación.
     * 
     * <p>
     * Similar al logout, solicita confirmación mediante un diálogo emergente.
     * </p>
     * 
     * @param event El evento de acción disparado por el botón "Salir".
     */
    @FXML
    private void handleQuitGame(ActionEvent event) {
        // Marcamos la intención de salir del programa
        GameContext.getInstance().setActionToConfirm(GameContext.ActionConfirmType.QUIT);
        NavigationController.navigateTo(event, "ConfirmDialogView.fxml", NavigationController.Direction.FORWARD);
    }
}
