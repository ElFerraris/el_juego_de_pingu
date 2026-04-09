package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import modelo.Jugador;
import javafx.application.Platform;

/**
 * MainMenuController
 * 
 * Controlador para la vista del Menú Principal.
 * Gestiona las opciones de iniciar nueva partida, cargar o salir.
 */
public class MainMenuController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        // Obtenemos el usuario del contexto global para darle la bienvenida
        Jugador current = GameContext.getInstance().getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Bienvenido, " + current.getNombre());
        } else {
            welcomeLabel.setText("Bienvenido, Pingüino");
        }
    }

    @FXML
    private void showPlayerConfig(ActionEvent event) {
        // Navegamos a la configuración de jugadores con transición hacia adelante (sube)
        NavigationController.navigateTo(event, "PlayerConfigView.fxml", NavigationController.Direction.FORWARD);
    }

    @FXML
    private void showLoadGame(ActionEvent event) {
        // Navegamos a la carga de partidas con transición hacia adelante
        NavigationController.navigateTo(event, "LoadGameView.fxml", NavigationController.Direction.FORWARD);
    }

    @FXML
    private void showOptions(ActionEvent event) {
        // Navegamos a las opciones con transición hacia adelante
        NavigationController.navigateTo(event, "OptionsView.fxml", NavigationController.Direction.FORWARD);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Marcamos la acción a confirmar y navegamos al diálogo
        GameContext.getInstance().setActionToConfirm(GameContext.ActionConfirmType.LOGOUT);
        NavigationController.navigateTo(event, "ConfirmDialogView.fxml", NavigationController.Direction.FORWARD);
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        // Marcamos la acción a confirmar y navegamos al diálogo
        GameContext.getInstance().setActionToConfirm(GameContext.ActionConfirmType.QUIT);
        NavigationController.navigateTo(event, "ConfirmDialogView.fxml", NavigationController.Direction.FORWARD);
    }
}
