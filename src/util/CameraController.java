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

        // Listener para restringir el zoom mínimo (fijo en 0.4)
        zoomProperty.addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() < MIN_ZOOM) {
                zoomProperty.set(MIN_ZOOM);
            }
        });

        // Zoom suave con Scroll
        viewport.addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double zoomStep = (delta > 0) ? 1.05 : 0.95;

            double nextZoom = zoomProperty.get() * zoomStep;
            double minZ = getDynamicMinZoom();
            
            if (nextZoom >= minZ && nextZoom <= MAX_ZOOM) {
                zoomProperty.set(nextZoom);
            } else if (nextZoom < minZ) {
                zoomProperty.set(minZ);
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
                autoMode = false;
                if (transition != null) {
                    transition.stop();
                }
                
                // IMPORTANTE: Dividimos por el zoom para que el arrastre sea 1:1 con el ratón
                double s = zoomProperty.get();
                double dx = (event.getSceneX() - mouseAnchorX) / s;
                double dy = (event.getSceneY() - mouseAnchorY) / s;
                
                board.setTranslateX(translateAnchorX + dx);
                board.setTranslateY(translateAnchorY + dy);
                
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
        centerBoardSmooth(0.0); // Centrado instantáneo
    }

    /**
     * Centra el tablero con una animación suave.
     */
    public void centerBoardSmooth(double duration) {
        if (board == null || viewport == null) return;

        double viewWidth = viewport.getWidth();
        double viewHeight = viewport.getHeight();
        if (viewWidth <= 0) viewWidth = 1280;
        if (viewHeight <= 0) viewHeight = 720;

        // Calculamos el centro visual restando el panel lateral derecho (300px) si existe
        double visualCenterX = (viewWidth - 300) / 2.0;
        double visualCenterY = viewHeight / 2.0;

        // Punto central del tablero (ajuste empírico basado en el rombo)
        double targetTX = visualCenterX - 1200;
        double targetTY = visualCenterY - 950;

        if (duration <= 0) {
            board.setTranslateX(targetTX);
            board.setTranslateY(targetTY);
            clampCamera();
        } else {
            if (transition != null) transition.stop();
            transition = new TranslateTransition(Duration.seconds(duration), board);
            transition.setToX(targetTX);
            transition.setToY(targetTY);
            transition.setInterpolator(Interpolator.EASE_BOTH);
            transition.setOnFinished(e -> clampCamera());
            transition.play();
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
     * Restringe el desplazamiento del tablero para que no se pierda del todo.
     * Permite libertad casi total siempre que quede un trozo visible.
     */
    public void clampCamera() {
        if (board == null || viewport == null) return;

        double s = zoomProperty.get();
        double viewW = viewport.getWidth() / s;
        double viewH = viewport.getHeight() / s;

        // Margen mínimo de seguridad (en píxeles del tablero) para no perderlo de vista
        double margin = 200.0; 

        // Límites relajados: el tablero puede salir de pantalla 
        // siempre que el borde opuesto no pase del margen de seguridad.
        double minTX = margin - BG_MAX_X;
        double maxTX = viewW - margin - BG_MIN_X;
        double minTY = margin - BG_MAX_Y;
        double maxTY = viewH - margin - BG_MIN_Y;

        double currentTX = board.getTranslateX();
        double currentTY = board.getTranslateY();

        if (currentTX < minTX) board.setTranslateX(minTX);
        else if (currentTX > maxTX) board.setTranslateX(maxTX);

        if (currentTY < minTY) board.setTranslateY(minTY);
        else if (currentTY > maxTY) board.setTranslateY(maxTY);
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
