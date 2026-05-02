package controlador;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.event.ActionEvent;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.*;
import datos.BBDD;
import util.SettingsManager;
import util.SoundManager;
import util.CameraController;
import util.GameFlowManager;

/**
 * Controlador principal de la vista del Tablero de Juego.
 * 
 * <p>
 * Es la clase más compleja del sistema, encargada de orquestar el renderizado
 * isométrico
 * del mundo, la gestión de la cámara (zoom y pan), las animaciones de
 * movimiento
 * escalonado, la lógica de turnos tanto para humanos como para la CPU, y la
 * gestión de los diversos menús y overlays (inventario, pausa, eventos).
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class TableroController implements GameFlowManager.GameUIHandler {

    @FXML
    private Pane boardPane;
    @FXML
    private Pane cameraViewport;
    @FXML
    private Group zoomGroup;
    @FXML
    private HBox turnIndicatorBox;
    @FXML
    private Button btnDado;
    @FXML
    private Button btnRecolectar;
    @FXML
    private Button btnInventario;
    @FXML
    private Button btnMenu;

    // Capas de animación
    @FXML
    private VBox diceAnimationContainer;
    @FXML
    private ImageView diceImageView;
    @FXML
    private Label diceNumberLabel;
    @FXML
    private Label dadoResultadoLabel;
    @FXML
    private TextArea gameLogArea;

    // Panel lateral de Log
    @FXML
    private HBox logPanelContainer;
    @FXML
    private VBox logContentBox;
    @FXML
    private Button btnToggleLog;
    private boolean isLogOpen = false;

    // PANEL DE ESTADO SECUNDARIO (En el Log)
    @FXML
    private VBox secondaryStatusContainer;

    // INVENTARIO FLOTANTE
    @FXML
    private VBox inventoryOverlayPanel;
    @FXML
    private FlowPane inventoryFlowPane;
    private boolean isInventoryOpen = false;

    // CAPA DE EVENTOS
    @FXML
    private StackPane eventOverlay;
    @FXML
    private Pane eventDimmer;
    @FXML
    private Pane highlightLayer;
    @FXML
    private VBox eventDialogueBox;
    @FXML
    private Label eventTitleLabel;
    @FXML
    private Label eventMessageLabel;
    @FXML
    private Button btnDadoLento;
    @FXML
    private Button btnDadoRapido;

    // --- ELEMENTOS DE RAÍZ Y OVERLAY ---
    @FXML
    private StackPane rootStack;
    @FXML
    private StackPane overlayPane;
    @FXML
    private VBox menuPausa;
    @FXML
    private VBox optionsOverlay;
    @FXML
    private VBox confirmOverlay;

    // Controles de Opciones en Overlay
    @FXML
    private ComboBox<String> resComboOverlay;
    @FXML
    private CheckBox fsCheckOverlay;
    @FXML
    private Slider musicSliderOverlay;
    @FXML
    private Label musicLabelOverlay;
    @FXML
    private Slider sfxSliderOverlay;
    @FXML
    private Label sfxLabelOverlay;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Button btnApplyOverlay;

    // Controles de Seguridad en Overlay
    @FXML
    private VBox passInfoOverlay;
    @FXML
    private Label usernameLabelOverlay;
    @FXML
    private VBox passFormOverlay;
    @FXML
    private PasswordField newPassFieldOverlay;
    @FXML
    private PasswordField confirmPassFieldOverlay;
    @FXML
    private Label passErrorLabelOverlay;

    // Controles de Confirmación de Objetos
    @FXML
    private VBox itemConfirmOverlay;
    @FXML
    private Label itemConfirmTitle;
    @FXML
    private Label itemConfirmMessage;
    @FXML
    private Label itemConfirmSubMessage;
    @FXML
    private Button btnItemConfirmYes;

    private String selectedItemType = null;

    // Controles de Confirmación
    @FXML
    private Button btnConfirmYes;
    @FXML
    private Label confirmTitle;
    @FXML
    private Label confirmMessage;

    // Valores iniciales para Opciones (Dirty State)
    private String initialResolution;
    private boolean initialFullscreen;
    private double initialMusic;
    private double initialSfx;

    // Estado de confirmación actual
    private int pendingAction;

    private Tablero tablero;
    private List<Jugador> jugadores;
    private int turnoActual = 0;
    private boolean animacionEnCurso = false;

    private Map<Integer, StackPane> casillaNodes = new HashMap<>();
    private Map<Jugador, ImageView> playerTokens = new HashMap<>(); // Fichas en el tablero
    private Map<Jugador, ImageView> turnCircles = new HashMap<>(); // Círculos del indicador superior
    private Pane tokensPane = new Pane(); // Capa superior para que los jugadores no queden detrás

    // GESTIÓN DE FLUJO Y REGLAS (Delegada)
    private GameFlowManager gameFlow;

    // ESTADO DE EVENTOS
    private Map<Jugador, Pane> highlightingBackups = new HashMap<>();
    private Runnable onEventContinue;

    // GESTIÓN DE CÁMARA (Delegada)
    private CameraController camera;

    @FXML
    private StackPane eventNotificationContainer;
    @FXML
    private Label eventNotificationLabel;
    @FXML
    private StackPane cpuTurnOverlay;

    private final BBDD bbdd = new BBDD();
    private Juego juegoSimulado = new Juego();

    /**
     * Inicializa el controlador del tablero.
     * 
     * <p>
     * Configura la cámara, inicializa los overlays, carga la semilla del mundo,
     * prepara a los jugadores y decide si se trata de una partida nueva o cargada
     * para sincronizar con la base de datos. Finalmente, dispara el renderizado
     * inicial.
     * </p>
     */
    @FXML
    public void initialize() {
        // Iniciamos el sistema de música dinámica para la partida
        util.SoundManager.startGameMusic();

        // Inicialización de Opciones Overlay
        initOptionsOverlay();

        // Inicializar Gestores
        this.camera = new CameraController(cameraViewport, boardPane, zoomGroup, casillaNodes);
        this.camera.init();

        // Configuración y sincronización del slider de zoom
        if (zoomSlider != null) {
            zoomSlider.setMin(CameraController.MIN_ZOOM);
            zoomSlider.setMax(CameraController.MAX_ZOOM);
            zoomSlider.setValue(this.camera.zoomProperty().get());
            zoomSlider.valueProperty().bindBidirectional(this.camera.zoomProperty());
        }

        // Esconder panel de log al inicio
        logPanelContainer.setTranslateX(-320);

        this.tablero = new Tablero();
        String seed = GameContext.getInstance().getSeed();
        tablero.introducirSeed(seed);

        // Asignamos el nombre de la partida
        juegoSimulado.setNombrePartida(GameContext.getInstance().getGameName());

        this.jugadores = GameContext.getInstance().getConfiguredPlayers();

        // Inicializar Gestor de Flujo (después de tener tablero y jugadores)
        this.gameFlow = new GameFlowManager(this.tablero, this.jugadores, this);

        // Centrar en el primer jugador
        if (jugadores != null && !jugadores.isEmpty()) {
            camera.smoothCenterOnPlayer(jugadores.get(0), 1.0);
        } else {
            camera.centerBoard();
        }

        // Centrar tablero automáticamente al conocer el tamaño
        cameraViewport.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0)
                camera.centerBoard();
        });
        cameraViewport.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0)
                camera.centerBoard();
        });

        juegoSimulado.setTablero(this.tablero);
        juegoSimulado.getJugadores().addAll(this.jugadores);

        if (!GameContext.getInstance().isPartidaCargada()) {
            // Juego Nuevo -> Guardar en BBDD
            for (Jugador j : jugadores) {
                int newId = bbdd.registrarJugadorSiNoExiste(j);
                if (newId != -1)
                    j.setId(newId);
            }

            int idPartida = bbdd.guardarNuevaPartida(juegoSimulado);
            if (idPartida > 0) {
                tablero.setIdPartida(idPartida);
                for (Jugador j : jugadores) {
                    bbdd.insertarParticipacion(idPartida, j.getId(), j.getColor());
                }
                log("Partida #" + idPartida + " guardada en BBDD.");
            } else {
                log("Aviso: No se pudo conectar a BBDD.");
            }
            log("¡Partida lista! Comienza " + jugadores.get(0).getNombre());
        } else {
            // Juego Cargado -> Restaurar estado
            tablero.setIdPartida(GameContext.getInstance().getIdPartidaCargar());
            this.turnoActual = GameContext.getInstance().getTurnoCargado();
            log("Partida #" + tablero.getIdPartida() + " restaurada.");
            log("Es el turno de " + jugadores.get(this.turnoActual).getNombre());

            GameContext.getInstance().setIdPartidaCargar(-1);
        }

        dibujarTablero();
        crearFichasJugadores();
        crearIndicadorTurnos();

        actualizarUI();

        // OCULTAR GIF DE CARGA
        NavigationController.hideLoading();

        comprobarBloqueoInicioTurno();
    }

    /**
     * Renderiza el tablero de forma dinámica con un efecto de "ola" descendente.
     * 
     * <p>
     * Calcula las posiciones isométricas de cada pilar basándose en su índice y
     * aplica una secuencia de animaciones de caída y rebote escalonadas en el
     * tiempo.
     * </p>
     */
    private void dibujarTablero() {
        boardPane.getChildren().clear();
        casillaNodes.clear();

        // --- CAPA DE FONDO DEL ENTORNO ---
        ImageView marView = new ImageView(); // Declaramos la capa de mar fuera para usarla después
        try {
            String fondoPath = "/assets/tablero/fondo_tablero/fondo_tablero.png";
            String marPath = "/assets/tablero/fondo_tablero/fondo_tablero_mar.png";
            java.net.URL fondoUrl = getClass().getResource(fondoPath);
            java.net.URL marUrl = getClass().getResource(marPath);

            if (fondoUrl != null) {
                Image fondoImg = new Image(fondoUrl.toExternalForm(), true);
                ImageView fondoView = new ImageView();

                // Carga y posicionamiento del fondo base
                fondoImg.progressProperty().addListener((obs, oldV, newV) -> {
                    if (newV.doubleValue() == 1.0) {
                        fondoView.setImage(fondoImg);
                        fondoView.setPreserveRatio(true);
                        fondoView.setSmooth(true);

                        double factorEscala = 3640;
                        fondoView.setFitWidth(factorEscala);
                        double imgW = factorEscala;
                        double imgH = fondoImg.getHeight() * (factorEscala / fondoImg.getWidth());

                        fondoView.setLayoutX(((2400 - imgW) / 2) - 20);
                        fondoView.setLayoutY(((2000 - imgH) / 2) + 50);
                    }
                });
                boardPane.getChildren().add(0, fondoView);
            }

            if (marUrl != null) {
                Image marImg = new Image(marUrl.toExternalForm(), true);
                
                // Carga y posicionamiento de la capa de mar (delante de las casillas)
                marImg.progressProperty().addListener((obs, oldV, newV) -> {
                    if (newV.doubleValue() == 1.0) {
                        marView.setImage(marImg);
                        marView.setPreserveRatio(true);
                        marView.setSmooth(true);

                        double factorEscala = 3640;
                        marView.setFitWidth(factorEscala);
                        double imgW = factorEscala;
                        double imgH = marImg.getHeight() * (factorEscala / marImg.getWidth());

                        marView.setLayoutX(((2400 - imgW) / 2) - 20);
                        marView.setLayoutY(((2000 - imgH) / 2) + 50);
                    }
                });
                // No lo añadimos aún, lo haremos después de las casillas
                marView.setMouseTransparent(true); // Para que no bloquee clics en el tablero
            }
        } catch (Exception e) {
            System.err.println("Aviso: Error al configurar capas de fondo: " + e.getMessage());
        }


        // Inicializamos la capa de tokens y la añadimos al final (encima de todo)
        tokensPane.getChildren().clear();
        tokensPane.setPickOnBounds(false); // Para que no bloquee clics en el tablero

        List<StackPane> sortedNodes = new java.util.ArrayList<>();

        double centerX = 1200.0;
        double bottomY = 1470.0; // Un pelín hacia atrás (arriba)

        double xOffset = 48.0; // Reducido más para forzar el solapamiento y evitar huecos
        double yOffset = 24.0; // Manteniendo la proporción isométrica 2:1

        for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
            Casilla c = tablero.getCasilla(i);

            if (c != null) {
                StackPane cellNode = crearNodoCasilla(c);

                double layoutX, layoutY;
                int zOrder;

                if (i < 49) {
                    int gy = i / 7;
                    int gx;
                    if (gy % 2 == 0) {
                        gx = i % 7;
                    } else {
                        gx = 6 - (i % 7);
                    }

                    zOrder = gx + gy;

                    // ESCALERA MUY SUAVE: Diferencia casi imperceptible (2px por casilla)
                    double stepHeight = i * 2.0;

                    layoutX = centerX + (gx - gy) * xOffset - 55;
                    layoutY = bottomY - (gx + gy) * yOffset - stepHeight - 150;
                } else {
                    zOrder = 0;
                    // Posicionamos el último pilar (la meta) encima del iglú del fondo.
                    // Píxel original del iglú: 728, 467.
                    // Escala: 3640 / 1450 = 2.5103...
                    // Offset fondo: X=-640, Y=-311.86
                    // Coordenadas en boardPane: X=1187.53, Y=860.47
                    // Ajustamos -55 en X y -35 en Y para que el token del jugador caiga justo en el centro del píxel.
                    layoutX = 1132.5; 
                    layoutY = 825.5;
                }

                cellNode.setUserData(zOrder);

                // La posición layout es la FINAL
                cellNode.setLayoutX(layoutX);
                cellNode.setLayoutY(layoutY);
                
                if (i == 49) {
                    cellNode.setOpacity(0);
                }

                // Animación de caída (modifica translateY desde muy arriba hasta 0)
                double dropDistance = -1000.0;

                TranslateTransition drop = new TranslateTransition(Duration.seconds(0.2), cellNode);
                drop.setFromY(dropDistance);
                drop.setToY(0);
                drop.setInterpolator(Interpolator.EASE_IN);

                // Efecto de rebote sutil
                TranslateTransition bounce = new TranslateTransition(Duration.seconds(0.15), cellNode);
                bounce.setByY(-10);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.setInterpolator(Interpolator.EASE_OUT);

                SequentialTransition seq = new SequentialTransition(drop, bounce);
                // Retardo muy corto para que la ola sea rápida
                seq.setDelay(Duration.millis(i * 15));
                seq.play();

                casillaNodes.put(i, cellNode);
                sortedNodes.add(cellNode);
            }
        }

        sortedNodes.sort((a, b) -> Integer.compare((int) b.getUserData(), (int) a.getUserData()));

        boardPane.getChildren().addAll(sortedNodes);
        
        // Añadimos la capa de mar sobre las casillas
        if (marView != null) {
            boardPane.getChildren().add(marView);
        }

        tokensPane.setOpacity(0); // Ocultamos los pinguinos al principio
        boardPane.getChildren().add(tokensPane);

        // Animamos la aparición de los jugadores cuando terminen de caer los pilares
        double totalAnimDuration = 0.2 + 0.15 + (Tablero.TAMANYO_TABLERO * 0.015);
        PauseTransition showPlayers = new PauseTransition(Duration.seconds(totalAnimDuration));
        showPlayers.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), tokensPane);
            fade.setToValue(1.0);
            fade.play();

            // Animación extra: pequeño saltito de los pingüinos al aparecer
            for (Node token : tokensPane.getChildren()) {
                TranslateTransition jump = new TranslateTransition(Duration.seconds(0.2), token);
                jump.setByY(-15);
                jump.setAutoReverse(true);
                jump.setCycleCount(2);

                // Retardo aleatorio para que no salten todos a la vez
                jump.setDelay(Duration.millis(Math.random() * 200));
                jump.play();
            }
        });
        showPlayers.play();
    }

    /**
     * Crea un nodo visual (StackPane) que representa un pilar del tablero.
     * 
     * @param c El objeto Casilla del modelo con la información del tipo y posición.
     * @return Un StackPane configurado con el sprite y estilos correspondientes.
     */
    private StackPane crearNodoCasilla(Casilla c) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("casilla-base");
        // Pilares más grandes
        pane.setPrefSize(110, 300);
        pane.setAlignment(Pos.BOTTOM_CENTER);

        try {
            Image img = new Image(getClass().getResourceAsStream(c.getSpritePath()));
            ImageView view = new ImageView(img);
            view.setFitWidth(110);
            view.setPreserveRatio(true);
            pane.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error cargando pilar para " + c.getTipo() + ": " + e.getMessage());
            pane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;");
        }

        Label numLabel = new Label(String.valueOf(c.getPosicion()));
        numLabel.getStyleClass().add("label-num-casilla");
        StackPane.setAlignment(numLabel, Pos.TOP_RIGHT);
        numLabel.setTranslateY(35); // Ajustado para el nuevo tamaño
        numLabel.setTranslateX(-10);

        pane.getChildren().add(numLabel);
        return pane;
    }

    /**
     * Genera las fichas visuales (tokens) de cada jugador basándose en su color.
     * Las fichas se almacenan en un mapa para su posterior manipulación animada.
     */
    private void crearFichasJugadores() {
        for (Jugador j : jugadores) {
            ImageView token = new ImageView();
            try {
                String colorName = j.getColor() != null ? j.getColor().toLowerCase() : "gris";
                if (j instanceof Foca)
                    colorName = "gris";
                Image img = new Image(getClass().getResourceAsStream("/assets/jugadores/" + colorName + ".png"));
                token.setImage(img);
            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen para " + j.getColor());
            }
            token.setFitWidth(36);
            token.setFitHeight(36);
            token.setPreserveRatio(true);
            token.getStyleClass().add("player-token");

            // Replicar el efecto visual del borde con el color del jugador
            DropShadow ds = new DropShadow();
            if (j instanceof Foca) {
                ds.setColor(Color.web("#546e7a"));
            } else {
                ds.setColor(getColorFromString(j.getColor()));
            }
            ds.setRadius(3);
            ds.setSpread(1.0);
            token.setEffect(ds);

            playerTokens.put(j, token);
        }
        // Posicionamos a todos (por si hay varios en la 0)
        for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
            actualizarPosicionesJugadoresEnCasilla(i);
        }
    }

    /**
     * Coloca la ficha visual de un jugador en su posición correspondiente del
     * tablero.
     * 
     * @param j El jugador cuya ficha se desea posicionar.
     */
    private void posicionarToken(Jugador j) {
        actualizarPosicionesJugadoresEnCasilla(j.getPosicion());
    }

    /**
     * Reorganiza las fichas de todos los jugadores que se encuentran en una misma
     * casilla.
     * 
     * <p>
     * Utiliza patrones de dispersión (diamante, triángulo, etc.) para que las
     * fichas
     * no se solapen completamente y sean visibles simultáneamente sobre el pilar.
     * </p>
     * 
     * @param pos El índice de la casilla a organizar.
     */
    private void actualizarPosicionesJugadoresEnCasilla(int pos) {
        List<Jugador> jugadoresEnCasilla = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (j.getPosicion() == pos) {
                jugadoresEnCasilla.add(j);
            }
        }

        StackPane cell = casillaNodes.get(pos);
        if (cell == null || jugadoresEnCasilla.isEmpty())
            return;

        double cellX = cell.getLayoutX();
        double cellY = cell.getLayoutY();

        int num = jugadoresEnCasilla.size();
        for (int k = 0; k < num; k++) {
            Jugador j = jugadoresEnCasilla.get(k);
            ImageView token = playerTokens.get(j);

            double offsetX = 0;
            double offsetY = 0;

            // PATRONES DE DISPERSIÓN SEGÚN BORRADOR DEL USUARIO
            switch (num) {
                case 1:
                    // Un solo jugador: Centrado
                    offsetX = 0;
                    offsetY = 0;
                    break;
                case 2:
                    // Dos jugadores: Uno a cada lado
                    offsetX = (k == 0) ? -18 : 18;
                    offsetY = -5;
                    break;
                case 3:
                    // Tres jugadores: Formación de triángulo
                    if (k == 0) {
                        offsetX = 0;
                        offsetY = -15;
                    } else if (k == 1) {
                        offsetX = -18;
                        offsetY = 5;
                    } else {
                        offsetX = 18;
                        offsetY = 5;
                    }
                    break;
                case 4:
                    // Cuatro jugadores: Cuadrado / Diamante
                    if (k == 0) {
                        offsetX = -18;
                        offsetY = -12;
                    } else if (k == 1) {
                        offsetX = 18;
                        offsetY = -12;
                    } else if (k == 2) {
                        offsetX = -18;
                        offsetY = 12;
                    } else {
                        offsetX = 18;
                        offsetY = 12;
                    }
                    break;
            }

            // 55 es el centro horizontal del pilar (110/2)
            // 35 es el ajuste de altura para que los pingüinos queden centrados en la parte
            // superior
            double targetX = cellX + 55 + offsetX;
            double targetY = cellY + 35 + offsetY;

            token.setTranslateX(targetX);
            token.setTranslateY(targetY);

            if (!tokensPane.getChildren().contains(token)) {
                tokensPane.getChildren().add(token);
            }
        }
    }

    /**
     * Genera la barra superior de indicadores de turno.
     * Muestra los iconos de los jugadores y resalta quién tiene el turno activo.
     */
    private void crearIndicadorTurnos() {
        turnIndicatorBox.getChildren().clear();
        turnCircles.clear();

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);

            ImageView circle = new ImageView();
            try {
                String colorName = j.getColor() != null ? j.getColor().toLowerCase() : "gris";
                if (j instanceof Foca)
                    colorName = "gris";
                Image img = new Image(
                        getClass().getResourceAsStream("/assets/ico_jugadores/ico_" + colorName + ".png"));
                circle.setImage(img);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono para " + j.getColor());
            }
            circle.setFitWidth(36);
            circle.setFitHeight(36);
            circle.setPreserveRatio(true);

            DropShadow ds = new DropShadow();
            ds.setRadius(5);
            ds.setColor(Color.BLACK);
            circle.setEffect(ds);

            turnCircles.put(j, circle);
            turnIndicatorBox.getChildren().add(circle);

            if (i < jugadores.size() - 1) {
                Label arrow = new Label("→");
                arrow.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");
                turnIndicatorBox.getChildren().add(arrow);
            }
        }
    }

    // --- LÓGICA DE TURNOS ---

    /**
     * Maneja el evento de lanzar el dado normal.
     * 
     * @param event El evento de acción del botón.
     */
    @FXML
    private void handleRollDice(ActionEvent event) {
        if (!animacionEnCurso) {
            int pasos = (int) (Math.random() * 6) + 1;
            ejecutarTurno(pasos);
        }
    }

    /**
     * Maneja el evento de recolectar bolas de nieve.
     * 
     * <p>
     * El jugador renuncia a su movimiento para intentar obtener de 1 a 3
     * bolas de nieve aleatoriamente, siempre que no supere el máximo del
     * inventario.
     * </p>
     * 
     * @param event El evento de acción del botón.
     */
    @FXML
    private void handleCollectSnowballs(ActionEvent event) {
        if (!animacionEnCurso) {
            Jugador jActual = jugadores.get(turnoActual);
            int actuales = jActual.getInventario().getCantidad("BolaNieve");

            if (actuales >= Inventario.MAX_BOLAS_NIEVE) {
                mostrarNotificacionEvento("¡MOCHILA LLENA!", jActual);
                log(jActual.getNombre() + " ya tiene el máximo de bolas de nieve (" + Inventario.MAX_BOLAS_NIEVE + ").");
                util.SoundManager.playBack();
                if (jActual instanceof Foca) {
                    finalizarTurno();
                }
            } else {
                int cantidadAIntentar = (int) (Math.random() * 3) + 1;
                int recolectadas = 0;

                for (int i = 0; i < cantidadAIntentar; i++) {
                    if (jActual.getInventario().agregarObjeto("BolaNieve")) {
                        recolectadas++;
                    }
                }

                if (recolectadas > 0) {
                    String msg = "¡+" + recolectadas + " BOLAS NIEVE!";
                    log(jActual.getNombre() + " se queda quieto y recolecta " + recolectadas + " bolas de nieve ❄");
                    mostrarNotificacionEvento(msg, jActual);
                    util.SoundManager.playConfirm();
                    actualizarUI();

                    setControlesBloqueados(true);
                    PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> finalizarTurno());
                    pause.play();
                }
            }
        }
    }

    /**
     * Maneja el uso de un dado lento (rango 1-3).
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleUseDadoLento(ActionEvent event) {
        if (!animacionEnCurso) {
            Jugador jActual = jugadores.get(turnoActual);
            if (jActual.getInventario().tieneObjeto("DadoLento")) {
                jActual.getInventario().usarDadoEspecifico("Lento", jActual);
                int pasos = (int) (Math.random() * 3) + 1;
                ejecutarTurno(pasos);
            } else {
                log(jActual.getNombre() + " no tiene Dados Lentos.");
            }
        }
    }

    /**
     * Maneja el uso de un dado rápido (rango 3-8).
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleUseDadoRapido(ActionEvent event) {
        if (!animacionEnCurso) {
            Jugador jActual = jugadores.get(turnoActual);
            if (jActual.getInventario().tieneObjeto("DadoRapido")) {
                jActual.getInventario().usarDadoEspecifico("Rapido", jActual);
                int pasos = (int) (Math.random() * 6) + 3;
                ejecutarTurno(pasos);
            } else {
                log(jActual.getNombre() + " no tiene Dados Rápidos.");
            }
        }
    }

    /**
     * Inicia la secuencia de turno tras lanzar un dado.
     * 
     * <p>
     * Muestra la animación del dado rodando y posteriormente dispara el movimiento
     * casilla por casilla del jugador.
     * </p>
     * 
     * @param pasos El número de casillas a avanzar obtenido del dado.
     */
    private void ejecutarTurno(int pasos) {
        if (!animacionEnCurso) {
            animacionEnCurso = true;

            Jugador jActual = jugadores.get(turnoActual);
            dadoResultadoLabel.setText("Último dado: " + pasos);
            log(jActual.getNombre() + " saca un " + pasos);

            setControlesBloqueados(true);

            try {
                Image rollingGif = new Image(
                        getClass().getResource("/assets/tablero/dados/dado_rodando.gif").toExternalForm());
                diceImageView.setImage(rollingGif);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el GIF del dado: " + e.getMessage());
            }

            diceImageView.setVisible(true);
            diceNumberLabel.setVisible(false);
            diceAnimationContainer.setOpacity(0);
            diceAnimationContainer.setTranslateY(0);
            diceAnimationContainer.setVisible(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), diceAnimationContainer);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            PauseTransition rollPause = new PauseTransition(Duration.seconds(2.0));
            rollPause.setOnFinished(e -> {
                diceImageView.setVisible(false);
                diceNumberLabel.setText(String.valueOf(pasos));
                diceNumberLabel.setVisible(true);

                PauseTransition moveDelay = new PauseTransition(Duration.seconds(1.0));
                moveDelay.setOnFinished(e2 -> {
                    TranslateTransition moveUp = new TranslateTransition(Duration.millis(500), diceAnimationContainer);
                    moveUp.setToY(-280);
                    moveUp.setInterpolator(Interpolator.EASE_BOTH);
                    moveUp.play();

                    moverJugadorAnimado(jActual, pasos);
                });
                moveDelay.play();
            });
            rollPause.play();
        }
    }

    /**
     * Realiza el movimiento animado del jugador saltando de casilla en casilla.
     * 
     * @param j              El jugador que se mueve.
     * @param pasosRestantes Cuántas casillas le quedan por saltar.
     * @param onComplete     Acción a ejecutar cuando el jugador llega a su destino
     *                       final.
     */
    private void moverJugadorAnimado(Jugador j, int pasosRestantes) {
        if (pasosRestantes <= 0 || j.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            diceNumberLabel.setText("0");

            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), diceAnimationContainer);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                diceAnimationContainer.setVisible(false);
                gameFlow.processCellEffects(j);
            });
            fadeOut.play();
        } else {
            animacionEnCurso = true;
            diceNumberLabel.setText(String.valueOf(pasosRestantes));

            int posAntigua = j.getPosicion();
            int posNueva = posAntigua + 1;

            j.setPosicion(posNueva);

            StackPane cellAntigua = casillaNodes.get(posAntigua);

            cellAntigua.getChildren().remove(playerTokens.get(j));
            posicionarToken(j);

            if (camera.isAutoMode()) {
                camera.smoothCenterOnPlayer(j, 0.4);
            }

            PauseTransition pause = new PauseTransition(Duration.millis(350));
            pause.setOnFinished(e -> {
                moverJugadorAnimado(j, pasosRestantes - 1);
            });
            pause.play();
        }
    }

    @Override
    public void notifyEvent(String msg, Jugador j) {
        mostrarNotificacionEvento(msg, j);
    }

    @Override
    public void showEventDialog(String title, String msg, Runnable onComplete, Jugador... players) {
        mostrarEventoDialogo(title, msg, onComplete, players);
    }

    @Override
    public void moveTokenDirect(Jugador j, int desde, int hasta, Runnable onComplete) {
        moverFichaDirecta(j, desde, hasta, onComplete);
    }

    @Override
    public void finishTurn() {
        finalizarTurno();
    }

    @Override
    public void playShakeAnimation(int position, Runnable onComplete) {
        Node pilar = casillaNodes.get(position);
        if (pilar != null) {
            TranslateTransition tiembla = new TranslateTransition(Duration.millis(50), pilar);
            tiembla.setByX(4);
            tiembla.setAutoReverse(true);
            tiembla.setCycleCount(6);
            tiembla.setOnFinished(e -> {
                if (onComplete != null)
                    onComplete.run();
            });
            tiembla.play();
        } else {
            if (onComplete != null)
                onComplete.run();
        }
    }

    /**
     * Mueve una ficha directamente entre dos posiciones con efectos visuales
     * especiales.
     * 
     * <p>
     * Dependiendo del tipo de casilla de origen (agujero, trineo, etc.), aplica
     * animaciones de hundimiento, desaparición o impulsos.
     * </p>
     * 
     * @param j          El jugador a mover.
     * @param desde      Casilla de origen.
     * @param hasta      Casilla de destino.
     * @param onComplete Acción al terminar el movimiento.
     */
    private void moverFichaDirecta(Jugador j, int desde, int hasta, Runnable onComplete) {
        Casilla cOrig = tablero.getCasilla(desde);
        String tipo = (cOrig != null) ? cOrig.getTipo().toUpperCase() : "";
        Node token = playerTokens.get(j);
        Node pilar = casillaNodes.get(desde);

        if (tipo.contains("AGUJERO") || tipo.contains("ROMPEDIZAS")) {
            // Pilar se hunde
            TranslateTransition hundirPilar = new TranslateTransition(Duration.millis(500), pilar);
            hundirPilar.setByY(20);
            hundirPilar.setInterpolator(Interpolator.EASE_IN);

            // Jugador desaparece
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), token);
            fadeOut.setToValue(0);

            ParallelTransition pt1 = new ParallelTransition(hundirPilar, fadeOut);
            pt1.setOnFinished(e -> {
                // Mover token a la nueva pos y que caiga del cielo
                j.setPosicion(hasta);
                posicionarToken(j);
                double finalY = token.getTranslateY();
                token.setTranslateY(finalY - 600); // Lo sube arriba del cielo

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), token);
                fadeIn.setToValue(1);

                TranslateTransition caerCielo = new TranslateTransition(Duration.millis(500), token);
                caerCielo.setToY(finalY);
                caerCielo.setInterpolator(Interpolator.EASE_OUT);

                ParallelTransition pt2 = new ParallelTransition(fadeIn, caerCielo);
                pt2.setOnFinished(e2 -> {
                    // Restaurar pilar silenciosamente
                    TranslateTransition subirPilar = new TranslateTransition(Duration.millis(500), pilar);
                    subirPilar.setByY(-20);
                    subirPilar.play();

                    if (onComplete != null)
                        onComplete.run();
                });
                pt2.play();
            });
            pt1.play();

        } else if (tipo.contains("TRINEO") || tipo.contains("OSO") || tipo.contains("MOTO")) {
            // Pilar se mueve un poco hacia arriba para impulsar al jugador
            TranslateTransition impulsarPilar = new TranslateTransition(Duration.millis(150), pilar);
            impulsarPilar.setByY(-15);
            impulsarPilar.setAutoReverse(true);
            impulsarPilar.setCycleCount(2);
            impulsarPilar.setInterpolator(Interpolator.EASE_OUT);

            impulsarPilar.setOnFinished(e -> {
                animarSalto(j, desde, hasta, 800, onComplete);
            });
            impulsarPilar.play();

        } else {
            animarSalto(j, desde, hasta, 1000, onComplete);
        }
    }

    /**
     * Ejecuta una animación de salto parabólico entre dos casillas.
     * 
     * @param j          El jugador que salta.
     * @param desde      Casilla de origen.
     * @param hasta      Casilla de destino.
     * @param duracionMs Tiempo en milisegundos que dura el salto.
     * @param onComplete Acción tras aterrizar.
     */
    private void animarSalto(Jugador j, int desde, int hasta, int duracionMs, Runnable onComplete) {
        Node token = playerTokens.get(j);

        // Calcular posiciones exactas usando posicionarToken para evitar errores de
        // offset
        double startX = token.getTranslateX();
        double startY = token.getTranslateY();

        j.setPosicion(hasta);
        posicionarToken(j);
        double endX = token.getTranslateX();
        double endY = token.getTranslateY();

        j.setPosicion(desde); // Volver temporalmente
        posicionarToken(j);

        double dx = endX - startX;
        double dy = endY - startY;

        TranslateTransition move = new TranslateTransition(Duration.millis(duracionMs), token);
        move.setByX(dx);
        move.setByY(dy);
        move.setInterpolator(Interpolator.LINEAR);

        TranslateTransition jump = new TranslateTransition(Duration.millis(duracionMs / 2.0), token);
        jump.setByY(-120);
        jump.setCycleCount(2);
        jump.setAutoReverse(true);
        jump.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition pt = new ParallelTransition(move, jump);
        pt.setOnFinished(e -> {
            j.setPosicion(hasta);
            posicionarToken(j);
            if (onComplete != null)
                onComplete.run();
        });
        pt.play();
    }

    /**
     * Finaliza el turno del jugador actual y realiza las comprobaciones de
     * victoria.
     * Si la partida continúa, cede el turno al siguiente jugador y actualiza la
     * cámara.
     */
    private void finalizarTurno() {
        animacionEnCurso = false;

        juegoSimulado.setTurnoActual(turnoActual);

        // Se ha eliminado el auto-guardado por turno para optimizar rendimiento
        // y por petición del usuario.

        if (jugadores.get(turnoActual).getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            Jugador ganador = jugadores.get(turnoActual);
            log("¡" + ganador.getNombre() + " HA GANADO LA PARTIDA!");
            
            // Guardamos el ganador en el contexto global para la pantalla de victoria
            GameContext.getInstance().setWinner(ganador);
            
            // Pequeña pausa para que se vea el movimiento final antes de cambiar de escena
            PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
            pause.setOnFinished(e -> {
                Stage stage = (Stage) boardPane.getScene().getWindow();
                NavigationController.navigateTo(stage, "VictoryView.fxml");
            });
            pause.play();
        } else {
            turnoActual = (turnoActual + 1) % jugadores.size();
            actualizarUI();
            setControlesBloqueados(false);

            if (camera.isAutoMode()) {
                camera.smoothCenterOnPlayer(jugadores.get(turnoActual), 1.0);
            }

            comprobarBloqueoInicioTurno();
        }
    }

    /**
     * Verifica si el jugador del turno activo tiene bloqueos o es la CPU.
     * Si está bloqueado, lo salta automáticamente tras un breve retardo.
     */
    private void comprobarBloqueoInicioTurno() {
        Jugador j = jugadores.get(turnoActual);
        if (j.estaBloqueado()) {
            log(j.getNombre() + " está bloqueado. Saltando turno...");
            j.setTurnosBloqueados(j.getTurnosBloqueados() - 1);

            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> finalizarTurno());
            pause.play();
        } else if (j instanceof Foca) {
            setControlesBloqueados(true);
            ejecutarTurnoCPU((Foca) j);
        }
    }

    /**
     * Lógica de decisión para el turno de la Inteligencia Artificial.
     * 
     * @param foca La instancia de la Foca que debe decidir su acción.
     */
    private void ejecutarTurnoCPU(Foca foca) {
        log("La CPU (" + foca.getNombre() + ") está pensando...");

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> {
            int accion = Cpu.decidirAccion(foca, tablero);

            if (accion == Cpu.Accion.LANZAR_DADO) {
                log("La CPU decide lanzar el dado.");
                handleRollDice(null);
            } else if (accion == Cpu.Accion.RECOLECTAR_BOLAS) {
                log("La CPU decide recolectar bolas de nieve.");
                handleCollectSnowballs(null);
            } else if (accion == Cpu.Accion.USAR_DADO_RAPIDO) {
                log("La CPU decide usar un DADO RÁPIDO.");
                handleUseDadoRapido(null);
            } else if (accion == Cpu.Accion.USAR_DADO_LENTO) {
                log("La CPU decide usar un DADO LENTO.");
                handleUseDadoLento(null);
            }
        });
        delay.play();
    }

    /**
     * Actualiza todos los elementos de la interfaz de usuario para el turno actual.
     * Refresca los indicadores, el inventario, los estilos de botones y la música.
     */
    private void actualizarUI() {
        Jugador jActual = jugadores.get(turnoActual);

        mostrarOverlayCPU(jActual instanceof Foca);

        turnCircles.forEach((j, circle) -> {
            if (j == jActual) {
                DropShadow glow = new DropShadow();
                glow.setRadius(20);
                glow.setColor(Color.YELLOW);
                circle.setEffect(glow);
            } else {
                DropShadow ds = new DropShadow();
                ds.setRadius(5);
                ds.setColor(Color.BLACK);
                circle.setEffect(ds);
            }
        });

        secondaryStatusContainer.getChildren().clear();

        actualizarInventarioFlotante(jActual);

        aplicarEstiloPorJugador(jActual);

        for (Jugador p : jugadores) {
            VBox secondaryCard = crearTarjetaEstadoSecundaria(p);
            secondaryStatusContainer.getChildren().add(secondaryCard);
        }

        // Actualizamos la música dinámica según el jugador más avanzado
        recalcularMusicaDinamica();
    }

    /**
     * Ajusta la intensidad de la música de fondo basándose en la posición
     * del jugador más adelantado del tablero.
     */
    private void recalcularMusicaDinamica() {
        int maxPos = 0;
        if (jugadores != null) {
            for (Jugador j : jugadores) {
                if (j.getPosicion() > maxPos) {
                    maxPos = j.getPosicion();
                }
            }
        }
        util.SoundManager.updateGameMusicByPosition(maxPos);
    }

    /**
     * Inyecta estilos CSS dinámicos a los botones de acción para que coincidan
     * con el color del jugador que tiene el turno.
     * 
     * @param j El jugador activo.
     */
    private void aplicarEstiloPorJugador(Jugador j) {
        Color c = getColorFromString(j.getColor());

        Color border = c;
        Color fill = c.deriveColor(0, 1.0, 0.6, 1.0);
        Color hover = c.deriveColor(0, 1.0, 0.8, 1.0);

        String hexBorder = String.format("#%02X%02X%02X",
                (int) (border.getRed() * 255), (int) (border.getGreen() * 255), (int) (border.getBlue() * 255));

        String hexFill = String.format("#%02X%02X%02X",
                (int) (fill.getRed() * 255), (int) (fill.getGreen() * 255), (int) (fill.getBlue() * 255));

        String hexHover = String.format("#%02X%02X%02X",
                (int) (hover.getRed() * 255), (int) (hover.getGreen() * 255), (int) (hover.getBlue() * 255));

        String shadowHex = String.format("rgba(%d, %d, %d, 0.5)",
                (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));

        String style = "-fx-player-color-border: " + hexBorder + "; " +
                "-fx-player-color-fill: " + hexFill + "; " +
                "-fx-player-color-hover: " + hexHover + "; " +
                "-fx-player-color-shadow: " + shadowHex + ";";

        btnDado.setStyle(style);
        btnRecolectar.setStyle(style);
        btnInventario.setStyle(style);

        inventoryOverlayPanel.lookup(".inventory-floating-panel")
                .setStyle("-fx-border-color: " + hexBorder + "; -fx-border-width: 8 0 0 0;");
    }

    /**
     * Crea una tarjeta visual con el resumen de estado de un jugador para el panel
     * lateral.
     * 
     * @param j El jugador del que se desea crear la tarjeta.
     * @return Un VBox con el nombre, posición e inventario del jugador.
     */
    private VBox crearTarjetaEstadoSecundaria(Jugador j) {
        VBox card = new VBox(2);
        card.getStyleClass().add("secondary-player-card");

        Color c = getColorFromString(j.getColor());
        String hex = String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));

        card.setStyle("-fx-border-color: " + hex + ";");

        Label nameLabel = new Label(j.getNombre());
        nameLabel.getStyleClass().add("secondary-player-name");

        Label infoLabel = new Label("Pos: " + j.getPosicion() + " | Obj: " + j.getInventario().getCantidad("Total"));
        infoLabel.getStyleClass().add("secondary-player-info");

        card.getChildren().addAll(nameLabel, infoLabel);
        return card;
    }

    /**
     * Refresca el contenido del panel de inventario flotante.
     * 
     * @param j El jugador cuyo inventario se desea mostrar.
     */
    private void actualizarInventarioFlotante(Jugador j) {
        inventoryFlowPane.getChildren().clear();
        Inventario inv = j.getInventario();

        agregarIconoObjeto(inventoryFlowPane, "Pez", inv.getCantidad("Pez"), "PEZ", "inventory-item-pez");
        agregarIconoObjeto(inventoryFlowPane, "BolaNieve", inv.getCantidad("BolaNieve"), "BOLA NIEVE",
                "inventory-item-bola");
        agregarIconoObjeto(inventoryFlowPane, "DadoRapido", inv.getCantidad("DadoRapido"), "DADO RÁPIDO",
                "inventory-item-rapido");
        agregarIconoObjeto(inventoryFlowPane, "DadoLento", inv.getCantidad("DadoLento"), "DADO LENTO",
                "inventory-item-lento");

        if (inv.getCantidad("Total") == 0) {
            inventoryFlowPane.getChildren().add(new Label("La mochila está vacía..."));
        }
    }

    /**
     * Crea y añade un elemento visual al contenedor de inventario.
     * 
     * @param container  El contenedor FlowPane.
     * @param tipo       Identificador interno del tipo de objeto.
     * @param cantidad   Cantidad poseída.
     * @param nombre     Texto legible a mostrar.
     * @param styleClass Clase CSS para el estilo específico del icono.
     */
    private void agregarIconoObjeto(FlowPane container, String tipo, int cantidad, String nombre, String styleClass) {
        if (cantidad > 0) {
            HBox itemNode = new HBox();
            itemNode.setAlignment(Pos.CENTER_LEFT);
            itemNode.setSpacing(10);
            itemNode.setPadding(new Insets(5, 10, 5, 10));
            itemNode.getStyleClass().addAll("inventory-item", styleClass);
            itemNode.setPrefWidth(240);

            Label nameLabel = new Label(nombre);
            nameLabel.getStyleClass().add("inventory-item-name");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label countLabel = new Label("x" + cantidad);
            countLabel.getStyleClass().add("inventory-item-count-text");

            itemNode.getChildren().addAll(nameLabel, spacer, countLabel);

            itemNode.setOnMouseClicked(e -> prepararUsoObjeto(tipo));

            container.getChildren().add(itemNode);
        }
    }

    /**
     * Prepara el uso de un objeto del inventario abriendo un diálogo de
     * confirmación.
     * 
     * @param tipo El tipo de objeto que el usuario ha seleccionado.
     */
    private void prepararUsoObjeto(String tipo) {
        if (!animacionEnCurso) {
            this.selectedItemType = tipo;
            util.SoundManager.playConfirm();

            String nombreMsg = tipo;
            boolean esDado = tipo.startsWith("Dado");

            switch (tipo) {
                case "Pez":
                    nombreMsg = "un PEZ";
                    break;
                case "BolaNieve":
                    nombreMsg = "una BOLA DE NIEVE";
                    break;
                case "DadoRapido":
                    nombreMsg = "un DADO RÁPIDO";
                    break;
                case "DadoLento":
                    nombreMsg = "un DADO LENTO";
                    break;
            }

            itemConfirmTitle.setText("USAR " + tipo.toUpperCase());
            itemConfirmMessage.setText("¿Quieres usar " + nombreMsg + "?");

            if (!esDado) {
                itemConfirmSubMessage.setText("Este objeto se usa automáticamente en eventos.");
                itemConfirmSubMessage.setVisible(true);
                btnItemConfirmYes.setDisable(true);
                btnItemConfirmYes.setOpacity(0.5);
            } else {
                itemConfirmSubMessage.setVisible(false);
                btnItemConfirmYes.setDisable(false);
                btnItemConfirmYes.setOpacity(1.0);
            }

            animateIn(itemConfirmOverlay);
        }
    }

    /**
     * Confirma el uso del objeto seleccionado y ejecuta su acción.
     * 
     * @param event El evento de acción del botón "Sí".
     */
    @FXML
    private void handleItemConfirmYes(ActionEvent event) {
        if (selectedItemType != null) {
            util.SoundManager.playConfirm();
            String tipo = selectedItemType;
            animateOut(itemConfirmOverlay, () -> {
                if (isInventoryOpen)
                    handleCloseInventory();

                FadeTransition fadeBg = new FadeTransition(Duration.millis(300), overlayPane);
                fadeBg.setToValue(0);
                fadeBg.setOnFinished(ev -> overlayPane.setVisible(false));
                fadeBg.play();

                if (tipo.equals("DadoRapido")) {
                    handleUseDadoRapido(null);
                } else if (tipo.equals("DadoLento")) {
                    handleUseDadoLento(null);
                }
            });
        }
    }

    /**
     * Cancela el uso del objeto y cierra el diálogo.
     * 
     * @param event El evento de acción del botón "No".
     */
    @FXML
    private void handleItemConfirmNo(ActionEvent event) {
        util.SoundManager.playBack();
        animateOut(itemConfirmOverlay, null);
    }

    /**
     * Bloquea o desbloquea los botones principales de la interfaz.
     * Se usa durante las animaciones para evitar entradas de usuario conflictivas.
     * 
     * @param bloqueado true para desactivar, false para activar.
     */
    private void setControlesBloqueados(boolean bloqueado) {
        btnDado.setDisable(bloqueado);
        btnRecolectar.setDisable(bloqueado);
        btnInventario.setDisable(bloqueado);
        logContentBox.setDisable(bloqueado);
    }

    /**
     * Registra un mensaje en el área de historial de la partida (Log).
     * 
     * @param msg El texto a mostrar.
     */
    public void log(String msg) {
        gameLogArea.appendText("► " + msg + "\n");
    }

    // --- GESTIÓN DE MENÚS, OVERLAYS Y PANELES ---

    /**
     * Alterna la visibilidad del panel lateral de historial (Log).
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleToggleLog(ActionEvent event) {
        util.SoundManager.playConfirm();
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), logPanelContainer);
        if (isLogOpen) {
            tt.setToX(-logContentBox.getWidth());
            btnToggleLog.setText("▶");
            isLogOpen = false;
        } else {
            tt.setToX(0);
            btnToggleLog.setText("◀");
            isLogOpen = true;
        }
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
    }

    /**
     * Alterna la visibilidad del menú de pausa principal.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleToggleMenu(ActionEvent event) {
        util.SoundManager.playConfirm();
        syncOptionsOverlayValues();
        animateIn(menuPausa);
    }

    /**
     * Reanuda la partida cerrando el menú de pausa.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleResume(ActionEvent event) {
        util.SoundManager.playConfirm();
        animateOut(menuPausa, null);
    }

    /**
     * Muestra el panel de opciones de configuración.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleShowOptions(ActionEvent event) {
        util.SoundManager.playConfirm();
        animateOut(menuPausa, () -> {
            syncOptionsOverlayValues();
            animateIn(optionsOverlay);
        });
    }

    /**
     * Vuelve al menú de pausa desde el panel de opciones.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleBackFromOptions(ActionEvent event) {
        util.SoundManager.playBack();
        animateOut(optionsOverlay, () -> animateIn(menuPausa));
    }

    /**
     * Inicia el proceso para volver al menú principal, solicitando confirmación.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleGoToMainMenu(ActionEvent event) {
        util.SoundManager.playConfirm();
        pendingAction = GameContext.ActionConfirmType.LOGOUT;
        confirmTitle.setText("VOLVER AL MENÚ");
        confirmMessage
                .setText("¿Seguro que quieres volver al menú principal?\nSe guardará tu progreso automáticamente.");

        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    /**
     * Inicia el proceso para salir del juego, solicitando confirmación.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleExitGame(ActionEvent event) {
        util.SoundManager.playConfirm();
        pendingAction = GameContext.ActionConfirmType.QUIT;
        confirmTitle.setText("SALIR DEL JUEGO");
        confirmMessage.setText("¿Seguro que quieres cerrar el juego?\nSe guardará tu progreso antes de salir.");

        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    /**
     * Muestra el diálogo de confirmación para guardar la partida manualmente.
     */
    @FXML
    private void handleSavePartida() {
        util.SoundManager.playConfirm();
        pendingAction = GameContext.ActionConfirmType.SAVE;

        confirmTitle.setText("GUARDAR PARTIDA");
        confirmMessage.setText("¿Deseas guardar el estado actual de la partida?");
        btnConfirmYes.setText("SÍ, GUARDAR");
        btnConfirmYes.getStyleClass().remove("button-danger");
        btnConfirmYes.getStyleClass().add("button-primary");

        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    /**
     * Ejecuta la acción confirmada (salir, volver al menú o guardar) guardando la partida.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleConfirmYes(ActionEvent event) {
        util.SoundManager.playConfirm();

        if (pendingAction == GameContext.ActionConfirmType.SAVE) {
            int idPartida = tablero.getIdPartida();
            if (idPartida > 0) {
                boolean exito = bbdd.guardarEstadoCompleto(idPartida, juegoSimulado);
                if (exito) {
                    mostrarNotificacionEvento("PARTIDA GUARDADA CON ÉXITO", jugadores.get(turnoActual));
                    animateOut(confirmOverlay, null); // Volver a la partida directamente cerrando el overlay de confirmación
                } else {
                    mostrarNotificacionEvento("ERROR AL GUARDAR PARTIDA", jugadores.get(turnoActual));
                }
            }
            return;
        }

        // GUARDADO MANUAL ANTES DE SALIR
        int idPartida = tablero.getIdPartida();
        if (idPartida > 0) {
            System.out.println("► Guardando partida #" + idPartida + " antes de salir...");
            bbdd.guardarEstadoCompleto(idPartida, juegoSimulado);
        }

        if (pendingAction == GameContext.ActionConfirmType.LOGOUT) {
            NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
        } else if (pendingAction == GameContext.ActionConfirmType.QUIT) {
            Platform.exit();
        }
    }

    /**
     * Cancela la acción crítica y vuelve al menú de pausa.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleConfirmNo(ActionEvent event) {
        util.SoundManager.playBack();
        
        // Restauramos el estilo por defecto del botón de confirmación por si acaso
        btnConfirmYes.setText("SÍ, SALIR");
        btnConfirmYes.getStyleClass().remove("button-primary");
        if (!btnConfirmYes.getStyleClass().contains("button-danger")) {
            btnConfirmYes.getStyleClass().add("button-danger");
        }

        animateOut(confirmOverlay, () -> animateIn(menuPausa));
    }

    /**
     * Resetea la cámara a su posición automática centrada en el jugador actual.
     */
    @FXML
    private void handleResetCamera() {
        util.SoundManager.playConfirm();
        camera.setAutoMode(true); // Reactivar modo automático
        camera.smoothCenterOnPlayer(jugadores.get(turnoActual), 1.0);
    }

    /**
     * Muestra una notificación visual efímera en el centro de la pantalla.
     * 
     * @param mensaje El texto a mostrar.
     * @param j       El jugador relacionado con el evento (para el color del
     *                brillo).
     */
    private void mostrarNotificacionEvento(String mensaje, Jugador j) {
        if (eventNotificationLabel != null) {
            Color c = getColorFromString(j.getColor());
            String hex = String.format("#%02X%02X%02X",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255));

            eventNotificationLabel.setText(mensaje.toUpperCase());
            eventNotificationLabel.setStyle("-fx-effect: dropshadow(three-pass-box, " + hex + ", 15, 0.5, 0, 0);");
            eventNotificationLabel.setVisible(true);
            eventNotificationLabel.setOpacity(0);
            eventNotificationLabel.setTranslateY(-50);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), eventNotificationLabel);
            fadeIn.setToValue(1);

            TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), eventNotificationLabel);
            slideDown.setToY(0);

            PauseTransition stay = new PauseTransition(Duration.seconds(2));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), eventNotificationLabel);
            fadeOut.setToValue(0);

            SequentialTransition seq = new SequentialTransition(
                    new ParallelTransition(fadeIn, slideDown),
                    stay,
                    fadeOut);
            seq.setOnFinished(e -> eventNotificationLabel.setVisible(false));
            seq.play();
        }
    }

    /**
     * Abre un diálogo de evento bloqueante que resalta a los jugadores implicados.
     * 
     * @param titulo       El título del evento.
     * @param mensaje      Descripción de lo ocurrido.
     * @param onComplete   Acción a realizar al cerrar el diálogo.
     * @param involucrados Lista de jugadores que se deben resaltar.
     */
    private void mostrarEventoDialogo(String titulo, String mensaje, Runnable onComplete, Jugador... involucrados) {
        eventTitleLabel.setText(titulo);
        eventMessageLabel.setText(mensaje);
        onEventContinue = onComplete;

        highlightingBackups.clear();
        highlightLayer.getChildren().clear();

        for (Jugador j : involucrados) {
            ImageView token = playerTokens.get(j);
            if (token != null) {
                // Cast explícito a Pane para evitar que el compilador se queje de visibilidad
                Pane originalParent = (Pane) token.getParent();
                highlightingBackups.put(j, originalParent);

                // Coordenadas globales
                javafx.geometry.Bounds bounds = token.localToScene(token.getBoundsInLocal());

                // Mover ficha de capa
                originalParent.getChildren().remove(token);
                highlightLayer.getChildren().add(token);

                // Reposicionar visualmente
                javafx.geometry.Point2D localP = highlightLayer.sceneToLocal(bounds.getMinX(), bounds.getMinY());
                token.setTranslateX(localP.getX() + token.getFitWidth() / 2);
                token.setTranslateY(localP.getY() + token.getFitHeight() / 2);

                token.setEffect(new DropShadow(25, Color.WHITE));
            }
        }
        // Limpiar interferencias de otras capas
        overlayPane.setVisible(false);

        eventOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), eventOverlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        animateIn(eventDialogueBox);
    }

    /**
     * Cierra el diálogo de evento y restaura las fichas de los jugadores a su capa
     * original.
     */
    @FXML
    private void handleCloseEvent() {
        if (!eventOverlay.isVisible() || eventOverlay.getOpacity() < 1.0) {
            return; // Ya se está cerrando, ignorar múltiples clics
        }

        util.SoundManager.playBack();

        // Restaurar fichas
        for (Map.Entry<Jugador, Pane> entry : highlightingBackups.entrySet()) {
            Jugador j = entry.getKey();
            Pane originalParent = entry.getValue();
            ImageView token = playerTokens.get(j);

            highlightLayer.getChildren().remove(token);
            token.setEffect(null);
            if (!originalParent.getChildren().contains(token)) {
                originalParent.getChildren().add(token);
            }
            posicionarToken(j);
        }
        highlightingBackups.clear();

        FadeTransition ft = new FadeTransition(Duration.millis(300), eventOverlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            eventOverlay.setVisible(false);
            if (onEventContinue != null) {
                Runnable action = onEventContinue;
                onEventContinue = null; // Prevenir doble ejecución
                action.run();
            }
        });
        ft.play();

        animateOut(eventDialogueBox, null);
    }

    // --- LÓGICA DE ANIMACIONES ---

    /**
     * Ejecuta una animación de entrada suave para un panel u overlay.
     * 
     * @param content El nodo visual a animar.
     */
    private void animateIn(Node content) {
        // Ocultar otros posibles overlays activos para evitar superposiciones
        if (menuPausa != null && menuPausa != content)
            menuPausa.setVisible(false);
        if (optionsOverlay != null && optionsOverlay != content)
            optionsOverlay.setVisible(false);
        if (confirmOverlay != null && confirmOverlay != content)
            confirmOverlay.setVisible(false);
        if (itemConfirmOverlay != null && itemConfirmOverlay != content)
            itemConfirmOverlay.setVisible(false);

        // Si mostramos un menú de sistema, ocultamos la capa de eventos
        if (content != eventDialogueBox)
            eventOverlay.setVisible(false);

        overlayPane.setVisible(true);
        // Si el fondo ya es visible (cambio entre menús), no lo re-animamos
        if (overlayPane.getOpacity() < 0.1) {
            FadeTransition fadeBg = new FadeTransition(Duration.millis(300), overlayPane);
            fadeBg.setFromValue(0);
            fadeBg.setToValue(1);
            fadeBg.play();
        }

        content.setVisible(true);
        content.setOpacity(0);
        content.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), content);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), content);
        slideUp.setToY(0);

        ParallelTransition pt = new ParallelTransition(fadeIn, slideUp);
        pt.setInterpolator(Interpolator.EASE_OUT);
        pt.play();
    }

    /**
     * Ejecuta una animación de salida suave para un panel u overlay.
     * 
     * @param content    El nodo visual a animar.
     * @param onFinished Acción opcional al terminar la animación.
     */
    private void animateOut(Node content, Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), content);
        fadeOut.setToValue(0);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), content);
        slideDown.setToY(30);

        ParallelTransition pt = new ParallelTransition(fadeOut, slideDown);
        pt.setInterpolator(Interpolator.EASE_IN);

        pt.setOnFinished(e -> {
            content.setVisible(false);
            if (onFinished != null) {
                onFinished.run();
            } else {
                // Si no hay siguiente acción, es que cerramos el menú completo
                FadeTransition fadeBg = new FadeTransition(Duration.millis(300), overlayPane);
                fadeBg.setToValue(0);
                fadeBg.setOnFinished(ev -> overlayPane.setVisible(false));
                fadeBg.play();
            }
        });
        pt.play();
    }

    // --- LÓGICA DE OPCIONES INTEGRADA ---

    /**
     * Inicializa los controles de configuración dentro del overlay.
     */
    private void initOptionsOverlay() {
        resComboOverlay.getItems().addAll("1280x720", "1600x900", "1920x1080");
        syncOptionsOverlayValues();

        resComboOverlay.valueProperty().addListener((o, old, n) -> checkOptionsDirty());
        fsCheckOverlay.selectedProperty().addListener((o, old, n) -> checkOptionsDirty());
        musicSliderOverlay.valueProperty().addListener((o, old, n) -> {
            musicLabelOverlay.setText(Math.round(n.doubleValue()) + "%");
            checkOptionsDirty();
        });
        sfxSliderOverlay.valueProperty().addListener((o, old, n) -> {
            sfxLabelOverlay.setText(Math.round(n.doubleValue()) + "%");
            checkOptionsDirty();
        });
    }

    /**
     * Sincroniza los valores de los controles con la configuración actual del
     * sistema.
     */
    private void syncOptionsOverlayValues() {
        SettingsManager sm = SettingsManager.getInstance();
        initialResolution = sm.getResolution();
        initialFullscreen = sm.isFullscreen();
        initialMusic = sm.getMusicVolume() * 100;
        initialSfx = sm.getSfxVolume() * 100;

        resComboOverlay.setValue(initialResolution);
        fsCheckOverlay.setSelected(initialFullscreen);
        musicSliderOverlay.setValue(initialMusic);
        sfxSliderOverlay.setValue(initialSfx);
        musicLabelOverlay.setText(Math.round(initialMusic) + "%");
        sfxLabelOverlay.setText(Math.round(initialSfx) + "%");

        btnApplyOverlay.setDisable(true);
        btnApplyOverlay.getStyleClass().remove("button-dirty");

        // Reset Seguridad Overlay
        if (GameContext.getInstance().getCurrentUser() != null) {
            usernameLabelOverlay.setText(GameContext.getInstance().getCurrentUser().getNombre());
        }
        hidePassFormOverlay();
    }

    /**
     * Verifica si ha habido cambios en la configuración para habilitar el botón de
     * aplicar.
     */
    private void checkOptionsDirty() {
        boolean changed = !resComboOverlay.getValue().equals(initialResolution) ||
                fsCheckOverlay.isSelected() != initialFullscreen ||
                Math.abs(musicSliderOverlay.getValue() - initialMusic) > 0.1 ||
                Math.abs(sfxSliderOverlay.getValue() - initialSfx) > 0.1;

        btnApplyOverlay.setDisable(!changed);
        if (changed) {
            if (!btnApplyOverlay.getStyleClass().contains("button-dirty")) {
                btnApplyOverlay.getStyleClass().add("button-dirty");
            }
        } else {
            btnApplyOverlay.getStyleClass().remove("button-dirty");
        }
    }

    /**
     * Guarda y aplica los cambios realizados en el panel de opciones.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleApplyOptionsOverlay(ActionEvent event) {
        util.SoundManager.playConfirm();
        SettingsManager sm = SettingsManager.getInstance();
        sm.setResolution(resComboOverlay.getValue());
        sm.setFullscreen(fsCheckOverlay.isSelected());
        sm.setMusicVolume(musicSliderOverlay.getValue() / 100.0);
        sm.setSfxVolume(sfxSliderOverlay.getValue() / 100.0);
        sm.save();

        initialResolution = sm.getResolution();
        initialFullscreen = sm.isFullscreen();
        initialMusic = musicSliderOverlay.getValue();
        initialSfx = sfxSliderOverlay.getValue();
        checkOptionsDirty();

        Stage stage = (Stage) rootStack.getScene().getWindow();
        stage.setFullScreen(sm.isFullscreen());
        if (!sm.isFullscreen()) {
            String[] res = sm.getResolution().split("x");
            stage.setWidth(Double.parseDouble(res[0]));
            stage.setHeight(Double.parseDouble(res[1]));
            stage.centerOnScreen();
        }
        util.SoundManager.setSfxVolume(sm.getSfxVolume());
        util.SoundManager.setMusicVolume(sm.getMusicVolume());
    }

    /**
     * Atajo para volver atrás o cerrar menús.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        handleToggleMenu(event); // Reutilizamos el nuevo sistema
    }

    // --- Helpers ---

    /**
     * Convierte una cadena de texto de color en un objeto Color de JavaFX.
     * 
     * @param colorRef Nombre del color o código hexadecimal.
     * @return El objeto {@link Color} correspondiente.
     */
    private Color getColorFromString(String colorRef) {
        if (colorRef == null)
            return Color.BLUE;
        switch (colorRef.toLowerCase()) {
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
                try {
                    return Color.web(colorRef);
                } catch (Exception e) {
                    return Color.DARKBLUE;
                }
        }
    }

    /**
     * Abre el panel de inventario con una animación de deslizamiento desde abajo.
     */
    @FXML
    private void handleOpenInventory() {
        if (!isInventoryOpen) {
            util.SoundManager.playConfirm();
            isInventoryOpen = true;

            inventoryOverlayPanel.setVisible(true);
            inventoryOverlayPanel.setTranslateY(400); // Empezamos desde abajo (fuera de vista)
            inventoryOverlayPanel.setOpacity(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(350), inventoryOverlayPanel);
            fadeIn.setToValue(1);

            TranslateTransition slideUp = new TranslateTransition(Duration.millis(350), inventoryOverlayPanel);
            slideUp.setToY(0);

            ParallelTransition pt = new ParallelTransition(fadeIn, slideUp);
            pt.setInterpolator(Interpolator.EASE_OUT);
            pt.setOnFinished(e -> setControlesBloqueados(true)); // Bloqueamos los de abajo para que no se pisen
            pt.play();
        }
    }

    /**
     * Cierra el panel de inventario.
     */
    @FXML
    private void handleCloseInventory() {
        if (isInventoryOpen) {
            util.SoundManager.playBack();
            isInventoryOpen = false;

            setControlesBloqueados(false); // Desbloqueamos antes de la animación para mejor respuesta

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), inventoryOverlayPanel);
            fadeOut.setToValue(0);

            TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), inventoryOverlayPanel);
            slideDown.setToY(400);

            ParallelTransition pt = new ParallelTransition(fadeOut, slideDown);
            pt.setInterpolator(Interpolator.EASE_IN);
            pt.setOnFinished(e -> inventoryOverlayPanel.setVisible(false));
            pt.play();
        }
    }

    /**
     * Muestra u oculta el indicador visual de que es el turno de la CPU.
     * 
     * @param mostrar true para mostrar, false para ocultar.
     */
    private void mostrarOverlayCPU(boolean mostrar) {
        if (cpuTurnOverlay != null) {
            if (mostrar && !cpuTurnOverlay.isVisible()) {
                cpuTurnOverlay.setVisible(true);
                cpuTurnOverlay.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(400), cpuTurnOverlay);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            } else if (!mostrar && cpuTurnOverlay.isVisible()) {
                FadeTransition ft = new FadeTransition(Duration.millis(400), cpuTurnOverlay);
                ft.setFromValue(cpuTurnOverlay.getOpacity());
                ft.setToValue(0);
                ft.setOnFinished(e -> cpuTurnOverlay.setVisible(false));
                ft.play();
            }
        }
    }
    // --- GESTIÓN DE SEGURIDAD EN OVERLAY ---

    @FXML
    private void showPassFormOverlay() {
        passInfoOverlay.setVisible(false);
        passFormOverlay.setVisible(true);
        passErrorLabelOverlay.setText("");
        newPassFieldOverlay.clear();
        confirmPassFieldOverlay.clear();
    }

    @FXML
    private void hidePassFormOverlay() {
        passInfoOverlay.setVisible(true);
        passFormOverlay.setVisible(false);
    }

    @FXML
    private void handleConfirmPassOverlay() {
        String pass = newPassFieldOverlay.getText();
        String confirm = confirmPassFieldOverlay.getText();

        if (pass.isEmpty()) {
            passErrorLabelOverlay.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px;");
            passErrorLabelOverlay.setText("La contraseña no puede estar vacía.");
            return;
        }

        if (!pass.equals(confirm)) {
            passErrorLabelOverlay.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px;");
            passErrorLabelOverlay.setText("Las contraseñas no coinciden.");
            return;
        }

        int userId = GameContext.getInstance().getCurrentUser().getId();
        boolean success = bbdd.cambiarContrasenaJugador(userId, pass);

        if (success) {
            passErrorLabelOverlay.setStyle("-fx-text-fill: #388e3c; -fx-font-size: 14px; -fx-font-weight: bold;");
            passErrorLabelOverlay.setText("¡Contraseña cambiada exitosamente!");
            // Limpiar los campos por seguridad
            newPassFieldOverlay.clear();
            confirmPassFieldOverlay.clear();
        } else {
            passErrorLabelOverlay.setStyle("-fx-text-fill: #d32f2f; -fx-font-size: 14px;");
            passErrorLabelOverlay.setText("Error al conectar con la base de datos.");
        }
    }
}
