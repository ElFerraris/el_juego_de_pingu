package controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

import javafx.application.Platform;

/**
 * NavigationController
 * 
 * Gestiona el cambio de escenas (ventanas) en la aplicación.
 * Centraliza la carga de FXML para evitar repetir código en cada botón del
 * menú.
 */
public class NavigationController {

    // Rutas base para las vistas y el estilo
    private static final String VISTA_PATH = "/vista/";
    private static final String CSS_PATH = "/vista/style.css";

    /**
     * Cierra la sesión, limpia el contexto y reinicia la aplicación desde el Login.
     */
    public static void logoutAndRestart(Stage currentStage) {
        System.out.println("► Reiniciando aplicación (Logout)...");

        // 1. Limpiamos el contexto global
        GameContext.getInstance().reset();

        // 2. Cerramos la ventana actual
        currentStage.close();

        // 3. Abrimos una nueva ventana desde cero usando la lógica de Main
        Platform.runLater(() -> {
            try {
                Stage newStage = new Stage();
                new aplicacion.Main().start(newStage);
            } catch (Exception e) {
                System.err.println("Error al reiniciar la aplicación: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Navega a una escena usando el ActionEvent de un botón.
     */
    public static void navigateTo(ActionEvent event, String fxmlFile) {
        navigateTo(event, fxmlFile, true); // Por defecto pantalla completa
    }

    /**
     * Sobrecarga para decidir si queremos pantalla completa o no.
     */
    public static void navigateTo(ActionEvent event, String fxmlFile, boolean fullScreen) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loadAndSet(stage, fxmlFile, fullScreen);
        } catch (IOException e) {
            System.err.println("Error al navegar a: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Navega a una escena usando directamente el Stage.
     */
    public static void navigateTo(Stage stage, String fxmlFile) {
        navigateTo(stage, fxmlFile, true);
    }

    /**
     * Sobrecarga para decidir si queremos pantalla completa o no.
     */
    public static void navigateTo(Stage stage, String fxmlFile, boolean fullScreen) {
        try {
            loadAndSet(stage, fxmlFile, fullScreen);
        } catch (IOException e) {
            System.err.println("Error al navegar a: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Carga el archivo FXML y lo pone en la escena actual.
     */
    private static void loadAndSet(Stage stage, String fxmlFile, boolean fullScreen) throws IOException {
        String fullPath = VISTA_PATH + fxmlFile;
        System.out.println("► Cargando escena: " + fullPath);

        FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fullPath));
        Parent root = loader.load();

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            stage.setScene(scene);
        } else {
            // LIMPIEZA DE ESTILOS PREVIOS: Evita que el CSS del login se mezcle con el del
            // juego
            scene.getStylesheets().clear();
            scene.setRoot(root);
        }

        // Aplicamos el stylesheet específico si existe (por seguridad)
        try {
            String cssPath = NavigationController.class.getResource(CSS_PATH).toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el CSS global en la escena.");
        }

        // Aplicamos efectos visuales automáticos
        applyGlobalEffects(root);

        // Configuramos el modo de pantalla
        stage.setFullScreen(fullScreen);
    }

    /**
     * Busca elementos interactivos en la nueva escena y les pone animaciones.
     */
    private static void applyGlobalEffects(Parent root) {
        if (root == null)
            return;

        // Aplica el efecto de hover a todos los nodos con la clase CSS ".button"
        root.lookupAll(".button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".remove-slot-button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".add-player-button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
    }
}
