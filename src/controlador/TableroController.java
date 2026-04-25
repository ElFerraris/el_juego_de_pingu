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

/**
 * TableroController
 * 
 * Gestiona el renderizado, movimiento animado y lógica de turnos.
 */
public class TableroController {

    @FXML private Pane boardPane;
    @FXML private Pane cameraViewport;
    @FXML private Group zoomGroup;
    @FXML private HBox turnIndicatorBox;
    @FXML private Button btnDado;
    @FXML private Button btnRecolectar;
    @FXML private Button btnInventario;
    @FXML private Button btnMenu;
    
    // Capas de animación
    @FXML private VBox diceAnimationContainer;
    @FXML private ImageView diceImageView;
    @FXML private Label diceNumberLabel;
    @FXML private Label dadoResultadoLabel;
    @FXML private TextArea gameLogArea;
    
    // Panel lateral de Log
    @FXML private HBox logPanelContainer;
    @FXML private VBox logContentBox;
    @FXML private Button btnToggleLog;
    private boolean isLogOpen = false;

    // PANEL DE ESTADO SECUNDARIO (En el Log)
    @FXML private VBox secondaryStatusContainer;

    // INVENTARIO FLOTANTE
    @FXML private VBox inventoryOverlayPanel;
    @FXML private FlowPane inventoryFlowPane;
    private boolean isInventoryOpen = false;

    
    // CAPA DE EVENTOS
    @FXML private StackPane eventOverlay;
    @FXML private Pane eventDimmer;
    @FXML private Pane highlightLayer;
    @FXML private VBox eventDialogueBox;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventMessageLabel;
    @FXML private Button btnDadoLento;
    @FXML private Button btnDadoRapido;

    // --- ELEMENTOS DE RAÍZ Y OVERLAY ---
    @FXML private StackPane rootStack;
    @FXML private StackPane overlayPane;
    @FXML private VBox menuPausa;
    @FXML private VBox optionsOverlay;
    @FXML private VBox confirmOverlay;
    
    // Controles de Opciones en Overlay
    @FXML private ComboBox<String> resComboOverlay;
    @FXML private CheckBox fsCheckOverlay;
    @FXML private Slider musicSliderOverlay;
    @FXML private Slider sfxSliderOverlay;
    @FXML private Button btnApplyOverlay;
    
    // Controles de Confirmación de Objetos
    @FXML private VBox itemConfirmOverlay;
    @FXML private Label itemConfirmTitle;
    @FXML private Label itemConfirmMessage;
    @FXML private Label itemConfirmSubMessage;
    @FXML private Button btnItemConfirmYes;

    private String selectedItemType = null;
    
    // Controles de Confirmación
    @FXML private Label confirmTitle;
    @FXML private Label confirmMessage;

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
    private Map<Jugador, Circle> playerTokens = new HashMap<>(); // Fichas en el tablero
    private Map<Jugador, Circle> turnCircles = new HashMap<>();  // Círculos del indicador superior
    private Pane tokensPane = new Pane(); // Capa superior para que los jugadores no queden detrás
    
    // ESTADO DE EVENTOS
    private Map<Jugador, Pane> highlightingBackups = new HashMap<>();
    private Runnable onEventContinue;
    
    // ESTADO DE CÁMARA
    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private double zoomFactor = 1.0;
    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 3.0;
    
    // CONTROL AUTOMÁTICO DE CÁMARA
    private boolean cameraAutoMode = true;
    private TranslateTransition cameraTransition;
    @FXML private StackPane eventNotificationContainer;
    @FXML private Label eventNotificationLabel;
    @FXML private StackPane cpuTurnOverlay;
    
    private final BBDD bbdd = new BBDD();
    private Juego juegoSimulado = new Juego();

