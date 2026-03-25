package vista;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public class SelectorJugadores {

	@FXML
	private Button btn_volver;
	@FXML
	private Button btn_jugar_partida;
	@FXML
	private ChoiceBox box_j2;
	@FXML
	private ChoiceBox box_j3;
	@FXML
	private ChoiceBox box_j4;

	@FXML
	void handleVolver(ActionEvent event) {
		// Volvemos a Nueva Partida por defecto
		cambiarEscena(event, "/vista/NuevaPartida.fxml");
	}

	@FXML
	void handleIniciarJuego(ActionEvent event) {
		cambiarEscena(event, "/vista/TableroJuego.fxml");
	}

	private void cambiarEscena(ActionEvent event, String fxmlPath) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
			Scene scene = new Scene(root);
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(scene);
			stage.centerOnScreen();
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
