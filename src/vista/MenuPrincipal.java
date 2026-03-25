package vista;

import java.io.IOException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuPrincipal {

	@FXML
	private Button btn_nueva_partida;
	@FXML
	private Button btn_cargar_partida;
	@FXML
	private Button btn_opciones;
	@FXML
	private Button bnt_salir;
	@FXML
	private Button bnt_cerrar_sesion;

	@FXML
	void handleNuevaPartida(ActionEvent event) {
		cambiarEscena(event, "/vista/NuevaPartida.fxml");
	}

	@FXML
	void handleCargarPartida(ActionEvent event) {
		cambiarEscena(event, "/vista/CargarPartida.fxml");
	}

	@FXML
	void handleCerrarSesion(ActionEvent event) {
		cambiarEscena(event, "/vista/Login.fxml");
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
			stage.setScene(scene);
			stage.centerOnScreen();
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
