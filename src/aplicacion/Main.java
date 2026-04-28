package aplicacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import util.SettingsManager;
import util.SoundManager;

/**
 * Clase principal que inicializa la aplicación JavaFX.
 * 
 * <p>
 * Esta clase se encarga de cargar las fuentes globales, inicializar el
 * gestor de configuración, aplicar los volúmenes de audio guardados y
 * mostrar la pantalla inicial de inicio de sesión (Login).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Main extends Application {

	/**
	 * Punto de entrada de JavaFX.
	 * 
	 * @param primaryStage El escenario principal proporcionado por la plataforma
	 *                     JavaFX.
	 */
	@Override
	public void start(Stage primaryStage) {
		// REGISTRO GLOBAL DE FUENTES
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
			primaryStage.setFullScreen(false);
			// primaryStage.setWidth(1200);
			// primaryStage.setHeight(700);
			primaryStage.centerOnScreen();

			primaryStage.show();

			SoundManager.setSfxVolume(sm.getSfxVolume());
			SoundManager.setMusicVolume(sm.getMusicVolume());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Método principal estándar de Java.
	 * 
	 * @param args Argumentos de línea de comandos pasados a la aplicación.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
