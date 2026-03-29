package util;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utilidades para la Interfaz de Usuario (UI).
 * Contiene animaciones y efectos para que el juego se sienta más dinámico.
 */
public class UIUtils {

    /**
     * Añade una animación suave de escalado cuando el ratón pasa por encima.
     * Ideal para botones y tarjetas de jugador.
     * 
     * @param nodes Los elementos (nodos) a los que aplicar el efecto.
     */
    public static void applyHoverAnimation(Node... nodes) {
        for (Node node : nodes) {
            if (node == null) continue;

            // Animación al entrar (agrandar un 5%)
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), node);
            scaleIn.setToX(1.05);
            scaleIn.setToY(1.05);

            // Animación al salir (volver al tamaño original)
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
