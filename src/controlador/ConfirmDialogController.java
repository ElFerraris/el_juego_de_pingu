package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * ConfirmDialogController
 * 
 * Gestiona la confirmación de acciones críticas como cerrar sesión o salir.
 */
public class ConfirmDialogController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        int action = GameContext.getInstance().getActionToConfirm();
        
        if (action == GameContext.ActionConfirmType.LOGOUT) {
            titleLabel.setText("CERRAR SESIÓN");
            messageLabel.setText("¿Estás seguro de que quieres cerrar la sesión actual?");
        } else if (action == GameContext.ActionConfirmType.QUIT) {
            titleLabel.setText("SALIR DEL JUEGO");
            messageLabel.setText("¿Estás seguro de que quieres salir y cerrar el juego?");
        }
    }

    @FXML
    private void handleYes(ActionEvent event) {
        int action = GameContext.getInstance().getActionToConfirm();
        
        if (action == GameContext.ActionConfirmType.LOGOUT) {
            // Cerramos sesión y reiniciamos
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            NavigationController.logoutAndRestart(stage);
        } else if (action == GameContext.ActionConfirmType.QUIT) {
            // Salimos del programa
            Platform.exit();
        }
    }

    @FXML
    private void handleNo(ActionEvent event) {
        // Volvemos al menú principal con la animación de "bajado"
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }
}
