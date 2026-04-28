package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import modelo.Tablero;

/**
 * Controlador de la vista de Selección de Semilla.
 * 
 * <p>
 * Gestiona la selección del mundo (tablero) mediante semillas (seeds). Permite
 * generar
 * un mundo aleatorio o introducir una semilla personalizada validando que
 * cumpla
 * los requisitos mínimos (ej. contener al menos un trineo y un agujero).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class SeedSelectionController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField seedField;
    @FXML
    private Label errorLabel;

    @FXML
    private Button btnRandom;
    @FXML
    private Button btnCustom;

    private boolean isCustomMode = false;

    /**
     * Inicializa la vista de selección de semilla.
     * 
     * <p>
     * Configura el límite de caracteres para el nombre de la partida y
     * selecciona por defecto la generación de semilla aleatoria.
     * </p>
     */
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
     * Genera automáticamente una semilla válida y la muestra en pantalla.
     * 
     * @param event El evento desencadenado por el botón "Aleatorio".
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
     * Activa el modo de semilla personalizada, habilitando el campo de texto
     * para que el usuario escriba su propia semilla.
     * 
     * @param event El evento desencadenado por el botón "Personalizado".
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

    /**
     * Copia la semilla actual al portapapeles del sistema operativo.
     * 
     * @param event El evento desencadenado por el botón de copiar.
     */
    @FXML
    private void handleCopySeed(ActionEvent event) {
        String seed = seedField.getText();
        if (seed != null && !seed.isEmpty()) {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(seed);
            clipboard.setContent(content);
            errorLabel.setText("¡Semilla copiada al portapapeles! 📋");
        }
    }

    /**
     * Elimina el estado visual de "seleccionado" de los botones de tipo de semilla.
     */
    private void desmarcarTodos() {
        btnRandom.getStyleClass().remove("button-selected");
        btnCustom.getStyleClass().remove("button-selected");
    }

    /**
     * Vuelve a la pantalla de configuración de jugadores.
     * 
     * @param event El evento desencadenado por el botón "Atrás".
     */
    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "PlayerConfigView.fxml", NavigationController.Direction.BACKWARD);
    }

    /**
     * Valida los datos introducidos (nombre y semilla) y arranca la partida.
     * 
     * <p>
     * Comprueba que los campos no estén vacíos y que la semilla introducida sea
     * válida.
     * Si todo es correcto, guarda la información en {@link GameContext} e inicia el
     * tablero.
     * </p>
     * 
     * @param event El evento desencadenado por el botón "Jugar".
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        String name = nameField.getText();
        String seed = seedField.getText();

        // 1. Validar nombre
        if (name == null || name.trim().isEmpty()) {
            errorLabel.setText("¡Falta el nombre de la partida!");
            nameField.requestFocus();
        } else {
            // 2. Validar semilla
            if (seed == null || seed.trim().isEmpty()) {
                errorLabel.setText("¡Falta la semilla del mundo!");
            } else {
                Tablero validador = new Tablero();
                if (!validador.validarSeed(seed)) {
                    errorLabel.setText("Semilla inválida: Faltan trineos o agujeros.");
                } else {
                    // Guardar en el contexto global
                    GameContext.getInstance().setGameName(name.trim());
                    GameContext.getInstance().setSeed(seed.trim());

                    System.out.println("► Partida: " + name + " | Seed: " + seed);

                    NavigationController.navigateToBoardAsync(event, "TableroJuego.fxml");
                }
            }
        }
    }
}
