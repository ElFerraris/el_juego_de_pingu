package util;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.Map;
import java.util.List;
import modelo.Jugador;

/**
 * Gestor de la cámara para el tablero de juego.
 * 
 * <p>
 * Se encarga de las funcionalidades de Zoom (rueda del ratón), 
 * Pan (arrastre manual) y Centrado Suave sobre jugadores o el tablero completo.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class CameraController {

    private final Pane viewport;
    private final Pane board;
    private final Group zoomGroup;
    private final Map<Integer, StackPane> cellNodes;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    
    private double zoomFactor = 1.0;
    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 3.0;

    private boolean autoMode = true;
    private TranslateTransition transition;

    /**
     * Constructor del gestor de cámara.
     * 
     * @param viewport Contenedor que detecta los eventos de ratón.
     * @param board    Panel que contiene el tablero y se desplaza.
     * @param zoomGroup Grupo que escala el contenido para el efecto de zoom.
     * @param cellNodes Mapa de nodos de casilla para posicionamiento.
     */
    public CameraController(Pane viewport, Pane board, Group zoomGroup, Map<Integer, StackPane> cellNodes) {
        this.viewport = viewport;
        this.board = board;
        this.zoomGroup = zoomGroup;
        this.cellNodes = cellNodes;
    }

    /**
     * Inicializa los listeners de eventos para el control manual.
     */
    public void init() {
        // Zoom suave con Scroll
        viewport.addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double zoomStep = (delta > 0) ? 1.1 : 0.9;

            // Calculamos el zoom mínimo necesario para que el fondo siga cubriendo la pantalla
            Bounds b = board.getBoundsInLocal();
            double minZoomX = viewport.getWidth() / b.getWidth();
            double minZoomY = viewport.getHeight() / b.getHeight();
            double minZ = Math.max(minZoomX, minZoomY);
            if (minZ < 0.1 || Double.isNaN(minZ)) minZ = 0.4; // Salvaguarda si no hay datos

            double nextZoom = zoomFactor * zoomStep;
            if (nextZoom >= minZ && nextZoom <= MAX_ZOOM) {
                zoomFactor = nextZoom;
                zoomGroup.setScaleX(zoomFactor);
                zoomGroup.setScaleY(zoomFactor);
                aplicarLimites(); // Corregir posición si el zoom nos saca de límites
            }
            event.consume();
        });

        // Desplazamiento (Pan) con Arrastre
        viewport.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
                translateAnchorX = board.getTranslateX();
                translateAnchorY = board.getTranslateY();
                viewport.setCursor(javafx.scene.Cursor.MOVE);
            }
        });

        viewport.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                autoMode = false; // El usuario toma el control manual
                if (transition != null) {
                    transition.stop();
                }
                board.setTranslateX(translateAnchorX + (event.getSceneX() - mouseAnchorX));
                board.setTranslateY(translateAnchorY + (event.getSceneY() - mouseAnchorY));
                aplicarLimites();
            }
        });

        viewport.setOnMouseReleased(event -> {
            viewport.setCursor(javafx.scene.Cursor.DEFAULT);
        });
    }

    /**
     * Centra el tablero completo en el centro visual del viewport.
     */
    public void centerBoard() {
        if (board != null && viewport != null) {
            double viewWidth = viewport.getWidth();
            double viewHeight = viewport.getHeight();

            if (viewWidth <= 0) viewWidth = 1280;
            if (viewHeight <= 0) viewHeight = 720;

            // Reseteamos zoom
            zoomFactor = 1.0;
            zoomGroup.setScaleX(1.0);
            zoomGroup.setScaleY(1.0);

            // Centro visual
            double visualCenterX = viewWidth / 2.0;
            double visualCenterY = viewHeight / 2.0;

            // Ajuste empírico para el centro del rombo isométrico
            board.setTranslateX(visualCenterX - 1200);
            board.setTranslateY(visualCenterY - 950);
            
            aplicarLimites(); // Asegurar que el centrado inicial no rompa los límites
        }
    }

    /**
     * Centra la cámara suavemente sobre un jugador.
     * 
     * @param j               El jugador objetivo.
     * @param durationSeconds Duración de la animación.
     */
    public void smoothCenterOnPlayer(Jugador j, double durationSeconds) {
        if (board != null && viewport != null && j != null) {
            StackPane cell = cellNodes.get(j.getPosicion());
            if (cell != null) {
                double viewWidth = viewport.getWidth();
                double viewHeight = viewport.getHeight();
                if (viewWidth <= 0) viewWidth = 1280;
                if (viewHeight <= 0) viewHeight = 720;

                // Centro visual restando el panel lateral derecho (300px)
                double visualCenterX = (viewWidth - 300) / 2.0;
                double visualCenterY = viewHeight / 2.0;

                double targetX = cell.getLayoutX() + (cell.getPrefWidth() / 2.0);
                double targetY = cell.getLayoutY() + (cell.getPrefHeight() / 2.0);

                double newTX = visualCenterX - targetX;
                double newTY = visualCenterY - targetY;

                if (transition != null) transition.stop();

                transition = new TranslateTransition(Duration.seconds(durationSeconds), board);
                transition.setToX(newTX);
                transition.setToY(newTY);
                transition.setInterpolator(Interpolator.EASE_BOTH);
                transition.setOnFinished(e -> aplicarLimites()); // Validar límites al terminar la animación
                transition.play();
            }
        }
    }

    /**
     * Restringe el movimiento del tablero para que el fondo siempre cubra el viewport.
     */
    private void aplicarLimites() {
        if (board == null || viewport == null) return;

        double z = zoomFactor;
        double vw = viewport.getWidth();
        double vh = viewport.getHeight();

        // Si el viewport aún no tiene tamaño (inicio), usamos valores por defecto
        if (vw <= 0) vw = 1280;
        if (vh <= 0) vh = 720;

        // Obtenemos los límites reales del contenido (incluyendo el fondo ampliado)
        Bounds b = board.getBoundsInLocal();
        
        // Si el tablero está vacío o no ha cargado, no aplicamos límites aún
        if (b.getWidth() < 100) return; 

        // El contenido visual mide (b.width * z)
        // La restricción es: (Translate * z) + (Bound * z) debe cubrir el viewport [0, vw]
        
        double minTX = (vw / z) - b.getMaxX();
        double maxTX = -b.getMinX();
        
        double minTY = (vh / z) - b.getMaxY();
        double maxTY = -b.getMinY();

        // Si el fondo es más pequeño que el viewport (mucho zoom out), centramos
        if (minTX > maxTX) {
            board.setTranslateX((maxTX + minTX) / 2.0);
        } else {
            if (board.getTranslateX() < minTX) board.setTranslateX(minTX);
            if (board.getTranslateX() > maxTX) board.setTranslateX(maxTX);
        }

        if (minTY > maxTY) {
            board.setTranslateY((maxTY + minTY) / 2.0);
        } else {
            if (board.getTranslateY() < minTY) board.setTranslateY(minTY);
            if (board.getTranslateY() > maxTY) board.setTranslateY(maxTY);
        }
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }
}
