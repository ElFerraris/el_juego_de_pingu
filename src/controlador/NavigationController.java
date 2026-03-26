package controlador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;

public class NavigationController {

    private static final String VISTA_PATH = "/vista/";
    private static final String CSS_PATH = "/vista/style.css";

    public static void navigateTo(ActionEvent event, String fxmlFile) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loadAndSet(stage, fxmlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void navigateTo(Stage stage, String fxmlFile) {
        try {
            loadAndSet(stage, fxmlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadAndSet(Stage stage, String fxmlFile) throws IOException {
        String fullPath = VISTA_PATH + fxmlFile;
        System.out.println("Navegando a: " + fullPath);
        
        FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fullPath));
        Parent root = loader.load();
        
        Scene scene = stage.getScene();
        if (scene == null) {
            // First time navigation or if Main hasn't set it
            scene = new Scene(root);
            scene.getStylesheets().add(NavigationController.class.getResource(CSS_PATH).toExternalForm());
            stage.setScene(scene);
        } else {
            // Seamless transition by changing the root
            scene.setRoot(root);
        }
        
        // APPLY GLOBAL TRANSITIONS
        applyGlobalTransitions(root);
        
        stage.setFullScreen(true);
    }

    private static void applyGlobalTransitions(Parent root) {
        if (root == null) return;
        
        // Find and animate all buttons and interactive elements
        root.lookupAll(".button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".btn-inv").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".remove-slot-button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
        root.lookupAll(".add-player-button").forEach(node -> util.UIUtils.applyHoverAnimation(node));
    }
}
