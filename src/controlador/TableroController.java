package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;

/**
 * TableroController
 * 
 * Este es un marcador de posición (placeholder) para la lógica de juego real.
 */
public class TableroController {

    @FXML private Label infoLabel;

    @FXML
    public void initialize() {
        // Mostramos un resumen de lo que el juego ha configurado hasta ahora
        int numJugadores = GameContext.getInstance().getConfiguredPlayers().size();
        String seed = GameContext.getInstance().getSeed();
        
        infoLabel.setText("Partida con " + numJugadores + " jugadores. Semilla: " + seed);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml");
    }
}
