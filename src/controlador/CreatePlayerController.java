package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import datos.BBDD;

public class CreatePlayerController {

    @FXML private TextField newUserNameField;
    @FXML private PasswordField newUserPassField;
    @FXML private Label errorLabel;

    private BBDD db = new BBDD();

    @FXML
    private void handleCreatePlayer(ActionEvent event) {
        String user = newUserNameField.getText();
        String pass = newUserPassField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Campos vacíos");
            return;
        }

        if (db.registrarNuevoJugador(user, pass)) {
            showLogin(event);
        } else {
            showError("Error al registrar (usuario ocupado)");
        }
    }

    @FXML
    private void showLogin(ActionEvent event) {
        NavigationController.navigateTo(event, "LoginView.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
