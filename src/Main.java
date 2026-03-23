import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada del juego "El Juego de Pingu".
 * Lanza la interfaz gráfica de usuario JavaFX.
 */
public class Main {

    public static class App extends Application {
        @Override
        public void start(Stage primaryStage) {
            try {
                // Se asume que PantallaMenu.fxml está en el classpath (ej. en la raíz del bin/src)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
                primaryStage.setTitle("El Juego de Pingu");
                primaryStage.setScene(scene);
                primaryStage.setFullScreenExitHint("");
                primaryStage.setFullScreen(true);
                primaryStage.show();
            } catch (Exception e) {
                System.err.println("Error fatal: No se pudo cargar el archivo FXML.");
                System.err.println("Asegúrate de que PantallaMenu.fxml y PantallaJuego.fxml están en la carpeta src o bin.");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
