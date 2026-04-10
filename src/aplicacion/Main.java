package aplicacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import util.SettingsManager;
import util.SoundManager;

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
			// CARGAR CONFIGURACIÓN
			SettingsManager sm = SettingsManager.getInstance();

			Parent root = FXMLLoader.load(getClass().getResource("/vista/Login.fxml"));
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);

			primaryStage.setResizable(false);
			primaryStage.setFullScreen(sm.isFullscreen());
			// primaryStage.setWidth(1200);
			// primaryStage.setHeight(700);
			primaryStage.centerOnScreen();

			primaryStage.show();

			SoundManager.setVolume(sm.getSfxVolume());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
