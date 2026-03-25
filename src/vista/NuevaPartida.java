package vista;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class NuevaPartida {

	@FXML
	private Button bnt_aleatorio;
	@FXML
	private Button bnt_seed;
	@FXML
	private TextField txt_seed;
	@FXML
	private Button btn_volver;

	@FXML
	void handleVolver(ActionEvent event) {
		cambiarEscena(event, "/vista/MenuPrincipal.fxml");
	}

	@FXML
	void handleSiguiente(ActionEvent event) {
		// Aquí se guardaría la seed si fuera necesario
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
