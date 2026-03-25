package vista;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CargarPartida {

	@FXML
	void handleVolver(ActionEvent event) {
		cambiarEscena(event, "/vista/MenuPrincipal.fxml");
	}

	@FXML
	void handleSiguiente(ActionEvent event) {
		cambiarEscena(event, "/vista/SelectorJugadores.fxml");
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
