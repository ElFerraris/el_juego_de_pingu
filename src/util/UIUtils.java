package util;

import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Colección de herramientas auxiliares para la interfaz de usuario (UI).
 * 
 * <p>
 * Proporciona métodos para la traducción de nombres de colores a objetos
 * {@link Color} de JavaFX y para la aplicación de micro-animaciones de
 * interactividad, como el escalado por proximidad del cursor (hover).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class UIUtils {

    /**
     * Convierte un nombre de color (String) al objeto Color de JavaFX.
     */
    public static Color colorDesdeNombre(String nombre) {
        if (nombre == null)
            return Color.GRAY;
        switch (nombre.toLowerCase()) {
            case "rojo":
                return Color.RED;
            case "azul":
                return Color.BLUE;
            case "verde":
                return Color.GREEN;
            case "amarillo":
                return Color.YELLOW;
            case "naranja":
                return Color.ORANGE;
            case "morado":
                return Color.PURPLE;
            case "rosa":
                return Color.PINK;
            default:
                return Color.GRAY;
        }
    }

    /**
     * Añade una animación suave de escalado cuando el ratón pasa por encima.
     * Ideal para botones y tarjetas de jugador.
     * 
     * @param nodes Los elementos (nodos) a los que aplicar el efecto.
     */
    public static void applyHoverAnimation(Node... nodes) {
        for (Node node : nodes) {
            if (node != null) {
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
                    SoundManager.playHover(); // Nuevo: Sonido al pasar el ratón
                });

                node.setOnMouseExited(e -> {
                    scaleIn.stop();
                    scaleOut.playFromStart();
                });
            }
        }
    }
}
