package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.Node;

/**
 * SeedSelectionController
 * 
 * Permite al usuario elegir el mundo (semilla) antes de empezar la partida.
 * Los datos se guardan en el GameContext para que el Generador de Tableros los use.
 */
public class SeedSelectionController {

    @FXML private TextField seedField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Podríamos precargar una semilla aleatoria si quisiéramos
        seedField.setText("WORLD_" + (int)(Math.random() * 9999));
    }

    /**
     * Maneja los botones de selección rápida (Isla, Valle, Montaña).
     */
    @FXML
    private void handleQuickSeed(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String quickSeed = (String) btn.getUserData();
        if (quickSeed != null) {
            seedField.setText(quickSeed);
        }
    }

    /**
     * Navega de vuelta a la configuración de jugadores.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "PlayerConfigView.fxml");
    }

    /**
     * Guarda la semilla y lanza el tablero de juego.
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        String seed = seedField.getText();

        if (seed == null || seed.trim().isEmpty()) {
            errorLabel.setText("Debes introducir una semilla o elegir un mundo.");
            return;
        }

        // Guardamos la semilla en el contexto global
        GameContext.getInstance().setSeed(seed.trim());

        System.out.println("► Semilla seleccionada: " + seed);
        System.out.println("► Jugadores listos: " + GameContext.getInstance().getConfiguredPlayers().size());

        // Navegamos al tablero real (por ahora el placeholder)
        NavigationController.navigateTo(event, "TableroJuego.fxml");
    }
}
