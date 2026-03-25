package vista;

import java.io.IOException;
import java.net.URL;
import java.io.File;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class Intro {

    @FXML
    private MediaView mediaView;

    private MediaPlayer mediaPlayer;

    @FXML
    public void initialize() {
        // Ruta al video
        String path = "src/assets/BadLabsIntro/BadLabsIntro.mp4";
        File file = new File(path);
        
        if (file.exists()) {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Importante: El video debe ser proporcional al contenedor
            mediaView.setPreserveRatio(true);

            // Al terminar el video, pasar al menú principal
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.stop();
                cargarMenuPrincipal();
            });

            // Solo reproducimos cuando el video esté listo para evitar tirones
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
            });
        } else {
            System.err.println("No se encontró el archivo de video en: " + path);
            cargarMenuPrincipal(); // Si no hay video, saltamos al menú
        }
    }

    private void cargarMenuPrincipal() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/MenuPrincipal.fxml"));
            Scene scene = new Scene(root);
            
            // Obtener el Stage desde el mediaView
            Stage stage = (Stage) mediaView.getScene().getWindow();
            
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
