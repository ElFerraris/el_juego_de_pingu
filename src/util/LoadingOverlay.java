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
 * Sistema de visualización de estados de carga (Overlay).
 * 
 * <p>
 * Permite inyectar una capa visual sobre cualquier escena activa para indicar
 * al usuario que el sistema está procesando información (ej. guardado o carga
 * de partida). Utiliza una animación desvanecida (fade) y un elemento visual
 * dinámico en la esquina de la pantalla.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class LoadingOverlay {

    /** Contenedor gráfico para el overlay. */
    private static StackPane container;
    /** Componente de visualización para el GIF animado. */
    private static ImageView gifView;
    /** Ruta del recurso visual de carga. */
    private static final String GIF_PATH = "/assets/loading/tongo-dancing.gif";

    /**
     * Muestra el overlay de carga sobre la escena especificada.
     * 
     * @param scene La {@link Scene} donde se debe inyectar el componente.
     */
    public static void show(Scene scene) {
        if (scene != null && (scene.getRoot() instanceof Pane)) {
            Pane root = (Pane) scene.getRoot();

            if (container == null) {
                container = new StackPane();
                container.setMouseTransparent(true); // No bloquea los clics

                try {
                    Image gif = new Image(LoadingOverlay.class.getResourceAsStream(GIF_PATH));
                    gifView = new ImageView(gif);
                    gifView.setFitWidth(100); // Ajustar tamaño según sea necesario
                    gifView.setPreserveRatio(true);

                    StackPane.setAlignment(gifView, Pos.BOTTOM_RIGHT);
                    container.getChildren().add(gifView);
                } catch (Exception e) {
                    System.err.println("Error cargando el GIF: " + e.getMessage());
                }
            }

            if (container != null && !root.getChildren().contains(container)) {
                root.getChildren().add(container);
                container.setOpacity(0);

                FadeTransition ft = new FadeTransition(Duration.millis(300), container);
                ft.setToValue(1.0);
                ft.play();
            }
        }
    }

    /**
     * Oculta el overlay de carga mediante una transición de desvanecimiento suave
     * y lo elimina de la jerarquía de la escena.
     */
    public static void hide() {
        if (container != null && container.getParent() != null) {
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

}
