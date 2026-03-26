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
            // Pre-cargar la fuente para que esté disponible globalmente
            try {
            // Registro de fuentes para uso global
            try {
                javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fuentes/upheavtt.ttf"), 12);
                javafx.scene.text.Font grapeSoda = javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fuentes/GrapeSoda.ttf"), 12);
                
                // Fallback si no están en el classpath
                if (grapeSoda == null) {
                    java.io.File grapeFile = new java.io.File("fuentes/GrapeSoda.ttf");
                    if (grapeFile.exists()) {
                        javafx.scene.text.Font.loadFont(grapeFile.toURI().toString(), 12);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error cargando fuentes: " + e.getMessage());
            }
            } catch (Exception e) {
                System.err.println("Error en bloque de fuentes: " + e.getMessage());
            }

            try {
                // Se asume que SplashView.fxml está en el classpath (ej. en /vista/)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/SplashView.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                
                // Verificar carga de CSS
                java.net.URL cssURL = getClass().getResource("/vista/style.css");
                if (cssURL != null) {
                    scene.getStylesheets().add(cssURL.toExternalForm());
                    System.out.println("CSS cargado correctamente: " + cssURL.toExternalForm());
                } else {
                    System.err.println("ERROR: No se encontró /style.css en el classpath.");
                }

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
