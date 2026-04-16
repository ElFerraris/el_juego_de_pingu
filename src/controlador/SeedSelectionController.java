package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import modelo.Tablero;

/**
 * SeedSelectionController
 * 
 * Gestiona la selección de mundos y semillas con validación en tiempo real.
 */
public class SeedSelectionController {

    @FXML private TextField nameField;
    @FXML private TextField seedField;
    @FXML private Label errorLabel;
    
    @FXML private Button btnRandom;
    @FXML private Button btnCustom;

    private boolean isCustomMode = false;

    @FXML
    public void initialize() {
        // Limitador de 100 caracteres para el nombre
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 100) {
                nameField.setText(oldValue);
            }
        });
        
        // Por defecto: Semilla aleatoria
        handleRandomSeed(null);
    }

    /**
     * Genera automáticamente una semilla válida.
     */
    @FXML
    private void handleRandomSeed(ActionEvent event) {
        desmarcarTodos();
        btnRandom.getStyleClass().add("button-selected");
        
        isCustomMode = false;
        seedField.setDisable(true);
        
        // Generamos una semilla válida real
        String validSeed = Tablero.generarSeedValida();
        seedField.setText(validSeed);
        
        errorLabel.setText("Estado: Semilla Aleatoria Generada");
    }

    /**
     * Activa el modo de semilla personalizada.
     */
    @FXML
    private void handleEnableCustomSeed(ActionEvent event) {
        desmarcarTodos();
        btnCustom.getStyleClass().add("button-selected");
        
        isCustomMode = true;
        seedField.setDisable(false);
        seedField.clear();
        seedField.setPromptText("Escribe aquí tu semilla numérica...");
        seedField.requestFocus();
        
        errorLabel.setText("Estado: Modo Personalizado");
    }

    private void desmarcarTodos() {
        btnRandom.getStyleClass().remove("button-selected");
        btnCustom.getStyleClass().remove("button-selected");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "PlayerConfigView.fxml", NavigationController.Direction.BACKWARD);
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        String name = nameField.getText();
        String seed = seedField.getText();

        // 1. Validar nombre
        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("¡Falta el nombre de la partida!");
            nameField.requestFocus();
            return;
        }

        // 2. Validar semilla
        if (seed == null || seed.trim().isEmpty()) {
            errorLabel.setText("¡Falta la semilla del mundo!");
            return;
        }

        Tablero validador = new Tablero();
        if (!validador.validarSeed(seed)) {
            errorLabel.setText("Semilla inválida: Faltan trineos o agujeros.");
            return;
        }

        // Guardar en el contexto global
        GameContext.getInstance().setGameName(name.trim());
        GameContext.getInstance().setSeed(seed.trim());
        
        System.out.println("► Partida: " + name + " | Seed: " + seed);

        NavigationController.navigateToBoardAsync(event, "TableroJuego.fxml");
    }
}
