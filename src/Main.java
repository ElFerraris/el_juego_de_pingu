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
                javafx.scene.text.Font loadedFont = javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/fuentes/upheavtt.ttf"), 12);
                
                if (loadedFont == null) {
                    System.err.println("Intentando carga alternativa de fuente...");
                    java.io.File fontFile = new java.io.File("fuentes/upheavtt.ttf");
                    if (fontFile.exists()) {
                        loadedFont = javafx.scene.text.Font.loadFont(fontFile.toURI().toString(), 12);
                    }
                }

                if (loadedFont != null) {
                    System.out.println("FUENTE CARGADA CORRECTAMENTE: " + loadedFont.getName() + " (Familia: " + loadedFont.getFamily() + ")");
                } else {
                    System.err.println("ERROR CRÍTICO: No se pudo cargar 'fuentes/upheavtt.ttf' por ningún método.");
                }
                
                // Listado de diagnóstico
                System.out.println("DEBUG - Fuentes con 'Upheaval':");
                javafx.scene.text.Font.getFamilies().stream()
                    .filter(f -> f.toLowerCase().contains("upheaval"))
                    .forEach(f -> System.out.println(" - " + f));

            } catch (Exception e) {
                System.err.println("Excepción en bloque de carga de fuentes: " + e.getMessage());
            }

            try {
                // Se asume que PantallaMenu.fxml está en el classpath (ej. en la raíz del bin/src)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml"));
                Parent root = loader.load();
                
                Scene scene = new Scene(root);
                
                // Verificar carga de CSS
                java.net.URL cssURL = getClass().getResource("/style.css");
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
