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
        // Navegamos a la configuración de jugadores con transición a la izquierda
        NavigationController.navigateTo(event, "PlayerConfigView.fxml", NavigationController.Direction.LEFT);
    }

    @FXML
    private void showLoadGame(ActionEvent event) {
        // Navegamos a la carga de partidas
        NavigationController.navigateTo(event, "LoadGameView.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Obtenemos la ventana actual y pedimos un reinicio limpio
        Stage currentStage = (Stage) welcomeLabel.getScene().getWindow();
        NavigationController.logoutAndRestart(currentStage);
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        // Cerramos la aplicación de forma segura
        javafx.application.Platform.exit();
    }
}
