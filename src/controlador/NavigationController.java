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
        LEFT, RIGHT, UP, DOWN, FORWARD, BACKWARD, TO_BOARD, NONE
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

        // Ahora permitimos cualquier Pane (StackPane, BorderPane, etc.) para la animación
        if (scene == null || !(scene.getRoot() instanceof javafx.scene.layout.Pane) || !(newRoot instanceof javafx.scene.layout.Pane)) {
            loadAndSet(stage, fxmlFile, true);
            return;
        }

        javafx.scene.layout.Pane currentRoot = (javafx.scene.layout.Pane) scene.getRoot();
        javafx.scene.layout.Pane nextRoot = (javafx.scene.layout.Pane) newRoot;

        // Comprobamos si ambos tienen el mismo tipo de fondo (por la clase CSS)
        boolean sameBg = currentRoot.getStyleClass().contains("root-plain-bg") && 
                        nextRoot.getStyleClass().contains("root-plain-bg");

        if (!sameBg) {
            loadAndSet(stage, fxmlFile, true);
            return;
        }

        // Aseguramos que el CSS esté cargado en la escena
        ensureCssLoaded(scene);

        // Si es hacia el tablero, no extraemos el "glass-panel" para no romper el BorderPane
        if (dir == Direction.TO_BOARD) {
            // Animamos la raíz completa del tablero
            currentRoot.getChildren().add(newRoot);
            newRoot.setOpacity(0);
            applyGlobalEffects(newRoot, fxmlFile);
            
            // Buscamos el contenido viejo para deslizarlo
            Node oldContentToSlide = null;
            for (Node n : currentRoot.getChildren()) {
                if (n.getStyleClass().contains("glass-panel")) {
                    oldContentToSlide = n;
                    break;
                }
            }
            
            double height = scene.getHeight();
            ParallelTransition pt = new ParallelTransition();
            pt.setInterpolator(Interpolator.EASE_BOTH);
            
            // El menú cae
            if (oldContentToSlide != null) {
                TranslateTransition slideDown = new TranslateTransition(Duration.millis(500), oldContentToSlide);
                slideDown.setToY(height);
                pt.getChildren().add(slideDown);
            }
            
            // El tablero funde
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), newRoot);
            fadeIn.setToValue(1.0);
            pt.getChildren().add(fadeIn);
            
            pt.setOnFinished(e -> {
                currentRoot.getChildren().remove(newRoot);
                scene.setRoot(newRoot);
                newRoot.setOpacity(1.0);
                newRoot.layout(); // Forzamos recalculado de layout
            });
            pt.play();
            return;
        }

        // --- LÓGICA ESTÁNDAR PARA MENÚS (glass-panel) ---
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
        applyGlobalEffects(newContent, fxmlFile);

        double width = scene.getWidth();
        double height = scene.getHeight();

        // Configuración de las animaciones (usando ParallelTransition para combinar efectos)
        ParallelTransition pt = new ParallelTransition();
        pt.setInterpolator(Interpolator.EASE_BOTH);

        switch (dir) {
            case LEFT:
                newContent.setTranslateX(width);
                TranslateTransition outL = new TranslateTransition(Duration.millis(350), oldContent);
                outL.setToX(-width);
                TranslateTransition inL = new TranslateTransition(Duration.millis(350), newContent);
                inL.setToX(0);
                pt.getChildren().addAll(outL, inL);
                break;
            case RIGHT:
                newContent.setTranslateX(-width);
                TranslateTransition outR = new TranslateTransition(Duration.millis(350), oldContent);
                outR.setToX(width);
                TranslateTransition inR = new TranslateTransition(Duration.millis(350), newContent);
                inR.setToX(0);
                pt.getChildren().addAll(outR, inR);
                break;
            case FORWARD:
                newContent.setTranslateY(height);
                newContent.setOpacity(0);
                
                TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), newContent);
                slideUp.setToY(0);
                
                FadeTransition fadeInNew = new FadeTransition(Duration.millis(400), newContent);
                fadeInNew.setToValue(1.0);
                
                FadeTransition fadeOutOld = new FadeTransition(Duration.millis(300), oldContent);
                fadeOutOld.setToValue(0.0);
                
                pt.getChildren().addAll(slideUp, fadeInNew, fadeOutOld);
                break;
            case BACKWARD:
                newContent.setOpacity(0);
                newContent.setTranslateY(0);
                
                TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), oldContent);
                slideDown.setToY(height);
                
                FadeTransition fadeInOlder = new FadeTransition(Duration.millis(400), newContent);
                fadeInOlder.setToValue(1.0);
                
                pt.getChildren().addAll(slideDown, fadeInOlder);
                break;
            case UP:
                newContent.setTranslateY(height);
                TranslateTransition outU = new TranslateTransition(Duration.millis(350), oldContent);
                outU.setToY(-height);
                TranslateTransition inU = new TranslateTransition(Duration.millis(350), newContent);
                inU.setToY(0);
                pt.getChildren().addAll(outU, inU);
                break;
            case DOWN:
                newContent.setTranslateY(-height);
                TranslateTransition outD = new TranslateTransition(Duration.millis(350), oldContent);
                outD.setToY(height);
                TranslateTransition inD = new TranslateTransition(Duration.millis(350), newContent);
                inD.setToY(0);
                pt.getChildren().addAll(outD, inD);
                break;
            case TO_BOARD:
                // Esta rama ya no se debería alcanzar por el 'return' de arriba, 
                // pero la mantenemos por coherencia o por si se refactoriza.
                break;
            default:
                break;
        }

        final Node contentToMoveBack = newContent;
        final Node contentToRemove = oldContent;
        pt.setOnFinished(e -> {
            // Caso estándar de menús (glass-panel)
            currentRoot.getChildren().remove(contentToMoveBack);
            currentRoot.getChildren().remove(contentToRemove);
            
            contentToMoveBack.setTranslateX(0);
            contentToMoveBack.setTranslateY(0);
            contentToMoveBack.setOpacity(1.0);
            nextRoot.getChildren().add(contentToMoveBack);
            
            scene.setRoot(nextRoot);
            nextRoot.layout();
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

        ensureCssLoaded(scene);
        applyGlobalEffects(root, fxmlFile);
        stage.setFullScreen(fullScreen);
    }

    /**
     * Garantiza que el archivo de estilos principal esté cargado en la escena.
     */
    private static void ensureCssLoaded(Scene scene) {
        if (scene == null) return;
        try {
            String cssPath = NavigationController.class.getResource(CSS_PATH).toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("Error cargando CSS: " + e.getMessage());
        }
    }

    /**
     * Busca elementos interactivos en la nueva escena y les pone animaciones y sonidos.
     */
    private static void applyGlobalEffects(Parent root, String fxmlFile) {
        if (root == null) return;

        // Filtramos para NO aplicar estos sonidos en Login o Tablero
        boolean menuSoundsEnabled = fxmlFile != null && 
                                   !fxmlFile.equalsIgnoreCase("LoginView.fxml") && // Ajustar según nombre real
                                   !fxmlFile.equalsIgnoreCase("TableroJuego.fxml") &&
                                   !fxmlFile.equalsIgnoreCase("Intro.fxml");

        root.lookupAll(".button").forEach(node -> {
            util.UIUtils.applyHoverAnimation(node);
            if (menuSoundsEnabled) attachMenuSounds(node);
        });

        root.lookupAll(".remove-slot-button").forEach(node -> {
            util.UIUtils.applyHoverAnimation(node);
            if (menuSoundsEnabled) attachMenuSounds(node);
        });

        root.lookupAll(".add-player-button").forEach(node -> {
            util.UIUtils.applyHoverAnimation(node);
            if (menuSoundsEnabled) attachMenuSounds(node);
        });
    }

    private static void attachMenuSounds(Node node) {
        node.setOnMouseClicked(e -> {
            String text = "";
            if (node instanceof javafx.scene.control.Button) {
                text = ((javafx.scene.control.Button) node).getText().toUpperCase();
            }

            // CASOS ESPECIALES POR TEXTO (Prioridad)
            if (text.equals("SALIR") || text.contains("SÍ") || text.contains("CONFIRMAR")) {
                util.SoundManager.playConfirm();
            } 
            else if (text.contains("VOLVER") || text.contains("ATRÁS") || text.contains("NO")) {
                util.SoundManager.playBack();
            }
            // CASOS POR CLASE CSS (Segunda opción)
            else if (node.getStyleClass().contains("button-primary") || 
                     node.getStyleClass().contains("button-danger") ||
                     node.getStyleClass().contains("add-player-button")) {
                util.SoundManager.playConfirm();
            } 
            else if (node.getStyleClass().contains("button-secondary") || 
                       node.getStyleClass().contains("remove-slot-button")) {
                util.SoundManager.playBack();
            } 
            else {
                // Sonido por defecto si no tiene clase específica
                util.SoundManager.playConfirm();
            }
        });
    }

    private static void applyGlobalEffects(Node node, String fxmlFile) {
        if (node instanceof Parent) {
            applyGlobalEffects((Parent) node, fxmlFile);
        }
    }
}
