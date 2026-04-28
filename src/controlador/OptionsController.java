package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import util.SettingsManager;
import util.SoundManager;
import javafx.application.Platform;

/**
 * Controlador para la vista de Opciones.
 * 
 * <p>
 * Gestiona la lógica de la pantalla de configuración, permitiendo al usuario
 * modificar
 * ajustes como la resolución, el modo de pantalla completa y los volúmenes de
 * música y SFX.
 * Incluye detección de cambios (estado "dirty") para habilitar el botón de
 * aplicar solo
 * cuando es necesario.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class OptionsController {

    @FXML
    private ComboBox<String> resolutionCombo;
    @FXML
    private CheckBox fullscreenCheck;
    @FXML
    private Slider musicSlider;
    @FXML
    private Label musicLabel;
    @FXML
    private Slider sfxSlider;
    @FXML
    private Label sfxLabel;
    @FXML
    private Button applyButton;

    // Valores iniciales para detectar cambios (Dirty State)
    private String initialResolution;
    private boolean initialFullscreen;
    private double initialMusic;
    private double initialSfx;

    /**
     * Inicializa la vista de opciones.
     * 
     * <p>
     * Carga los valores actuales desde {@link util.SettingsManager} y los establece
     * en los controles visuales correspondientes. Configura los listeners
     * iniciales.
     * </p>
     */
    @FXML
    public void initialize() {
        // Rellenar las resoluciones
        resolutionCombo.getItems().addAll("1280x720", "1600x900", "1920x1080");

        // Cargar los valores actuales desde el SettingsManager
        SettingsManager sm = SettingsManager.getInstance();

        initialResolution = sm.getResolution();
        initialFullscreen = sm.isFullscreen();
        initialMusic = sm.getMusicVolume() * 100;
        initialSfx = sm.getSfxVolume() * 100;

        // Establecer valores en los controles
        resolutionCombo.setValue(initialResolution);
        fullscreenCheck.setSelected(initialFullscreen);
        musicSlider.setValue(initialMusic);
        sfxSlider.setValue(initialSfx);
        musicLabel.setText(Math.round(initialMusic) + "%");
        sfxLabel.setText(Math.round(initialSfx) + "%");

        // Deshabilitar botón por defecto
        applyButton.setDisable(true);

        // Añadir listeners para detectar cambios
        setupListeners();

        System.out.println("► Opciones inicializadas - Fullscreen: " + initialFullscreen);
    }

    /**
     * Configura los oyentes (listeners) de los diferentes componentes visuales.
     * 
     * <p>
     * Detecta cambios en el ComboBox de resolución, el CheckBox de pantalla
     * completa
     * y los sliders de volumen. También sincroniza externamente el estado de la
     * ventana
     * (por ejemplo, si el usuario pulsa F11 en vez de usar el CheckBox).
     * </p>
     */
    private void setupListeners() {
        resolutionCombo.valueProperty().addListener((obs, oldVal, newVal) -> checkChanges());
        fullscreenCheck.selectedProperty().addListener((obs, oldVal, newVal) -> checkChanges());
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            musicLabel.setText(Math.round(newVal.doubleValue()) + "%");
            checkChanges();
        });
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sfxLabel.setText(Math.round(newVal.doubleValue()) + "%");
            checkChanges();
        });

        // Sincronización en tiempo real si el estado cambia externamente (tecla F11)
        applyButton.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsW, oldWin, newWin) -> {
                    if (newWin instanceof Stage stage) {
                        stage.fullScreenProperty().addListener((obsF, oldF, isNowFull) -> {
                            if (fullscreenCheck.isSelected() != isNowFull) {
                                fullscreenCheck.setSelected(isNowFull);
                                initialFullscreen = isNowFull; // Sincronizamos el estado inicial para evitar marcar
                                                               // cambios falsos
                                checkChanges();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Comprueba si hay cambios entre los valores actuales de los componentes
     * visuales
     * y los valores iniciales. Si hay cambios, habilita el botón "Aplicar" y le
     * añade la clase CSS "button-dirty" para indicarlo visualmente.
     */
    private void checkChanges() {
        boolean changed = !resolutionCombo.getValue().equals(initialResolution) ||
                fullscreenCheck.isSelected() != initialFullscreen ||
                Math.abs(musicSlider.getValue() - initialMusic) > 0.1 ||
                Math.abs(sfxSlider.getValue() - initialSfx) > 0.1;

        applyButton.setDisable(!changed);

        if (changed) {
            if (!applyButton.getStyleClass().contains("button-dirty")) {
                applyButton.getStyleClass().add("button-dirty");
            }
        } else {
            applyButton.getStyleClass().remove("button-dirty");
        }
    }

    /**
     * Aplica los cambios realizados en las opciones y los guarda en las
     * preferencias.
     * 
     * <p>
     * Actualiza la resolución, estado de pantalla completa y los volúmenes de
     * audio tanto en la configuración persistente como de manera visual e
     * inmediata.
     * </p>
     * 
     * @param event El evento desencadenado por el botón "Aplicar".
     */
    @FXML
    private void handleApply(ActionEvent event) {
        SettingsManager sm = SettingsManager.getInstance();

        // Guardar en el manager
        sm.setResolution(resolutionCombo.getValue());
        sm.setFullscreen(fullscreenCheck.isSelected());
        sm.setMusicVolume(musicSlider.getValue() / 100.0);
        sm.setSfxVolume(sfxSlider.getValue() / 100.0);

        sm.save();

        // Actualizar valores iniciales para que el botón se deshabilite
        initialResolution = sm.getResolution();
        initialFullscreen = sm.isFullscreen();
        initialMusic = musicSlider.getValue();
        initialSfx = sfxSlider.getValue();
        checkChanges();

        // Aplicar cambios visuales inmediatos
        Stage stage = (Stage) applyButton.getScene().getWindow();

        // 1. Pantalla completa
        stage.setFullScreen(sm.isFullscreen());

        // 2. Resolución (Solo si no está en pantalla completa)
        if (!sm.isFullscreen()) {
            String[] res = sm.getResolution().split("x");
            stage.setWidth(Double.parseDouble(res[0]));
            stage.setHeight(Double.parseDouble(res[1]));
            stage.centerOnScreen();
        }

        // 3. Audio inmediatos
        SoundManager.setSfxVolume(sm.getSfxVolume());
        SoundManager.setMusicVolume(sm.getMusicVolume());

        System.out.println("► Cambios aplicados y guardados correctamente.");
    }

    /**
     * Vuelve al menú principal.
     * 
     * @param event El evento desencadenado por el botón "Volver".
     */
    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }
}