    @FXML
    public void initialize() {
        // Iniciamos el sistema de música dinámica para la partida
        util.SoundManager.startGameMusic();

        // Inicialización de Opciones Overlay
        initOptionsOverlay();
        initCamera();
        
        // Esconder panel de log al inicio
        logPanelContainer.setTranslateX(-320);
        
        this.tablero = new Tablero();
        String seed = GameContext.getInstance().getSeed();
        tablero.introducirSeed(seed);
        
        // Asignamos el nombre de la partida
        juegoSimulado.setNombrePartida(GameContext.getInstance().getGameName());
        
        this.jugadores = GameContext.getInstance().getConfiguredPlayers();
        
        // Centrar en el primer jugador
        if (jugadores != null && !jugadores.isEmpty()) {
            smoothCenterOnPlayer(jugadores.get(0), 1.0);
        } else {
            centrarTablero();
        }

        // Centrar tablero automáticamente al conocer el tamaño
        cameraViewport.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) this.centrarTablero();
        });
        cameraViewport.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) this.centrarTablero();
        });
        
        juegoSimulado.setTablero(this.tablero);
        juegoSimulado.getJugadores().addAll(this.jugadores);

        if (!GameContext.getInstance().isPartidaCargada()) {
            // Juego Nuevo -> Guardar en BBDD
            for (Jugador j : jugadores) {
                int newId = bbdd.registrarJugadorSiNoExiste(j);
                if (newId != -1) j.setId(newId);
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

    private void dibujarTablero() {
        boardPane.getChildren().clear();
        casillaNodes.clear();
        
        // Inicializamos la capa de tokens y la añadimos al final (encima de todo)
        tokensPane.getChildren().clear();
        tokensPane.setPickOnBounds(false); // Para que no bloquee clics en el tablero
        
        List<StackPane> sortedNodes = new java.util.ArrayList<>();
        
        double centerX = 1200.0; 
        double bottomY = 1500.0; // Bajamos más el inicio para que quepa la escalera hacia arriba
        
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
                    zOrder = 7 + 7;
                    double stepHeight = 49 * 2.0;
                    layoutX = centerX + (7 - 7) * xOffset - 55;
                    layoutY = bottomY - (7 + 7) * yOffset - stepHeight - 150 - 12;
                }
                
                cellNode.setUserData(zOrder);
                cellNode.setLayoutX(layoutX);
                cellNode.setLayoutY(layoutY);
                
                casillaNodes.put(i, cellNode);
                sortedNodes.add(cellNode);
            }
        }
        
        sortedNodes.sort((a, b) -> Integer.compare((int) b.getUserData(), (int) a.getUserData()));
        
        boardPane.getChildren().addAll(sortedNodes);
        boardPane.getChildren().add(tokensPane); // Los jugadores siempre encima
    }

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


    private void crearFichasJugadores() {
        for (Jugador j : jugadores) {
            Circle token = new Circle(12);
            token.setFill(getColorFromString(j.getColor()));
            token.setStroke(Color.WHITE);
            token.getStyleClass().add("player-token");
            playerTokens.put(j, token);
        }
        // Posicionamos a todos (por si hay varios en la 0)
        for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
            actualizarPosicionesJugadoresEnCasilla(i);
        }
    }

    private void posicionarToken(Jugador j) {
        actualizarPosicionesJugadoresEnCasilla(j.getPosicion());
    }

    private void actualizarPosicionesJugadoresEnCasilla(int pos) {
        List<Jugador> jugadoresEnCasilla = new ArrayList<>();
        for (Jugador j : jugadores) {
            if (j.getPosicion() == pos) {
                jugadoresEnCasilla.add(j);
            }
        }

        StackPane cell = casillaNodes.get(pos);
        if (cell == null || jugadoresEnCasilla.isEmpty()) return;

        double cellX = cell.getLayoutX();
        double cellY = cell.getLayoutY();

        int num = jugadoresEnCasilla.size();
        for (int k = 0; k < num; k++) {
            Jugador j = jugadoresEnCasilla.get(k);
            Circle token = playerTokens.get(j);

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
                    if (k == 0) { offsetX = 0; offsetY = -15; }
                    else if (k == 1) { offsetX = -18; offsetY = 5; }
                    else { offsetX = 18; offsetY = 5; }
                    break;
                case 4:
                    // Cuatro jugadores: Cuadrado / Diamante
                    if (k == 0) { offsetX = -18; offsetY = -12; }
                    else if (k == 1) { offsetX = 18; offsetY = -12; }
                    else if (k == 2) { offsetX = -18; offsetY = 12; }
                    else { offsetX = 18; offsetY = 12; }
                    break;
            }

            // 55 es el centro horizontal del pilar (110/2)
            // 35 es el ajuste de altura para que los pingüinos queden centrados en la parte superior
            double targetX = cellX + 55 + offsetX;
            double targetY = cellY + 35 + offsetY;

            token.setTranslateX(targetX);
            token.setTranslateY(targetY);

            if (!tokensPane.getChildren().contains(token)) {
                tokensPane.getChildren().add(token);
            }
        }
    }

    private void crearIndicadorTurnos() {
        turnIndicatorBox.getChildren().clear();
        turnCircles.clear();
        
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            
            Circle circle = new Circle(18);
            circle.setFill(getColorFromString(j.getColor()));
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
            
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

    @FXML
    private void handleRollDice(ActionEvent event) {
        if (!animacionEnCurso) {
            int pasos = (int)(Math.random() * 6) + 1;
            ejecutarTurno(pasos);
        }
    }

    @FXML
    private void handleCollectSnowballs(ActionEvent event) {
        if (!animacionEnCurso) {
            Jugador jActual = jugadores.get(turnoActual);
            int actuales = jActual.getInventario().getCantidad("BolaNieve");
            
            if (actuales >= Inventario.MAX_BOLAS_NIEVE) {
                log(jActual.getNombre() + " ya tiene el máximo de bolas de nieve (" + Inventario.MAX_BOLAS_NIEVE + ").");
                if (jActual instanceof Foca) {
                    finalizarTurno();
                }
            } else {
                int cantidadAIntentar = (int)(Math.random() * 3) + 1; 
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

    @FXML
    private void handleUseDadoLento(ActionEvent event) {
        if (!animacionEnCurso) {
            Jugador jActual = jugadores.get(turnoActual);
            if (jActual.getInventario().tieneObjeto("DadoLento")) {
                jActual.getInventario().usarDadoEspecifico("Lento", jActual);
                int pasos = (int)(Math.random() * 3) + 1;
                ejecutarTurno(pasos);
            } else {
                log(jActual.getNombre() + " no tiene Dados Lentos.");
            }
        }
    }

    @FXML
    private void handleUseDadoRapido(ActionEvent event) {
        if (!animacionEnCurso) {
            Jugador jActual = jugadores.get(turnoActual);
            if (jActual.getInventario().tieneObjeto("DadoRapido")) {
                jActual.getInventario().usarDadoEspecifico("Rapido", jActual);
                int pasos = (int)(Math.random() * 6) + 3;
                ejecutarTurno(pasos);
            } else {
                log(jActual.getNombre() + " no tiene Dados Rápidos.");
            }
        }
    }

    private void ejecutarTurno(int pasos) {
        if (!animacionEnCurso) {
            animacionEnCurso = true; 
            
            Jugador jActual = jugadores.get(turnoActual);
            dadoResultadoLabel.setText("Último dado: " + pasos);
            log(jActual.getNombre() + " saca un " + pasos);
            
            setControlesBloqueados(true);

            try {
                Image rollingGif = new Image(getClass().getResource("/assets/tablero/dados/dado_rodando.gif").toExternalForm());
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

                    moverJugadorAnimado(jActual, pasos, () -> {
                        procesarEfectosCasilla(jActual);
                    });
                });
                moveDelay.play();
            });
            rollPause.play();
        }
    }

    private void moverJugadorAnimado(Jugador j, int pasosRestantes, Runnable onComplete) {
        if (pasosRestantes <= 0 || j.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            diceNumberLabel.setText("0");
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), diceAnimationContainer);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                diceAnimationContainer.setVisible(false);
                onComplete.run();
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

            if (cameraAutoMode) {
                smoothCenterOnPlayer(j, 0.4);
            }

            PauseTransition pause = new PauseTransition(Duration.millis(350));
            pause.setOnFinished(e -> {
                moverJugadorAnimado(j, pasosRestantes - 1, onComplete);
            });
            pause.play();
        }
    }

    private void procesarEfectosCasilla(Jugador j) {
        PauseTransition effectDelay = new PauseTransition(Duration.seconds(1));
        effectDelay.setOnFinished(ev -> {
            int posAntes = j.getPosicion();
            
            String logEfecto = tablero.aplicarEfectoCasilla(j);
            if (logEfecto != null && !logEfecto.isEmpty()) {
                log(logEfecto.replace("\n", " "));
            }
            
            int posDespues = j.getPosicion();

            if (posAntes != posDespues) {
                String tipo = tablero.getCasilla(posAntes).getTipo().replace("Casilla ", "");
                mostrarNotificacionEvento("¡" + tipo + "!", j);
                
                log("¡Efecto! " + j.getNombre() + " se mueve a la casilla " + posDespues);
                moverFichaDirecta(j, posAntes, posDespues);
                
                PauseTransition wait = new PauseTransition(Duration.seconds(1));
                wait.setOnFinished(e -> comprobarBatallaYFinalizarTurno(j));
                wait.play();
            } else {
                comprobarBatallaYFinalizarTurno(j);
            }
        });
        effectDelay.play();
    }

    private void comprobarBatallaYFinalizarTurno(Jugador jActual) {
        if (jActual.getPosicion() <= 0 || jActual.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            finalizarTurno();
        } else {
            Jugador oponente = null;
            for (Jugador p : jugadores) {
                if (oponente == null && p != jActual && p.getPosicion() == jActual.getPosicion()) {
                    oponente = p;
                }
            }

            if (oponente != null) {
                log("¡COLISIÓN en casilla " + jActual.getPosicion() + "!");
                mostrarNotificacionEvento("¡COLISIÓN!", jActual);
                resolverCombate(jActual, oponente);
            } else {
                finalizarTurno();
            }
        }
    }

    private void resolverCombate(Jugador atacante, Jugador defensor) {
        boolean esFoca = (atacante instanceof Foca || defensor instanceof Foca);
        String titulo = esFoca ? "¡Ataque de la Foca Loca!" : "¡Guerra de Bolas de Nieve!";
        StringBuilder mensaje = new StringBuilder();

        if (esFoca) {
            Jugador humano = (atacante instanceof Foca) ? defensor : atacante;
            Foca foca = (Foca) ((atacante instanceof Foca) ? atacante : defensor);
            
            mensaje.append("¡La Foca Loca choca con ").append(humano.getNombre()).append("!\n\n");
            
            if (humano.getInventario().tieneObjeto("Pez")) {
                humano.getInventario().usarObjeto("Pez", humano);
                foca.setTurnosBloqueados(foca.getTurnosBloqueados() + 2);
                mensaje.append(humano.getNombre()).append(" lanza un veloz PEZ a la Foca.\n");
                mensaje.append("La Foca se entretiene comiendo y pierde 2 turnos.");
            } else {
                mensaje.append(humano.getNombre()).append(" no tiene Peces para distraerla.\n");
                mensaje.append("¡ZAS! Recibe un aletazo implacable.\n");
                
                int casillaAgujero = -1;
                for (int i = humano.getPosicion() - 1; i >= 0 && casillaAgujero == -1; i--) {
                    if (tablero.getCasilla(i) != null && tablero.getCasilla(i).getTipo().equals("Casilla AGUJERO")) {
                        casillaAgujero = i;
                    }
                }
                
                int posAntigua = humano.getPosicion();
                if (casillaAgujero != -1) {
                    humano.setPosicion(casillaAgujero);
                    mensaje.append(humano.getNombre()).append(" sale volando al Agujero de la casilla ").append(casillaAgujero).append(".");
                } else {
                    humano.setPosicion(0);
                    mensaje.append(humano.getNombre()).append(" sale volando hasta la SALIDA.");
                }
                moverFichaDirecta(humano, posAntigua, humano.getPosicion());
            }
        } else {
            int bolasAtacante = atacante.getInventario().getCantidad("BolaNieve");
            int bolasDefensor = defensor.getInventario().getCantidad("BolaNieve");
            
            mensaje.append(atacante.getNombre()).append(" (").append(bolasAtacante).append(" bolas) VS ")
                   .append(defensor.getNombre()).append(" (").append(bolasDefensor).append(" bolas)\n\n");
            
            if (bolasAtacante > bolasDefensor) {
                int diff = bolasAtacante - bolasDefensor;
                int posAntigua = defensor.getPosicion();
                defensor.setPosicion(Math.max(0, posAntigua - diff));
                mensaje.append(atacante.getNombre()).append(" gana.\n");
                mensaje.append(defensor.getNombre()).append(" retrocede ").append(posAntigua - defensor.getPosicion()).append(" casillas.");
                moverFichaDirecta(defensor, posAntigua, defensor.getPosicion());
            } else if (bolasDefensor > bolasAtacante) {
                int diff = bolasDefensor - bolasAtacante;
                int posAntigua = atacante.getPosicion();
                atacante.setPosicion(Math.max(0, posAntigua - diff));
                mensaje.append(defensor.getNombre()).append(" se defiende y gana.\n");
                mensaje.append(atacante.getNombre()).append(" retrocede ").append(posAntigua - atacante.getPosicion()).append(" casillas.");
                moverFichaDirecta(atacante, posAntigua, atacante.getPosicion());
            } else {
                mensaje.append("¡Empate técnico! Nadie tiene más bolas. Se quedan donde están.");
            }
            
            while (atacante.getInventario().getCantidad("BolaNieve") > 0) atacante.getInventario().eliminarObjeto("BolaNieve");
            while (defensor.getInventario().getCantidad("BolaNieve") > 0) defensor.getInventario().eliminarObjeto("BolaNieve");
        }
        
        
        log(mensaje.toString().replace("\n", "  |  "));
        
        mostrarEventoDialogo(titulo, mensaje.toString(), this::finalizarTurno, atacante, defensor);
    }
    
    private void moverFichaDirecta(Jugador j, int desde, int hasta) {
        Casilla cOrig = tablero.getCasilla(desde);
        String tipo = (cOrig != null) ? cOrig.getTipo().toUpperCase() : "";
        Node token = playerTokens.get(j);
        
        if (tipo.contains("AGUJERO")) {
            FadeTransition fo = new FadeTransition(Duration.millis(500), token);
            fo.setToValue(0);
            fo.setOnFinished(e -> {
                casillaNodes.get(desde).getChildren().remove(token);
                posicionarToken(j);
                token.setOpacity(0);
                FadeTransition fi = new FadeTransition(Duration.millis(500), token);
                fi.setToValue(1);
                fi.play();
            });
            fo.play();
        } else if (tipo.contains("TRINEO")) {
            double dx = casillaNodes.get(hasta).getLayoutX() - casillaNodes.get(desde).getLayoutX();
            double dy = casillaNodes.get(hasta).getLayoutY() - casillaNodes.get(desde).getLayoutY();
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(800), token);
            tt.setByX(dx);
            tt.setByY(dy);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            tt.setOnFinished(e -> {
                casillaNodes.get(desde).getChildren().remove(token);
                token.setTranslateX(0); 
                token.setTranslateY(0);
                posicionarToken(j);
            });
            tt.play();
        } else {
            animarSalto(j, desde, hasta, 1000);
        }
    }

    private void animarSalto(Jugador j, int desde, int hasta, int duracionMs) {
        Node token = playerTokens.get(j);
        double dx = casillaNodes.get(hasta).getLayoutX() - casillaNodes.get(desde).getLayoutX();
        double dy = casillaNodes.get(hasta).getLayoutY() - casillaNodes.get(desde).getLayoutY();

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
            casillaNodes.get(desde).getChildren().remove(token);
            token.setTranslateX(0);
            token.setTranslateY(0);
            posicionarToken(j);
        });
        pt.play();
    }

    private void finalizarTurno() {
        animacionEnCurso = false;
        
        juegoSimulado.setTurnoActual(turnoActual);
        juegoSimulado.comprobarGanador();
        
        // Se ha eliminado el auto-guardado por turno para optimizar rendimiento
        // y por petición del usuario.
        
        if (jugadores.get(turnoActual).getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            log("¡" + jugadores.get(turnoActual).getNombre() + " HA GANADO LA PARTIDA!");
            mostrarAlertaVictoria(jugadores.get(turnoActual).getNombre());
        } else {
            turnoActual = (turnoActual + 1) % jugadores.size();
            actualizarUI();
            setControlesBloqueados(false);
            
            if (cameraAutoMode) {
                smoothCenterOnPlayer(jugadores.get(turnoActual), 1.0);
            }
            
            comprobarBloqueoInicioTurno();
        }
    }

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

    private void actualizarUI() {
        Jugador jActual = jugadores.get(turnoActual);
        
        mostrarOverlayCPU(jActual instanceof Foca);

        turnCircles.forEach((j, circle) -> {
            if (j == jActual) {
                circle.setStroke(Color.YELLOW);
                circle.setStrokeWidth(4);
                
                DropShadow glow = new DropShadow();
                glow.setRadius(20);
                glow.setColor(Color.YELLOW);
                circle.setEffect(glow);
            } else {
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(2);
                
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

    private void aplicarEstiloPorJugador(Jugador j) {
        Color c = getColorFromString(j.getColor());
        
        Color border = c;
        Color fill = c.deriveColor(0, 1.0, 0.6, 1.0);
        Color hover = c.deriveColor(0, 1.0, 0.8, 1.0);
        
        String hexBorder = String.format("#%02X%02X%02X",
            (int)(border.getRed() * 255), (int)(border.getGreen() * 255), (int)(border.getBlue() * 255));
            
        String hexFill = String.format("#%02X%02X%02X",
            (int)(fill.getRed() * 255), (int)(fill.getGreen() * 255), (int)(fill.getBlue() * 255));
            
        String hexHover = String.format("#%02X%02X%02X",
            (int)(hover.getRed() * 255), (int)(hover.getGreen() * 255), (int)(hover.getBlue() * 255));

        String shadowHex = String.format("rgba(%d, %d, %d, 0.5)",
            (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));
        
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

    private VBox crearTarjetaEstadoSecundaria(Jugador j) {
        VBox card = new VBox(2);
        card.getStyleClass().add("secondary-player-card");
        
        Color c = getColorFromString(j.getColor());
        String hex = String.format("#%02X%02X%02X",
            (int)(c.getRed() * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue() * 255));
            
        card.setStyle("-fx-border-color: " + hex + ";");
        
        Label nameLabel = new Label(j.getNombre());
        nameLabel.getStyleClass().add("secondary-player-name");
        
        Label infoLabel = new Label("Pos: " + j.getPosicion() + " | Obj: " + j.getInventario().getCantidad("Total"));
        infoLabel.getStyleClass().add("secondary-player-info");
        
        card.getChildren().addAll(nameLabel, infoLabel);
        return card;
    }

    private void actualizarInventarioFlotante(Jugador j) {
        inventoryFlowPane.getChildren().clear();
        Inventario inv = j.getInventario();
        
        agregarIconoObjeto(inventoryFlowPane, "Pez", inv.getCantidad("Pez"), "PEZ", "inventory-item-pez");
        agregarIconoObjeto(inventoryFlowPane, "BolaNieve", inv.getCantidad("BolaNieve"), "BOLA NIEVE", "inventory-item-bola");
        agregarIconoObjeto(inventoryFlowPane, "DadoRapido", inv.getCantidad("DadoRapido"), "DADO RÁPIDO", "inventory-item-rapido");
        agregarIconoObjeto(inventoryFlowPane, "DadoLento", inv.getCantidad("DadoLento"), "DADO LENTO", "inventory-item-lento");

        if (inv.getCantidad("Total") == 0) {
            inventoryFlowPane.getChildren().add(new Label("La mochila está vacía..."));
        }
    }

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

    private void prepararUsoObjeto(String tipo) {
        if (!animacionEnCurso) {
            this.selectedItemType = tipo;
            util.SoundManager.playConfirm();
            
            String nombreMsg = tipo;
            boolean esDado = tipo.startsWith("Dado");
            
            switch(tipo) {
                case "Pez": nombreMsg = "un PEZ"; break;
                case "BolaNieve": nombreMsg = "una BOLA DE NIEVE"; break;
                case "DadoRapido": nombreMsg = "un DADO RÁPIDO"; break;
                case "DadoLento": nombreMsg = "un DADO LENTO"; break;
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

    @FXML
    private void handleItemConfirmYes(ActionEvent event) {
        if (selectedItemType != null) {
            util.SoundManager.playConfirm();
            String tipo = selectedItemType;
            animateOut(itemConfirmOverlay, () -> {
                if (isInventoryOpen) handleCloseInventory();
                
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

    @FXML
    private void handleItemConfirmNo(ActionEvent event) {
        util.SoundManager.playBack();
        animateOut(itemConfirmOverlay, null);
    }

    private void setControlesBloqueados(boolean bloqueado) {
        btnDado.setDisable(bloqueado);
        btnRecolectar.setDisable(bloqueado);
        btnInventario.setDisable(bloqueado);
        logContentBox.setDisable(bloqueado);
    }

    private void log(String msg) {
        gameLogArea.appendText("► " + msg + "\n");
    }

    private void mostrarAlertaVictoria(String nombre) {
        String mensaje = "¡Enhorabuena " + nombre + ", has llegado a la meta!";
        mostrarEventoDialogo("¡Fin de la Partida!", mensaje, () -> handleBack(null));
    }

    // --- GESTIÓN DE MENÚS, OVERLAYS Y PANELES ---

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

    @FXML
    private void handleToggleMenu(ActionEvent event) {
        util.SoundManager.playConfirm();
        syncOptionsOverlayValues();
        animateIn(menuPausa);
    }

    @FXML
    private void handleResume(ActionEvent event) {
        util.SoundManager.playConfirm();
        animateOut(menuPausa, null);
    }

    @FXML
    private void handleShowOptions(ActionEvent event) {
        util.SoundManager.playConfirm();
        animateOut(menuPausa, () -> {
            syncOptionsOverlayValues();
            animateIn(optionsOverlay);
        });
    }

    @FXML
    private void handleBackFromOptions(ActionEvent event) {
        util.SoundManager.playBack();
        animateOut(optionsOverlay, () -> animateIn(menuPausa));
    }

    @FXML
    private void handleGoToMainMenu(ActionEvent event) {
        util.SoundManager.playConfirm();
        pendingAction = GameContext.ActionConfirmType.LOGOUT;
        confirmTitle.setText("VOLVER AL MENÚ");
        confirmMessage.setText("¿Seguro que quieres volver al menú principal?\nSe guardará tu progreso automáticamente.");
        
        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    @FXML
    private void handleExitGame(ActionEvent event) {
        util.SoundManager.playConfirm();
        pendingAction = GameContext.ActionConfirmType.QUIT;
        confirmTitle.setText("SALIR DEL JUEGO");
        confirmMessage.setText("¿Seguro que quieres cerrar el juego?\nSe guardará tu progreso antes de salir.");
        
        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    @FXML
    private void handleConfirmYes(ActionEvent event) {
        util.SoundManager.playConfirm();
        
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

    @FXML
    private void handleConfirmNo(ActionEvent event) {
        util.SoundManager.playBack();
        animateOut(confirmOverlay, () -> animateIn(menuPausa));
    }

    // --- LÓGICA DE ANIMACIONES ---
    
    private void initCamera() {
        // Hemos eliminado el clipping rígido para que el tablero se pueda ver por debajo de la interfaz
        
        // Zoom suave con Scroll
        cameraViewport.addEventFilter(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            double zoomStep = (delta > 0) ? 1.1 : 0.9;
            
            double nextZoom = zoomFactor * zoomStep;
            if (nextZoom >= MIN_ZOOM && nextZoom <= MAX_ZOOM) {
                zoomFactor = nextZoom;
                zoomGroup.setScaleX(zoomFactor);
                zoomGroup.setScaleY(zoomFactor);
            }
            event.consume();
        });

        // Desplazamiento (Pan) con Arrastre
        cameraViewport.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown()) {
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
                translateAnchorX = boardPane.getTranslateX();
                translateAnchorY = boardPane.getTranslateY();
                cameraViewport.setCursor(javafx.scene.Cursor.MOVE);
            }
        });

        cameraViewport.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                cameraAutoMode = false; // El usuario toma el control manual
                if (cameraTransition != null) cameraTransition.stop();
                boardPane.setTranslateX(translateAnchorX + (event.getSceneX() - mouseAnchorX));
                boardPane.setTranslateY(translateAnchorY + (event.getSceneY() - mouseAnchorY));
            }
        });

        cameraViewport.setOnMouseReleased(event -> {
            cameraViewport.setCursor(javafx.scene.Cursor.DEFAULT);
        });
    }

    @FXML
    private void handleResetCamera() {
        util.SoundManager.playConfirm();
        cameraAutoMode = true; // Reactivar modo automático
        smoothCenterOnPlayer(jugadores.get(turnoActual), 1.0);
    }

    /**
     * Centra la cámara suavemente sobre un jugador específico.
     */
    private void smoothCenterOnPlayer(Jugador j, double durationSeconds) {
        if (boardPane != null && cameraViewport != null && j != null) {
            StackPane cell = casillaNodes.get(j.getPosicion());
            if (cell != null) {
                double viewWidth = cameraViewport.getWidth();
                double viewHeight = cameraViewport.getHeight();
                if (viewWidth <= 0) viewWidth = 1280;
                if (viewHeight <= 0) viewHeight = 720;

                // Calculamos el centro visual (restando los 300px del panel derecho)
                double visualCenterX = (viewWidth - 300) / 2.0;
                double visualCenterY = viewHeight / 2.0;

                // Centro de la casilla en coordenadas del boardPane
                double targetX = cell.getLayoutX() + (cell.getPrefWidth() / 2.0);
                double targetY = cell.getLayoutY() + (cell.getPrefHeight() / 2.0);

                double newTX = visualCenterX - targetX;
                double newTY = visualCenterY - targetY;

                if (cameraTransition != null) cameraTransition.stop();

                cameraTransition = new TranslateTransition(Duration.seconds(durationSeconds), boardPane);
                cameraTransition.setToX(newTX);
                cameraTransition.setToY(newTY);
                cameraTransition.setInterpolator(Interpolator.EASE_BOTH);
                cameraTransition.play();
            }
        }
    }

    private void mostrarNotificacionEvento(String mensaje, Jugador j) {
        if (eventNotificationLabel != null) {
            Color c = getColorFromString(j.getColor());
            String hex = String.format("#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
                
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
                fadeOut
            );
            seq.setOnFinished(e -> eventNotificationLabel.setVisible(false));
            seq.play();
        }
    }

    private void mostrarEventoDialogo(String titulo, String mensaje, Runnable onComplete, Jugador... involucrados) {
        eventTitleLabel.setText(titulo);
        eventMessageLabel.setText(mensaje);
        onEventContinue = onComplete;
        
        highlightingBackups.clear();
        highlightLayer.getChildren().clear();
        
        for (Jugador j : involucrados) {
            Circle token = playerTokens.get(j);
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
                token.setTranslateX(localP.getX() + token.getRadius());
                token.setTranslateY(localP.getY() + token.getRadius());
                
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

    @FXML
    private void handleCloseEvent() {
        util.SoundManager.playBack();
        
        // Restaurar fichas
        for (Map.Entry<Jugador, Pane> entry : highlightingBackups.entrySet()) {
            Jugador j = entry.getKey();
            Pane originalParent = entry.getValue();
            Circle token = playerTokens.get(j);
            
            highlightLayer.getChildren().remove(token);
            token.setEffect(null);
            originalParent.getChildren().add(token);
            posicionarToken(j);
        }
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), eventOverlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            eventOverlay.setVisible(false);
            if (onEventContinue != null) onEventContinue.run();
        });
        ft.play();
        
        animateOut(eventDialogueBox, null);
    }

    private void centrarTablero() {
        if (boardPane != null && cameraViewport != null) {
            double viewWidth = cameraViewport.getWidth();
            double viewHeight = cameraViewport.getHeight();
            
            if (viewWidth <= 0) viewWidth = 1280;
            if (viewHeight <= 0) viewHeight = 720;
            
            // Reseteamos zoom
            zoomFactor = 1.0;
            zoomGroup.setScaleX(1.0);
            zoomGroup.setScaleY(1.0);
            
            // Calculamos el centro visual (ventana completa, ya no hay panel derecho)
            double visualCenterX = viewWidth / 2.0;
            double visualCenterY = viewHeight / 2.0;
            
            // El punto de dibujo central es (1200, 1100), pero el isométrico 
            // desplaza el "centro visual" del rombo un poco hacia arriba.
            // Ajustamos para que se vea perfectamente centrado.
            boardPane.setTranslateX(visualCenterX - 1200);
            boardPane.setTranslateY(visualCenterY - 950);
        }
    }

    private void animateIn(Node content) {
        // Ocultar otros posibles overlays activos para evitar superposiciones
        if (menuPausa != null && menuPausa != content) menuPausa.setVisible(false);
        if (optionsOverlay != null && optionsOverlay != content) optionsOverlay.setVisible(false);
        if (confirmOverlay != null && confirmOverlay != content) confirmOverlay.setVisible(false);
        if (itemConfirmOverlay != null && itemConfirmOverlay != content) itemConfirmOverlay.setVisible(false);
        
        // Si mostramos un menú de sistema, ocultamos la capa de eventos
        if (content != eventDialogueBox) eventOverlay.setVisible(false);

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

    private void initOptionsOverlay() {
        resComboOverlay.getItems().addAll("1280x720", "1600x900", "1920x1080");
        syncOptionsOverlayValues();
        
        resComboOverlay.valueProperty().addListener((o, old, n) -> checkOptionsDirty());
        fsCheckOverlay.selectedProperty().addListener((o, old, n) -> checkOptionsDirty());
        musicSliderOverlay.valueProperty().addListener((o, old, n) -> checkOptionsDirty());
        sfxSliderOverlay.valueProperty().addListener((o, old, n) -> checkOptionsDirty());
    }

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

        btnApplyOverlay.setDisable(true);
        btnApplyOverlay.getStyleClass().remove("button-dirty");
    }

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

    @FXML
    private void handleBack(ActionEvent event) {
        handleToggleMenu(event); // Reutilizamos el nuevo sistema
    }

    // --- Helpers ---




    private Color getColorFromString(String colorRef) {
        if (colorRef == null) return Color.BLUE;
        switch (colorRef.toLowerCase()) {
            case "rojo": return Color.RED;
            case "azul": return Color.BLUE;
            case "verde": return Color.GREEN;
            case "amarillo": return Color.YELLOW;
            case "naranja": return Color.ORANGE;
            case "morado": return Color.PURPLE;
            default: 
                try { return Color.web(colorRef); } 
                catch (Exception e) { return Color.DARKBLUE; }
        }
    }
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
}
