package aplicacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		// REGISTRO GLOBAL DE FUENTES (Andrei Style)
		try {
			Font.loadFont(getClass().getResourceAsStream("/assets/fuentes/GrapeSoda.ttf"), 12);
			Font.loadFont(getClass().getResourceAsStream("/assets/fuentes/upheavtt.ttf"), 12);
		} catch (Exception e) {
			System.err.println("No se pudieron cargar las fuentes desde Main: " + e.getMessage());
		}

		try {
			// BorderPane root = new BorderPane();
			Parent root = FXMLLoader.load(getClass().getResource("/vista/Login.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
