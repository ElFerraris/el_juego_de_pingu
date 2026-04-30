package util;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
    
    private final javafx.beans.property.DoubleProperty zoomProperty = new javafx.beans.property.SimpleDoubleProperty(1.0);
    public static final double MIN_ZOOM = 0.4;
    public static final double MAX_ZOOM = 3.0;

    // Límites lógicos del fondo (calculados según el escalado de TableroController - escala 3640)
    private static final double BG_MIN_X = -640.0;
    private static final double BG_MIN_Y = -312.0;
    private static final double BG_MAX_X = 3000.0;
    private static final double BG_MAX_Y = 2412.0;

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
        // Vinculamos la escala visual a la propiedad
        zoomGroup.scaleXProperty().bind(zoomProperty);
        zoomGroup.scaleYProperty().bind(zoomProperty);

        // Listener para restringir el zoom y la posición cuando cambie la escala
        zoomProperty.addListener((obs, oldVal, newVal) -> {
            double minZ = getDynamicMinZoom();
            if (newVal.doubleValue() < minZ - 0.001) { // Pequeño margen para evitar loops
                zoomProperty.set(minZ);
            }
            // Usamos un pequeño delay para asegurar que el motor de layout ha terminado
            Platform.runLater(this::clampCamera);
        });

        // Zoom suave con Scroll
        viewport.addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double zoomStep = (delta > 0) ? 1.05 : 0.95; // Pasos un poco más suaves

            double nextZoom = zoomProperty.get() * zoomStep;
            double minZ = getDynamicMinZoom();
            
            if (nextZoom >= minZ && nextZoom <= MAX_ZOOM) {
                zoomProperty.set(nextZoom);
            } else if (nextZoom < minZ) {
                zoomProperty.set(minZ);
            }
            event.consume();
            clampCamera(); // Clamp inmediato tras scroll
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
                
                clampCamera();
            }
        });

        viewport.setOnMouseReleased(event -> {
            viewport.setCursor(javafx.scene.Cursor.DEFAULT);
            clampCamera();
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
            zoomProperty.set(1.0);

            // Centro visual
            double visualCenterX = viewWidth / 2.0;
            double visualCenterY = viewHeight / 2.0;

            // Ajuste empírico para el centro del rombo isométrico
            board.setTranslateX(visualCenterX - 1200);
            board.setTranslateY(visualCenterY - 950);
            
            clampCamera();
        }
    }

    /**
     * Calcula el zoom mínimo dinámico para que el fondo siempre cubra el viewport.
     */
    private double getDynamicMinZoom() {
        if (viewport == null) return MIN_ZOOM;
        double viewW = viewport.getWidth();
        double viewH = viewport.getHeight();
        if (viewW <= 0) viewW = 1280;
        if (viewH <= 0) viewH = 720;

        double bgW = BG_MAX_X - BG_MIN_X;
        double bgH = BG_MAX_Y - BG_MIN_Y;

        double minZoomX = viewW / bgW;
        double minZoomY = viewH / bgH;

        return Math.max(MIN_ZOOM, Math.max(minZoomX, minZoomY));
    }

    /**
     * Restringe el desplazamiento del tablero para que el fondo no deje huecos.
     */
    public void clampCamera() {
        if (board == null || viewport == null || board.getScene() == null) return;

        // Forzamos actualización de layout para tener coordenadas frescas
        viewport.layout();
        board.layout();

        Point2D topLeft = board.localToScene(BG_MIN_X, BG_MIN_Y);
        Point2D bottomRight = board.localToScene(BG_MAX_X, BG_MAX_Y);
        Point2D viewTopLeft = viewport.localToScene(0, 0);
        Point2D viewBottomRight = viewport.localToScene(viewport.getWidth(), viewport.getHeight());

        if (topLeft == null || viewTopLeft == null || bottomRight == null || viewBottomRight == null) return;

        double dx = 0;
        double dy = 0;
        double viewW = viewBottomRight.getX() - viewTopLeft.getX();
        double viewH = viewBottomRight.getY() - viewTopLeft.getY();
        double bgW = bottomRight.getX() - topLeft.getX();
        double bgH = bottomRight.getY() - topLeft.getY();

        // --- LÓGICA HORIZONTAL ---
        if (bgW <= viewW + 1) {
            // Si el fondo es más pequeño o igual que la vista, lo centramos
            dx = (viewTopLeft.getX() + viewW / 2.0) - (topLeft.getX() + bgW / 2.0);
        } else {
            // Si es más grande, impedimos que se vean los bordes
            if (topLeft.getX() > viewTopLeft.getX()) {
                dx = viewTopLeft.getX() - topLeft.getX();
            } else if (bottomRight.getX() < viewBottomRight.getX()) {
                dx = viewBottomRight.getX() - bottomRight.getX();
            }
        }

        // --- LÓGICA VERTICAL ---
        if (bgH <= viewH + 1) {
            // Si el fondo es más pequeño o igual que la vista, lo centramos
            dy = (viewTopLeft.getY() + viewH / 2.0) - (topLeft.getY() + bgH / 2.0);
        } else {
            // Si es más grande, impedimos que se vean los bordes
            if (topLeft.getY() > viewTopLeft.getY()) {
                dy = viewTopLeft.getY() - topLeft.getY();
            } else if (bottomRight.getY() < viewBottomRight.getY()) {
                dy = viewBottomRight.getY() - bottomRight.getY();
            }
        }

        if (Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01) {
            double S = zoomProperty.get();
            board.setTranslateX(board.getTranslateX() + dx / S);
            board.setTranslateY(board.getTranslateY() + dy / S);
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
                transition.play();
            }
        }
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }

    public javafx.beans.property.DoubleProperty zoomProperty() {
        return zoomProperty;
    }
}
