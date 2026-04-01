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

    @FXML
    private TextField seedField;
    @FXML
    private Label errorLabel;

    @FXML
    private Button btnIsla;
    @FXML
    private Button btnValle;
    @FXML
    private Button btnMontana;
    @FXML
    private Button btnCustom;

    private boolean isCustomMode = false;

    @FXML
    public void initialize() {
        // Por defecto, seleccionamos la Isla con una semilla aleatoria
        seleccionarMundo(btnIsla);
    }

    /**
     * Toca un botón de mundo recomendado: genera semilla y bloquea el campo manual.
     */
    @FXML
    private void handleQuickSeed(ActionEvent event) {
        Button selected = (Button) event.getSource();
        seleccionarMundo(selected);
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

        errorLabel.setText("");
    }

    private void seleccionarMundo(Button btn) {
        desmarcarTodos();
        btn.getStyleClass().add("button-selected");

        isCustomMode = false;
        seedField.setDisable(true);

        // Generamos una semilla válida real para este mundo
        String validSeed = Tablero.generarSeedValida();
        seedField.setText(validSeed);

        errorLabel.setText("Modo: " + btn.getText() + " (Semilla Generada)");
    }

    private void desmarcarTodos() {
        btnIsla.getStyleClass().remove("button-selected");
        btnValle.getStyleClass().remove("button-selected");
        btnMontana.getStyleClass().remove("button-selected");
        btnCustom.getStyleClass().remove("button-selected");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "PlayerConfigView.fxml");
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        String seed = seedField.getText();

        if (seed == null || seed.trim().isEmpty()) {
            errorLabel.setText("Debes introducir una semilla o elegir un mundo.");
            return;
        }

        // Validación Real: Usamos un objeto Tablero temporal para validar
        Tablero validador = new Tablero();
        if (!validador.validarSeed(seed)) {
            errorLabel.setText("Semilla inválida: Se requieren al menos 2 trineos ('3') y 2 agujeros ('2').");
            return;
        }

        // Guardamos la semilla en el contexto global
        GameContext.getInstance().setSeed(seed.trim());
        System.out.println("► Semilla Validada: " + seed);

        // Navegamos al tablero
        NavigationController.navigateTo(event, "TableroJuego.fxml");
    }
}
