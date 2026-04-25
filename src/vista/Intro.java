package vista;

import java.io.IOException;
import java.io.File;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Intro {

    @FXML
    private MediaView mediaView;

    @FXML
    private StackPane rootPane;

    private MediaPlayer mediaPlayer;
    private boolean yaFinalizado = false;
    private javafx.event.EventHandler<KeyEvent> skipFilter;

    @FXML
    public void initialize() {
        // Paramos la música de menús para que no se solape con el audio del video
        util.SoundManager.stopMusic();

        // Configuramos la ruta del vídeo
        String path = "src/assets/BadLabsIntro/BadLabsIntro.mp4";
        File file = new File(path);

        if (file.exists()) {
            try {
                Media media = new Media(file.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaView.setPreserveRatio(true);

                // Aplicar volumen de música guardado
                mediaPlayer.setVolume(util.SettingsManager.getInstance().getMusicVolume());

                // --- MANEJO DE ERRORES ---
                mediaPlayer.setOnError(() -> {
                    System.err.println("► ERROR Media: " + mediaPlayer.getError().getMessage());
                    finalizarIntro();
                });

                media.setOnError(() -> {
                    System.err.println("► ERROR Fichero Media: " + media.getError().getMessage());
                    finalizarIntro();
                });

                // Al terminar el video, pasar al menú principal
                mediaPlayer.setOnEndOfMedia(this::finalizarIntro);

                // Asegurar que el video se reproduzca solo cuando esté listo
                mediaPlayer.setOnReady(() -> {
                    System.out.println("► Intro lista para reproducir.");
                    mediaPlayer.play();
                    ajustarDimensiones();
                });

                // Fallback: Si en 5 segundos no ha empezado, saltamos (evita pantalla en negro infinita)
                javafx.animation.PauseTransition failsafe = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
                failsafe.setOnFinished(e -> {
                    if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                        System.out.println("► Failsafe: La intro tarda demasiado en cargar, saltando...");
                        finalizarIntro();
                    }
                });
                failsafe.play();

            } catch (Exception e) {
                System.err.println("Error al inicializar el MediaPlayer: " + e.getMessage());
                finalizarIntro();
            }
        } else {
            System.err.println("No se encontró el archivo de video en: " + path);
            cargarMenuPrincipal();
        }

        // Configurar escena y foco
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                configurarEscena(newScene);
            }
        });
    }

    private void configurarEscena(Scene scene) {
        // 1. Detección de teclado
        this.skipFilter = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                System.out.println("► Skip detectado (ENTER)");
                finalizarIntro();
                event.consume();
            }
        };
        scene.addEventFilter(KeyEvent.KEY_PRESSED, skipFilter);

        // 2. Quitar el mensaje de ESC (con seguridad de Window)
        if (scene.getWindow() != null) {
            aplicarExitHint(scene.getWindow());
        } else {
            scene.windowProperty().addListener((obs, oldWin, newWin) -> {
                if (newWin != null) {
                    aplicarExitHint(newWin);
                }
            });
        }

        // 3. Forzar foco para que el teclado funcione
        rootPane.requestFocus();
    }

    private void aplicarExitHint(Window window) {
        if (window instanceof Stage stage) {
            stage.setFullScreenExitHint("");
        }
    }

    private void ajustarDimensiones() {
        if (mediaView.getScene() != null) {
            mediaView.fitWidthProperty().bind(mediaView.getScene().widthProperty());
            mediaView.fitHeightProperty().bind(mediaView.getScene().heightProperty());
        }
    }

    private void finalizarIntro() {
        if (!yaFinalizado) {
            yaFinalizado = true;
            
            // Limpiamos el filtro de la escena para que no persista en el menú
            if (rootPane != null && rootPane.getScene() != null && skipFilter != null) {
                rootPane.getScene().removeEventFilter(KeyEvent.KEY_PRESSED, skipFilter);
            }
            
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            cargarMenuPrincipal();
        }
    }

    private void cargarMenuPrincipal() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            if (stage != null) {
                // Usamos nuestro controlador de navegación respetando los ajustes guardados (Andrei Style)
                boolean fs = util.SettingsManager.getInstance().isFullscreen();
                controlador.NavigationController.navigateTo(stage, "MainMenuView.fxml", fs);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el Menú Principal desde la Intro.");
            e.printStackTrace();
        }
    }
}
