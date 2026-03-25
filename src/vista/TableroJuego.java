package vista;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class TableroJuego {

	@FXML
	private GridPane grid_board;
	@FXML
	private Button btn_salir_juego;

	@FXML
	void handleAbandonar(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/vista/MenuPrincipal.fxml"));
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
