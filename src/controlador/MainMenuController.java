package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import modelo.Jugador;

public class MainMenuController {

    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        Jugador current = GameContext.getInstance().getCurrentUser();
        if (current != null) {
            welcomeLabel.setText("Bienvenido, " + current.getNombre());
        }
    }

    @FXML
    private void showPlayerConfig(ActionEvent event) {
        NavigationController.navigateTo(event, "PlayerConfigView.fxml");
    }

    @FXML
    private void showLoadGame(ActionEvent event) {
        NavigationController.navigateTo(event, "LoadGameView.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        GameContext.getInstance().setCurrentUser(null);
        NavigationController.navigateTo(event, "LoginView.fxml");
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        javafx.application.Platform.exit();
    }
}
