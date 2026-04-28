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
import util.LoadingOverlay;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * <h1>NavigationController</h1>
 * 
 * <p>
 * El "cerebro" de la navegación de la aplicación. Centraliza la carga de
 * archivos FXML,
 * la gestión de escenas (Scenes) y el control de ventanas (Stages).
 * </p>
 * 
 * <p>
 * Sus responsabilidades principales incluyen:
 * </p>
 * <ul>
 * <li>Cargar vistas con transiciones animadas (desplazamientos, fundidos).</li>
 * <li>Gestionar la carga asíncrona del tablero para no bloquear la
 * interfaz.</li>
 * <li>Controlar el modo pantalla completa y los atajos de teclado
 * globales.</li>
 * <li>Aplicar efectos visuales y de sonido automáticos a los componentes de la
 * interfaz.</li>
 * </ul>
 * 
 * <h1>Observaciones</h1>
 * 
 * <p>
 * Esta clase utiliza una cosa llamada Sobrecarga de Métodos (o Method
 * Overloading en inglés). Donde
 * basicamente creamos varios metodos con el mismo nombre pero con diferentes
 * parametros, porque para
 * java no es lo mismo metodo(5) que metodo("hola"), pudiendo gestionar muy
 * biien ciertas cosas del
 * proyecto.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class NavigationController {

    /** Ruta base dentro de los recursos donde se encuentran los archivos .fxml */
    private static final String VISTA_PATH = "/vista/";

    /** Ruta del archivo CSS principal */
    private static final String CSS_PATH = "/vista/style.css";

    /** Marca de tiempo para controlar el spam de teclas de pantalla completa */
    private static long lastToggleTime = 0;

    /** Tiempo de espera (ms) entre cambios de pantalla completa */
    private static final long TOGGLE_COOLDOWN = 300;

    /**
     * Cierra la sesión, limpia el estado del juego y reinicia la aplicación.
     * 
     * @param currentStage El Stage actual que debe cerrarse.
     */
    public static void logoutAndRestart(Stage currentStage) {
        System.out.println("► Reiniciando aplicación (Logout)...");

        // 1. Limpiamos el contexto global para borrar jugadores, partida actual, etc.
        GameContext.getInstance().reset();

        // 2. Cerramos la ventana de juego actual
        currentStage.close();

        // 3. Lanzamos una nueva instancia de la aplicación directamente
        try {
            Stage newStage = new Stage();
            newStage.setResizable(false);
            new aplicacion.Main().start(newStage);
        } catch (Exception e) {
            System.err.println("Error al reiniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Direcciones posibles para las transiciones entre pantallas.
     */
    public static class Direction {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int UP = 2;
        public static final int DOWN = 3;
        public static final int FORWARD = 4;
        public static final int BACKWARD = 5;
        public static final int TO_BOARD = 6;
        public static final int NONE = 7;
    }

    /**
     * Cambia a una nueva escena con una transición animada.
     * 
     * @param event    El evento del botón que disparó la navegación.
     * @param fxmlFile Nombre del archivo .fxml (ej: "MainMenuView.fxml").
     * @param dir      La dirección de la animación.
     */
    public static void navigateTo(ActionEvent event, String fxmlFile, int dir) {
        try {
            // Obtenemos la ventana (Stage) a partir del componente que disparó el evento
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            loadAndSetWithTransition(stage, fxmlFile, dir);
        } catch (IOException e) {
            System.err.println("Error al navegar a: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Cambia a una nueva escena sin transición animada.
     * 
     * @param event    El evento del botón que disparó la navegación.
     * @param fxmlFile Nombre del archivo .fxml.
     */
    public static void navigateTo(ActionEvent event, String fxmlFile) {
        navigateTo(event, fxmlFile, Direction.NONE);
    }

    /**
     * Navega a una escena permitiendo forzar el modo pantalla completa.
     * 
     * @param event      El evento del botón.
     * @param fxmlFile   Nombre del archivo .fxml.
     * @param fullScreen True para activar pantalla completa, false para modo
     *                   ventana.
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
     * Navega a una escena directamente usando el Stage (sin evento de botón).
     * 
     * @param stage    La ventana principal.
     * @param fxmlFile Nombre del archivo .fxml.
     */
    public static void navigateTo(Stage stage, String fxmlFile) {
        navigateTo(stage, fxmlFile, true);
    }

    /**
     * Navega a una escena usando el Stage y definiendo el modo de pantalla.
     */
    public static void navigateTo(Stage stage, String fxmlFile, boolean fullScreen) {
        try {
            loadAndSet(stage, fxmlFile, fullScreen);
        } catch (IOException e) {
            System.err.println("Error al navegar a: " + fxmlFile);
            e.printStackTrace();
        }
    }

    /**
     * Carga el tablero de forma síncrona.
     * 
     * <p>
     * NOTA: Se ha eliminado la carga asíncrona por requisitos del proyecto.
     * </p>
     * 
     * @param event    El evento que dispara la carga.
     * @param fxmlFile El archivo del tablero.
     */
    public static void navigateToBoardAsync(ActionEvent event, String fxmlFile) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            String fullPath = VISTA_PATH + fxmlFile;
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fullPath));
            Parent root = loader.load();

            Scene scene = stage.getScene();
            scene.setRoot(root);
            ensureCssLoaded(scene);
            applyGlobalEffects(root, fxmlFile);

            stage.setFullScreen(util.SettingsManager.getInstance().isFullscreen());
            setupGlobalHotkeys(scene);
            System.out.println("► Tablero cargado síncronamente.");
        } catch (Exception e) {
            System.err.println("► Error en carga síncrona: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lógica interna para manejar las transiciones animadas entre escenas.
     * 
     * <p>
     * Detecta si las vistas usan la estructura de "glass-panel" para animar
     * solo el contenido interno y no el fondo.
     * </p>
     * 
     * @param stage    La ventana principal en la que se realiza la transición.
     * @param fxmlFile Nombre del archivo de la vista destino a cargar.
     * @param dir      La dirección de la transición (ej. Direction.RIGHT).
     * @throws IOException Si ocurre un error al intentar leer el archivo FXML de
     *                     destino.
     */
    private static void loadAndSetWithTransition(Stage stage, String fxmlFile, int dir) throws IOException {
        // Si no hay dirección, cargamos de forma normal
        if (dir == Direction.NONE) {
            loadAndSet(stage, fxmlFile, util.SettingsManager.getInstance().isFullscreen());
        } else {
            String fullPath = VISTA_PATH + fxmlFile;
            System.out.println("► Cargando escena con transición: " + fullPath);

            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fullPath));
            Parent newRoot = loader.load();
            Scene scene = stage.getScene();

            // Verificamos que tanto la escena actual como la nueva sean válidas para animar
            if (scene == null || !(scene.getRoot() instanceof javafx.scene.layout.Pane)
                    || !(newRoot instanceof javafx.scene.layout.Pane)) {
                loadAndSet(stage, fxmlFile, util.SettingsManager.getInstance().isFullscreen());
            } else {
                javafx.scene.layout.Pane currentRoot = (javafx.scene.layout.Pane) scene.getRoot();
                javafx.scene.layout.Pane nextRoot = (javafx.scene.layout.Pane) newRoot;

                // Comprobamos si ambos tienen el mismo tipo de fondo (por la clase CSS) para
                // evitar saltos visuales
                boolean sameBg = currentRoot.getStyleClass().contains("root-plain-bg") &&
                        nextRoot.getStyleClass().contains("root-plain-bg");

                if (!sameBg) {
                    loadAndSet(stage, fxmlFile, util.SettingsManager.getInstance().isFullscreen());
                } else {
                    ensureCssLoaded(scene);

                    // TRANSICIÓN ESPECIAL: Entrada al Tablero
                    if (dir == Direction.TO_BOARD) {
                        currentRoot.getChildren().add(newRoot);
                        newRoot.setOpacity(0);
                        applyGlobalEffects(newRoot, fxmlFile);

                        // Buscamos el contenido viejo para deslizarlo hacia abajo (el menú "cae")
                        Node oldContentToSlide = null;
                        for (Node n : currentRoot.getChildren()) {
                            if (oldContentToSlide == null && n.getStyleClass().contains("glass-panel")) {
                                oldContentToSlide = n;
                            }
                        }

                        double height = scene.getHeight();
                        ParallelTransition pt = new ParallelTransition();
                        pt.setInterpolator(Interpolator.EASE_BOTH);

                        if (oldContentToSlide != null) {
                            TranslateTransition slideDown = new TranslateTransition(Duration.millis(500),
                                    oldContentToSlide);
                            slideDown.setToY(height);
                            pt.getChildren().add(slideDown);
                        }

                        // El tablero aparece con un fundido suave
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), newRoot);
                        fadeIn.setToValue(1.0);
                        pt.getChildren().add(fadeIn);

                        pt.setOnFinished(e -> {
                            currentRoot.getChildren().remove(newRoot);
                            scene.setRoot(newRoot);
                            newRoot.setOpacity(1.0);
                            newRoot.layout();
                        });
                        pt.play();
                    } else {
                        // --- LÓGICA ESTÁNDAR PARA MENÚS (Estructura glass-panel) ---
                        Node oldContent = null;
                        for (Node n : currentRoot.getChildren()) {
                            if (oldContent == null && n.getStyleClass().contains("glass-panel")) {
                                oldContent = n;
                            }
                        }

                        Node newContent = null;
                        for (Node n : nextRoot.getChildren()) {
                            if (newContent == null && n.getStyleClass().contains("glass-panel")) {
                                newContent = n;
                            }
                        }

                        // Si no encontramos los paneles internos, cargamos sin animación
                        if (oldContent == null || newContent == null) {
                            loadAndSet(stage, fxmlFile, util.SettingsManager.getInstance().isFullscreen());
                        } else {
                            // Añadimos el nuevo contenido temporalmente a la escena actual para animarlo
                            currentRoot.getChildren().add(newContent);
                            applyGlobalEffects(newContent, fxmlFile);

                            double width = scene.getWidth();
                            double height = scene.getHeight();

                            ParallelTransition pt = new ParallelTransition();
                            pt.setInterpolator(Interpolator.EASE_BOTH);

                            // Definimos las animaciones según la dirección solicitada
                            switch (dir) {
                                case Direction.LEFT:
                                    newContent.setTranslateX(width);
                                    TranslateTransition outL = new TranslateTransition(Duration.millis(350),
                                            oldContent);
                                    outL.setToX(-width);
                                    TranslateTransition inL = new TranslateTransition(Duration.millis(350), newContent);
                                    inL.setToX(0);
                                    pt.getChildren().addAll(outL, inL);
                                    break;
                                case Direction.RIGHT:
                                    newContent.setTranslateX(-width);
                                    TranslateTransition outR = new TranslateTransition(Duration.millis(350),
                                            oldContent);
                                    outR.setToX(width);
                                    TranslateTransition inR = new TranslateTransition(Duration.millis(350), newContent);
                                    inR.setToX(0);
                                    pt.getChildren().addAll(outR, inR);
                                    break;
                                case Direction.FORWARD:
                                    newContent.setTranslateY(height);
                                    newContent.setOpacity(0);
                                    TranslateTransition slideUp = new TranslateTransition(Duration.millis(400),
                                            newContent);
                                    slideUp.setToY(0);
                                    FadeTransition fadeInNew = new FadeTransition(Duration.millis(400), newContent);
                                    fadeInNew.setToValue(1.0);
                                    FadeTransition fadeOutOld = new FadeTransition(Duration.millis(300), oldContent);
                                    fadeOutOld.setToValue(0.0);
                                    pt.getChildren().addAll(slideUp, fadeInNew, fadeOutOld);
                                    break;
                                case Direction.BACKWARD:
                                    newContent.setOpacity(0);
                                    newContent.setTranslateY(0);
                                    TranslateTransition slideDown = new TranslateTransition(Duration.millis(400),
                                            oldContent);
                                    slideDown.setToY(height);
                                    FadeTransition fadeInOlder = new FadeTransition(Duration.millis(400), newContent);
                                    fadeInOlder.setToValue(1.0);
                                    pt.getChildren().addAll(slideDown, fadeInOlder);
                                    break;
                                case Direction.UP:
                                    newContent.setTranslateY(height);
                                    TranslateTransition outU = new TranslateTransition(Duration.millis(350),
                                            oldContent);
                                    outU.setToY(-height);
                                    TranslateTransition inU = new TranslateTransition(Duration.millis(350), newContent);
                                    inU.setToY(0);
                                    pt.getChildren().addAll(outU, inU);
                                    break;
                                case Direction.DOWN:
                                    newContent.setTranslateY(-height);
                                    TranslateTransition outD = new TranslateTransition(Duration.millis(350),
                                            oldContent);
                                    outD.setToY(height);
                                    TranslateTransition inD = new TranslateTransition(Duration.millis(350), newContent);
                                    inD.setToY(0);
                                    pt.getChildren().addAll(outD, inD);
                                    break;
                                default:
                                    break;
                            }

                            final Node contentToMoveBack = newContent;
                            final Node contentToRemove = oldContent;
                            pt.setOnFinished(e -> {
                                // Al terminar la animación, limpiamos los nodos temporales y cambiamos el Root
                                // de la escena
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
                    }
                }
            }
        }
    }

    /**
     * Carga física del archivo FXML y configuración inicial de la escena.
     * 
     * <p>
     * Este método es la base de toda la carga. Se asegura de:
     * </p>
     * <ul>
     * <li>Cargar el FXML.</li>
     * <li>Configurar o actualizar la escena del Stage.</li>
     * <li>Limpiar y reaplicar hojas de estilo.</li>
     * <li>Configurar atajos de teclado y persistencia de pantalla completa.</li>
     * <li>Aplicar la resolución configurada si no estamos en pantalla
     * completa.</li>
     * </ul>
     * 
     * @param stage      La ventana (Stage) donde se establecerá la escena.
     * @param fxmlFile   Nombre del archivo de la vista a cargar.
     * @param fullScreen Determina si la ventana debe configurarse en modo pantalla
     *                   completa.
     * @throws IOException Si ocurre un error leyendo el archivo FXML o construyendo
     *                     su jerarquía.
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
            // Limpiamos estilos para evitar acumulación al cambiar de vista
            scene.getStylesheets().clear();
            scene.setRoot(root);
        }

        ensureCssLoaded(scene);
        applyGlobalEffects(root, fxmlFile);
        setupGlobalHotkeys(scene);

        util.SettingsManager sm = util.SettingsManager.getInstance();
        stage.setFullScreen(fullScreen);

        // Listener para sincronizar el estado de pantalla completa con el archivo de
        // configuración
        if (stage.getUserData() == null || !stage.getUserData().equals("LISTENER_ADDED")) {
            stage.fullScreenProperty().addListener((obs, wasFull, isFull) -> {
                if (sm.isFullscreen() != isFull) {
                    sm.setFullscreen(isFull);
                    sm.save();
                    System.out.println("► Fullscreen persistido automáticamente: " + isFull);
                }
            });
            stage.setUserData("LISTENER_ADDED");
        }

        // Si no estamos en pantalla completa, aplicamos la resolución guardada en
        // opciones
        if (!isFullWindowed(stage)) {
            applyResolution(stage, sm);
        }
    }

    /**
     * Alterna entre modo ventana y pantalla completa.
     * 
     * @param stage La ventana a modificar.
     */
    public static void toggleFullscreen(Stage stage) {
        if (stage != null) {
            boolean newState = !stage.isFullScreen();
            stage.setFullScreen(newState);
        }
    }

    /**
     * Configura los atajos de teclado globales (F11) para la escena.
     * 
     * @param scene La escena donde se aplicarán los hotkeys.
     */
    public static void setupGlobalHotkeys(Scene scene) {
        if (scene != null) {
            // Evitamos añadir el filtro más de una vez a la misma Scene
            if (!scene.getProperties().containsKey("HOTKEYS_ADDED")) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.F11) {
                        long now = System.currentTimeMillis();
                        // Control de spam para evitar parpadeos
                        if (now - lastToggleTime > TOGGLE_COOLDOWN) {
                            lastToggleTime = now;
                            Stage stage = (Stage) scene.getWindow();
                            toggleFullscreen(stage);
                        }
                        event.consume();
                    }
                });

                scene.getProperties().put("HOTKEYS_ADDED", true);
            }
        }
    }

    /**
     * Verifica si la ventana está actualmente en pantalla completa.
     * 
     * @param stage La ventana a comprobar.
     * @return true si la ventana se encuentra en pantalla completa, false en caso
     *         contrario.
     */
    private static boolean isFullWindowed(Stage stage) {
        return stage.isFullScreen();
    }

    /**
     * Aplica la resolución de pantalla guardada en las preferencias.
     * 
     * @param stage La ventana a la que se le aplicará la resolución.
     * @param sm    Instancia del gestor de ajustes para extraer la resolución
     *              deseada.
     */
    private static void applyResolution(Stage stage, util.SettingsManager sm) {
        String[] dimensions = sm.getResolution().split("x");
        try {
            stage.setWidth(Double.parseDouble(dimensions[0]));
            stage.setHeight(Double.parseDouble(dimensions[1]));
            stage.centerOnScreen();
        } catch (Exception e) {
            // Silenciamos errores de parseo de resolución
        }
    }

    /**
     * Asegura que el archivo CSS principal esté cargado en la escena.
     * 
     * @param scene La escena en la que se inyectará la hoja de estilos global.
     */
    private static void ensureCssLoaded(Scene scene) {
        if (scene != null) {
            try {
                String cssPath = NavigationController.class.getResource(CSS_PATH).toExternalForm();
                if (!scene.getStylesheets().contains(cssPath)) {
                    scene.getStylesheets().add(cssPath);
                }
            } catch (Exception e) {
                System.err.println("Error cargando CSS: " + e.getMessage());
            }
        }
    }

    /**
     * Aplica automáticamente animaciones de hover y sonidos a todos los botones de
     * una vista.
     * 
     * <p>
     * Este método usa selectores CSS (.button, .add-player-button, etc.) para
     * inyectar
     * comportamiento sin tener que escribir código en cada controlador.
     * </p>
     * 
     * @param root     El nodo raíz de la vista (Parent) a analizar.
     * @param fxmlFile Nombre del archivo FXML para comprobar exclusiones de sonido
     *                 en ciertas pantallas.
     */
    private static void applyGlobalEffects(Parent root, String fxmlFile) {
        if (root != null) {
            // Desactivamos sonidos en pantallas específicas para evitar ruidos molestos
            boolean menuSoundsEnabled = fxmlFile != null &&
                    !fxmlFile.equalsIgnoreCase("LoginView.fxml") &&
                    !fxmlFile.equalsIgnoreCase("TableroJuego.fxml") &&
                    !fxmlFile.equalsIgnoreCase("Intro.fxml");

            // Buscamos todos los botones y aplicamos lógica
            root.lookupAll(".button").forEach(node -> {
                util.UIUtils.applyHoverAnimation(node);
                if (menuSoundsEnabled)
                    attachMenuSounds(node);
            });

            root.lookupAll(".remove-slot-button").forEach(node -> {
                util.UIUtils.applyHoverAnimation(node);
                if (menuSoundsEnabled)
                    attachMenuSounds(node);
            });

            root.lookupAll(".add-player-button").forEach(node -> {
                util.UIUtils.applyHoverAnimation(node);
                if (menuSoundsEnabled)
                    attachMenuSounds(node);
            });
        }
    }

    /**
     * Adjunta los sonidos de clic a un nodo basándose en su texto o clase CSS.
     * 
     * @param node El componente visual de la interfaz de usuario.
     */
    private static void attachMenuSounds(Node node) {
        node.setOnMouseClicked(e -> {
            String text = "";
            if (node instanceof javafx.scene.control.Button) {
                text = ((javafx.scene.control.Button) node).getText().toUpperCase();
            }

            // --- REGLAS DE SONIDO ---
            // Acciones afirmativas / avance
            if (text.equals("SALIR") || text.contains("SÍ") || text.contains("CONFIRMAR")) {
                util.SoundManager.playConfirm();
            }
            // Acciones negativas / retroceso
            else if (text.contains("VOLVER") || text.contains("ATRÁS") || text.contains("NO")) {
                util.SoundManager.playBack();
            }
            // Por clase CSS si no hay texto claro
            else if (node.getStyleClass().contains("button-primary") ||
                    node.getStyleClass().contains("button-danger") ||
                    node.getStyleClass().contains("add-player-button")) {
                util.SoundManager.playConfirm();
            } else if (node.getStyleClass().contains("button-secondary") ||
                    node.getStyleClass().contains("remove-slot-button")) {
                util.SoundManager.playBack();
            } else {
                util.SoundManager.playConfirm();
            }
        });
    }

    /**
     * Sobrecarga para aplicar efectos si el nodo es un Parent.
     * 
     * @param node     El nodo en el cual verificar si es del tipo Parent.
     * @param fxmlFile Nombre de la vista, utilizado para condiciones de audio.
     */
    private static void applyGlobalEffects(Node node, String fxmlFile) {
        if (node instanceof Parent) {
            applyGlobalEffects((Parent) node, fxmlFile);
        }
    }

    /**
     * Muestra el overlay de carga.
     * 
     * @param scene La escena sobre la cual se desplegará el overlay (pantalla de
     *              espera).
     */
    public static void showLoading(Scene scene) {
        LoadingOverlay.show(scene);
    }

    /** Oculta el overlay de carga. */
    public static void hideLoading() {
        LoadingOverlay.hide();
    }
}
