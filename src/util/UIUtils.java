package util;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class UIUtils {

    /**
     * Adds a smooth scale animation to nodes when mouse enters/exits.
     * @param nodes The nodes (buttons, cards, etc.) to animate.
     */
    public static void applyHoverAnimation(Node... nodes) {
        for (Node node : nodes) {
            if (node == null) continue;

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), node);
            scaleIn.setToX(1.05);
            scaleIn.setToY(1.05);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), node);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);

            node.setOnMouseEntered(e -> {
                scaleOut.stop();
                scaleIn.playFromStart();
            });

            node.setOnMouseExited(e -> {
                scaleIn.stop();
                scaleOut.playFromStart();
            });
        }
    }
}
