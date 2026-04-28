package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.application.Platform;

/**
 * Controlador para los diálogos de confirmación personalizados.
 * 
 * <p>
 * Este controlador gestiona una ventana emergente que solicita confirmación al
 * usuario
 * antes de realizar acciones irreversibles o críticas, como cerrar la sesión
 * actual
 * o salir completamente de la aplicación.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class ConfirmDialogController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;

    /**
     * Inicializa el diálogo configurando el texto según la acción a confirmar.
     * 
     * <p>
     * Consulta el {@link GameContext} para determinar si se trata de un Logout o un
     * Quit
     * y adapta los mensajes de título y descripción en consecuencia.
     * </p>
     */
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

    /**
     * Ejecuta la acción confirmada por el usuario.
     * 
     * <p>
     * Si la acción es Logout, reinicia la aplicación. Si es Quit, cierra el proceso
     * de ejecución completamente.
     * </p>
     * 
     * @param event El evento desencadenado por el botón "Sí".
     */
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

    /**
     * Cancela la acción y vuelve al menú principal.
     * 
     * @param event El evento desencadenado por el botón "No".
     */
    @FXML
    private void handleNo(ActionEvent event) {
        // Volvemos al menú principal con la animación de "bajado"
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }
}
