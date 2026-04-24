package util;

import javafx.animation.FadeTransition;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Utility to show a loading GIF in the bottom right corner of the screen.
 */
public class LoadingOverlay {

    private static StackPane container;
    private static ImageView gifView;
    private static final String GIF_PATH = "/assets/loading/tongo-dancing.gif";

    public static void show(Scene scene) {
        if (scene == null || !(scene.getRoot() instanceof Pane)) return;
        Pane root = (Pane) scene.getRoot();

        if (container == null) {
            container = new StackPane();
            container.setMouseTransparent(true); // Don't block clicks if possible
            
            try {
                Image gif = new Image(LoadingOverlay.class.getResourceAsStream(GIF_PATH));
                gifView = new ImageView(gif);
                gifView.setFitWidth(100); // Fit size as needed
                gifView.setPreserveRatio(true);
                
                StackPane.setAlignment(gifView, Pos.BOTTOM_RIGHT);
                container.getChildren().add(gifView);
            } catch (Exception e) {
                System.err.println("Error loading GIF: " + e.getMessage());
                return;
            }
        }

        if (!root.getChildren().contains(container)) {
            root.getChildren().add(container);
            container.setOpacity(0);
            
            FadeTransition ft = new FadeTransition(Duration.millis(300), container);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    public static void hide() {
        if (container == null || container.getParent() == null) return;

        FadeTransition ft = new FadeTransition(Duration.millis(300), container);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            Pane parent = (Pane) container.getParent();
            if (parent != null) {
                parent.getChildren().remove(container);
            }
        });
        ft.play();
    }

}
