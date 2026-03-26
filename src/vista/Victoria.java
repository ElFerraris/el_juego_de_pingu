package vista;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import modelo.CPU;
import modelo.Jugador;

public class Victoria {

    @FXML private Label lbl_ganador;
    @FXML private Label lbl_subtitulo;

    public void setGanador(Jugador ganador) {
        String emoji = (ganador instanceof CPU) ? "🦭" : "🐧";
        lbl_ganador.setText(emoji + "  " + ganador.getNombre() + "  " + emoji);
        if (ganador instanceof CPU) {
            lbl_subtitulo.setText("¡La foca ha arrasado con todos! 😱");
        }
    }

    @FXML
    void handleNuevaPartida(ActionEvent event) {
        GameState.getInstance().resetJuego();
        cambiarEscena(event, "/vista/NuevaPartida.fxml");
    }

    @FXML
    void handleMenu(ActionEvent event) {
        GameState.getInstance().resetJuego();
        cambiarEscena(event, "/vista/MenuPrincipal.fxml");
    }

    @FXML
    void handleSalir(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

    private void cambiarEscena(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setFullScreen(false);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
