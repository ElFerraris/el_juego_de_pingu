package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.util.List;
import modelo.Jugador;
import modelo.Tablero;

public class SeedSelectionController {

    @FXML private TextField seedField;
    @FXML private Label seedErrorLabel;

    @FXML
    private void handleRandomSeed(ActionEvent event) {
        GameContext.getInstance().setSeed(null);
        startGame(event);
    }

    @FXML
    private void handleManualSeed(ActionEvent event) {
        String seed = seedField.getText();
        if (seed.length() != 48 || !seed.matches("[0-9]+")) {
            seedErrorLabel.setText("La seed debe tener 48 dígitos (0-5)");
            return;
        }
        GameContext.getInstance().setSeed(seed);
        startGame(event);
    }

    @FXML
    private void showPlayerConfig(ActionEvent event) {
        NavigationController.navigateTo(event, "PlayerConfigView.fxml");
    }

    private void startGame(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/PantallaJuego.fxml"));
            Parent root = loader.load();
            
            JuegoController gameController = loader.getController();
            Juego nuevoJuego = new Juego();
            
            GameContext ctx = GameContext.getInstance();
            if (ctx.getSeed() != null) {
                nuevoJuego.getTablero().introducirSeed(ctx.getSeed());
            } else {
                nuevoJuego.getTablero().generarSeedAleatoria();
            }

            for (Jugador j : ctx.getConfiguredPlayers()) {
                nuevoJuego.agregarJugador(j);
            }
            nuevoJuego.iniciarPartida();
            
            gameController.setJuego(nuevoJuego);
            gameController.inicializarActual();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
