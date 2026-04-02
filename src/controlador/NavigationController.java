package controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

import javafx.application.Platform;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

/**
 * NavigationController
 * 
 * Gestiona el cambio de escenas (ventanas) en la aplicación.
 * Centraliza la carga de FXML para evitar repetir código en cada botón del menú.
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

    public enum Direction {
        LEFT, RIGHT, UP, DOWN, NONE
    }

    /**
     * Navega a una escena usando el ActionEvent de un botón con una transición.
     */
    public static void navigateTo(ActionEvent event, String fxmlFile, Direction dir) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loadAndSetWithTransition(stage, fxmlFile, dir);
        } catch (IOException e) {
            System.err.println("Error al navegar a: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Navega a una escena usando el ActionEvent de un botón.
     */
    public static void navigateTo(ActionEvent event, String fxmlFile) {
        navigateTo(event, fxmlFile, Direction.NONE);
    }

    /**
     * Sobrecarga para decidir si queremos pantalla completa o no (sin transición por defecto).
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

    private static void loadAndSetWithTransition(Stage stage, String fxmlFile, Direction dir) throws IOException {
        if (dir == Direction.NONE) {
            loadAndSet(stage, fxmlFile, true);
            return;
        }

        String fullPath = VISTA_PATH + fxmlFile;
        System.out.println("► Cargando escena con transición: " + fullPath);
        
        FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fullPath));
        Parent newRoot = loader.load();
        Scene scene = stage.getScene();

        if (scene == null || !(scene.getRoot() instanceof StackPane) || !(newRoot instanceof StackPane)) {
            loadAndSet(stage, fxmlFile, true);
            return;
        }

        StackPane currentRoot = (StackPane) scene.getRoot();
        StackPane nextRoot = (StackPane) newRoot;

        // Comprobamos si ambos tienen el mismo tipo de fondo (por la clase CSS)
        boolean sameBg = currentRoot.getStyleClass().contains("root-plain-bg") && 
                        nextRoot.getStyleClass().contains("root-plain-bg");

        if (!sameBg) {
            loadAndSet(stage, fxmlFile, true);
            return;
        }

        // Buscamos el panel de contenido ("glass-panel")
        Node oldContent = null;
        for (Node n : currentRoot.getChildren()) {
            if (n.getStyleClass().contains("glass-panel")) {
                oldContent = n;
                break;
            }
        }

        Node newContent = null;
        for (Node n : nextRoot.getChildren()) {
            if (n.getStyleClass().contains("glass-panel")) {
                newContent = n;
                break;
            }
        }

        if (oldContent == null || newContent == null) {
            loadAndSet(stage, fxmlFile, true);
            return;
        }

        // Preparamos la escena para el nuevo contenido
        currentRoot.getChildren().add(newContent);
        applyGlobalEffects(newContent);

        double width = scene.getWidth();
        double height = scene.getHeight();

        // Configuración de la animación con suavizado (Ease Both)
        TranslateTransition out = new TranslateTransition(Duration.millis(500), oldContent);
        out.setInterpolator(Interpolator.EASE_BOTH);
        
        TranslateTransition in = new TranslateTransition(Duration.millis(500), newContent);
        in.setInterpolator(Interpolator.EASE_BOTH);

        switch (dir) {
            case LEFT:
                newContent.setTranslateX(width);
                out.setToX(-width);
                in.setToX(0);
                break;
            case RIGHT:
                newContent.setTranslateX(-width);
                out.setToX(width);
                in.setToX(0);
                break;
            case UP:
                newContent.setTranslateY(height);
                out.setToY(-height);
                in.setToY(0);
                break;
            case DOWN:
                newContent.setTranslateY(-height);
                out.setToY(height);
                in.setToY(0);
                break;
            default:
                break;
        }

        final Node contentToMoveBack = newContent;
        final Node contentToRemove = oldContent;
        ParallelTransition pt = new ParallelTransition(out, in);
        pt.setOnFinished(e -> {
            // Limpieza: quitamos el nuevo contenido del root viejo
            currentRoot.getChildren().remove(contentToMoveBack);
            currentRoot.getChildren().remove(contentToRemove);
            
            // IMPORTANTE: Devolvemos el contenido a su root original (nextRoot) 
            // antes de cambiar el root de la escena, para que no desaparezca.
            contentToMoveBack.setTranslateX(0);
            contentToMoveBack.setTranslateY(0);
            nextRoot.getChildren().add(contentToMoveBack);
            
            // Ahora sí, cambiamos el root de la escena de forma definitiva
            scene.setRoot(nextRoot);
        });
        pt.play();
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
            // LIMPIEZA DE ESTILOS PREVIOS
            scene.getStylesheets().clear();
            scene.setRoot(root);
        }

        try {
            String cssPath = NavigationController.class.getResource(CSS_PATH).toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {}
        
        applyGlobalEffects(root);
        stage.setFullScreen(fullScreen);
    }

    /**
     * Busca elementos interactivos en la nueva escena y les pone animaciones.
     */
    private static void applyGlobalEffects(Parent root) {
        if (root == null) return;
        root.lookupAll(".button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".remove-slot-button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".add-player-button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
    }

    private static void applyGlobalEffects(Node node) {
        if (node instanceof Parent) {
            applyGlobalEffects((Parent) node);
        }
    }
}
