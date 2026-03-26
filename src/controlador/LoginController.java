package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import datos.BBDD;
import modelo.Jugador;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Label loginErrorLabel;

    private BBDD db = new BBDD();

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = userField.getText();
        String pass = passField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Campos vacíos");
            return;
        }

        int id = db.verificarCredenciales(user, pass);
        if (id != -1) {
            Jugador p = new Jugador(id, user, "Rojo"); // Color temporal, se cargará si es necesario
            GameContext.getInstance().setCurrentUser(p);
            NavigationController.navigateTo(event, "MainMenuView.fxml");
        } else {
            showError("Usuario o contraseña incorrectos");
        }
    }

    @FXML
    private void showCreatePlayer(ActionEvent event) {
        NavigationController.navigateTo(event, "CreatePlayerView.fxml");
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    private void showError(String msg) {
        loginErrorLabel.setText(msg);
        loginErrorLabel.setVisible(true);
    }
}
