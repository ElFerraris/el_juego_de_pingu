package controlador;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML private HBox turnIndicatorBox;
    @FXML private Label dadoResultadoLabel;
    @FXML private TextArea gameLogArea;
    @FXML private VBox playersStatusContainer;
    
    // Panel lateral de Log
    @FXML private HBox logPanelContainer;
    @FXML private VBox logContentBox;
    @FXML private Button btnToggleLog;
    private boolean isLogOpen = false;

    @FXML private Button btnDado;
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
    
    // Controles de Confirmación
    @FXML private Label confirmTitle;
    @FXML private Label confirmMessage;

    // Valores iniciales para Opciones (Dirty State)
    private String initialResolution;
    private boolean initialFullscreen;
    private double initialMusic;
    private double initialSfx;
    
    // Estado de confirmación actual
    private GameContext.ActionConfirmType pendingAction;

    private Tablero tablero;
    private List<Jugador> jugadores;
    private int turnoActual = 0;
    private boolean animacionEnCurso = false;
    
    private Map<Integer, StackPane> casillaNodes = new HashMap<>();
    private Map<Jugador, Circle> playerTokens = new HashMap<>(); // Fichas en el tablero
    private Map<Jugador, Circle> turnCircles = new HashMap<>();  // Círculos del indicador superior
    private Map<Jugador, VBox> playerStatusCards = new HashMap<>();

    private BBDD bbdd = new BBDD();
    private Juego juegoSimulado = new Juego();

    @FXML
    public void initialize() {
        // Inicialización de Opciones Overlay
        initOptionsOverlay();
        
        // Esconder panel de log al inicio directamente (320 es el prefWidth de logContentBox)
        logPanelContainer.setTranslateX(-320);
        
        this.tablero = new Tablero();
        String seed = GameContext.getInstance().getSeed();
        tablero.introducirSeed(seed);
        
        this.jugadores = GameContext.getInstance().getConfiguredPlayers();
        
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
            
            // Limpiamos la bandera para futuras partidas en la misma sesión
            GameContext.getInstance().setIdPartidaCargar(-1);
        }
        
        dibujarTablero();
        crearTarjetasJugadores();
        crearFichasJugadores();
        crearIndicadorTurnos();
        
        actualizarUI();
        
        // OCULTAR GIF DE CARGA (Andrei Style)
        NavigationController.hideLoading();
        
        // Verificamos si el primer jugador está ya bloqueado (raro pero por si acaso)
        comprobarBloqueoInicioTurno();
    }

    private void dibujarTablero() {
        boardPane.getChildren().clear();
        casillaNodes.clear();
        
        List<StackPane> sortedNodes = new java.util.ArrayList<>();
        
        double centerX = 470.0; // Centro horizontal ajustado
        double bottomY = 460.0; // Subimos el tablero para evitar cortes por debajo
        
        // Espacios reducidos proporcionalmente al nuevo tamaño (50px) para que quepa en la pantalla
        double xOffset = 56.0; 
        double yOffset = 31.0; 
        
        for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
            Casilla c = tablero.getCasilla(i);
            if (c == null) continue;
            
            StackPane cellNode = crearNodoCasilla(c);
            
            double layoutX, layoutY;
            int zOrder;
            
            if (i < 49) {
                // Patrón Boustrophedon 7x7 (Isométrico)
                int gy = i / 7;
                int gx;
                if (gy % 2 == 0) {
                    gx = i % 7; // Izquierda a derecha
                } else {
                    gx = 6 - (i % 7); // Derecha a izquierda
                }
                
                zOrder = gx + gy;
                
                // Calcular posiciones absolutas en isométrico adaptado al tamaño de 50px
                layoutX = centerX + (gx - gy) * xOffset - 25;
                layoutY = bottomY - (gx + gy) * yOffset - 25;
            } else {
                // Casilla 49 o 50 (La Meta / Igloo)
                zOrder = 7 + 7;
                layoutX = centerX + (7 - 7) * xOffset - 25;
                // Ajustada para que no pase de la parte superior del todo
                layoutY = bottomY - (7 + 7) * yOffset - 25 - 12;
            }
            
            cellNode.setUserData(-zOrder); // Guardar Z-Order negativo para fácil ordenación
            
            cellNode.setLayoutX(layoutX);
            cellNode.setLayoutY(layoutY);
            
            casillaNodes.put(i, cellNode);
            sortedNodes.add(cellNode);
        }
        
        // Ordenar nodos. El menor zOrder negativo significa mayor Z (más atrás), 
        // así que los que están más atrás tienen un valor numérico menor aquí y van primero
        sortedNodes.sort((a, b) -> Integer.compare((int) a.getUserData(), (int) b.getUserData()));
        
        boardPane.getChildren().addAll(sortedNodes);
    }

    private StackPane crearNodoCasilla(Casilla c) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("casilla-base");
        // Hemos reducido el bloque de 64 a 50 para que el rombo quepa entero en pantalla
        pane.setPrefSize(50, 50);
        
        // Cargar Sprite (Fondo de la casilla)
        try {
            Image img = new Image(getClass().getResourceAsStream(c.getSpritePath()));
            ImageView view = new ImageView(img);
            view.setFitWidth(50);
            view.setFitHeight(50);
            view.setPreserveRatio(true);
            pane.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("Error cargando sprite para " + c.getTipo() + ": " + e.getMessage());
            pane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc;"); // Fallback
        }
        
        // Número de casilla
        Label numLabel = new Label(String.valueOf(c.getPosicion()));
        numLabel.getStyleClass().add("label-num-casilla");
        StackPane.setAlignment(numLabel, Pos.TOP_LEFT);
        
        pane.getChildren().add(numLabel);
        return pane;
    }

    private void crearTarjetasJugadores() {
        playersStatusContainer.getChildren().clear();
        playerStatusCards.clear();
        for (Jugador j : jugadores) {
            VBox card = new VBox(5);
            card.getStyleClass().add("player-status-card");
            
            Color colorBorde = getColorFromString(j.getColor());
            String hexColor = String.format("#%02X%02X%02X",
                (int)(colorBorde.getRed() * 255),
                (int)(colorBorde.getGreen() * 255),
                (int)(colorBorde.getBlue() * 255));
                
            card.setStyle("-fx-border-color: " + hexColor + "; -fx-border-width: 0 0 0 5;");
            Label nameLabel = new Label(j.getNombre());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
            Label posLabel = new Label("Posición: " + j.getPosicion());
            Label invLabel = new Label("Objetos: " + j.getInventario().getCantidad("Total"));
            card.getChildren().addAll(nameLabel, posLabel, invLabel);
            playersStatusContainer.getChildren().add(card);
            playerStatusCards.put(j, card);
        }
    }

    private void crearFichasJugadores() {
        for (Jugador j : jugadores) {
            Circle token = new Circle(12);
            token.setFill(getColorFromString(j.getColor()));
            token.setStroke(Color.WHITE);
            token.getStyleClass().add("player-token");
            playerTokens.put(j, token);
            posicionarToken(j);
        }
    }

    private void posicionarToken(Jugador j) {
        StackPane cell = casillaNodes.get(j.getPosicion());
        if (cell != null) {
            Circle token = playerTokens.get(j);
            int index = jugadores.indexOf(j);
            token.setTranslateX(index * 6 - 8); 
            token.setTranslateY(index * 4 - 4);
            if (!cell.getChildren().contains(token)) {
                cell.getChildren().add(token);
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
        ejecutarTurno((int)(Math.random() * 6) + 1);
    }

    @FXML
    private void handleUseDadoLento(ActionEvent event) {
        Jugador jActual = jugadores.get(turnoActual);
        if (jActual.getInventario().tieneObjeto("DadoLento")) {
            jActual.getInventario().usarDadoEspecifico("Lento", jActual);
            ejecutarTurno((int)(Math.random() * 3) + 1);
        } else {
            log(jActual.getNombre() + " no tiene Dados Lentos.");
        }
    }

    @FXML
    private void handleUseDadoRapido(ActionEvent event) {
        Jugador jActual = jugadores.get(turnoActual);
        if (jActual.getInventario().tieneObjeto("DadoRapido")) {
            jActual.getInventario().usarDadoEspecifico("Rapido", jActual);
            ejecutarTurno((int)(Math.random() * 6) + 3);
        } else {
            log(jActual.getNombre() + " no tiene Dados Rápidos.");
        }
    }

    private void ejecutarTurno(int pasos) {
        if (animacionEnCurso) return;
        
        Jugador jActual = jugadores.get(turnoActual);
        dadoResultadoLabel.setText("Último dado: " + pasos);
        log(jActual.getNombre() + " saca un " + pasos);
        
        setControlesBloqueados(true);
        moverJugadorAnimado(jActual, pasos, () -> {
            // Al terminar el movimiento, aplicamos efectos
            procesarEfectosCasilla(jActual);
        });
    }

    /**
     * Mueve al jugador de 1 en 1 con animación recursiva.
     */
    private void moverJugadorAnimado(Jugador j, int pasosRestantes, Runnable onComplete) {
        if (pasosRestantes <= 0 || j.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            onComplete.run();
            return;
        }

        animacionEnCurso = true;
        int posAntigua = j.getPosicion();
        int posNueva = posAntigua + 1;
        
        j.setPosicion(posNueva);
        
        // Actualización Visual
        StackPane cellAntigua = casillaNodes.get(posAntigua);
        
        cellAntigua.getChildren().remove(playerTokens.get(j));
        posicionarToken(j);
        actualizarTarjetaJugador(j);

        // Pequeña pausa antes del siguiente paso
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            moverJugadorAnimado(j, pasosRestantes - 1, onComplete);
        });
        pause.play();
    }

    private void procesarEfectosCasilla(Jugador j) {
        int posAntes = j.getPosicion();
        
        String logEfecto = tablero.aplicarEfectoCasilla(j);
        if (logEfecto != null && !logEfecto.isEmpty()) {
            log(logEfecto.replace("\n", " "));
        }
        
        int posDespues = j.getPosicion();

        if (posAntes != posDespues) {
            log("¡Efecto! " + j.getNombre() + " se mueve a la casilla " + posDespues);
            // Animación secundaria rápida para el efecto (trineo/agujero)
            moverFichaDirecta(j, posAntes, posDespues);
            
            // Esperamos un poco después del efecto antes de comprobar batalla
            PauseTransition wait = new PauseTransition(Duration.seconds(1));
            wait.setOnFinished(e -> comprobarBatallaYFinalizarTurno(j));
            wait.play();
        } else {
            comprobarBatallaYFinalizarTurno(j);
        }
    }

    private void comprobarBatallaYFinalizarTurno(Jugador jActual) {
        if (jActual.getPosicion() <= 0 || jActual.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            finalizarTurno();
            return;
        }

        Jugador oponente = null;
        for (Jugador p : jugadores) {
            if (oponente == null && p != jActual && p.getPosicion() == jActual.getPosicion()) {
                oponente = p;
            }
        }

        if (oponente != null) {
            log("¡COLISIÓN en casilla " + jActual.getPosicion() + "!");
            resolverCombate(jActual, oponente);
        } else {
            finalizarTurno();
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
            
            // Vaciar inventarios
            while (atacante.getInventario().getCantidad("BolaNieve") > 0) atacante.getInventario().eliminarObjeto("BolaNieve");
            while (defensor.getInventario().getCantidad("BolaNieve") > 0) defensor.getInventario().eliminarObjeto("BolaNieve");
        }
        
        actualizarTarjetaJugador(atacante);
        actualizarTarjetaJugador(defensor);
        
        log(mensaje.toString().replace("\n", "  |  "));
        
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje.toString());
            try {
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
            } catch (Exception e) {}
            alert.showAndWait();
            
            finalizarTurno();
        });
    }
    
    private void moverFichaDirecta(Jugador j, int desde, int hasta) {
        casillaNodes.get(desde).getChildren().remove(playerTokens.get(j));
        posicionarToken(j);
        actualizarTarjetaJugador(j);
    }

    private void finalizarTurno() {
        animacionEnCurso = false;
        
        juegoSimulado.setTurnoActual(turnoActual);
        juegoSimulado.comprobarGanador();
        
        // Guardar estado en BBDD
        int idPartida = tablero.getIdPartida();
        if (idPartida > 0) {
            bbdd.guardarEstadoCompleto(idPartida, juegoSimulado);
        }
        
        // Comprobar Victoria
        if (jugadores.get(turnoActual).getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            log("¡" + jugadores.get(turnoActual).getNombre() + " HA GANADO LA PARTIDA!");
            mostrarAlertaVictoria(jugadores.get(turnoActual).getNombre());
            return;
        }

        turnoActual = (turnoActual + 1) % jugadores.size();
        actualizarUI();
        setControlesBloqueados(false);
        
        comprobarBloqueoInicioTurno();
    }

    private void comprobarBloqueoInicioTurno() {
        Jugador j = jugadores.get(turnoActual);
        if (j.estaBloqueado()) {
            log(j.getNombre() + " está bloqueado. Saltando turno...");
            j.setTurnosBloqueados(j.getTurnosBloqueados() - 1);
            
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> finalizarTurno());
            pause.play();
        }
    }

    private void actualizarUI() {
        Jugador jActual = jugadores.get(turnoActual);
        
        turnCircles.forEach((j, circle) -> {
            if (j == jActual) {
                // Iluminar para el turno actual
                circle.setStroke(Color.YELLOW);
                circle.setStrokeWidth(4);
                
                DropShadow glow = new DropShadow();
                glow.setRadius(20);
                glow.setColor(Color.YELLOW);
                circle.setEffect(glow);
            } else {
                // Estado normal
                circle.setStroke(Color.WHITE);
                circle.setStrokeWidth(2);
                
                DropShadow ds = new DropShadow();
                ds.setRadius(5);
                ds.setColor(Color.BLACK);
                circle.setEffect(ds);
            }
        });
        
        playersStatusContainer.getChildren().clear();
        VBox activeCard = playerStatusCards.get(jActual);
        if (activeCard != null) {
            // Asegurarse de que esté actualizada para este turno
            actualizarTarjetaJugador(jActual);
            playersStatusContainer.getChildren().add(activeCard);
        }
    }

    private void actualizarTarjetaJugador(Jugador j) {
        VBox card = playerStatusCards.get(j);
        if (card != null) {
            ((Label)card.getChildren().get(1)).setText("Posición: " + j.getPosicion());
            
            Inventario inv = j.getInventario();
            StringBuilder sb = new StringBuilder("Objetos:\n");
            int peces = inv.getCantidad("Pez");
            int bolas = inv.getCantidad("BolaNieve");
            int rapidos = inv.getCantidad("DadoRapido");
            int lentos = inv.getCantidad("DadoLento");
            
            if (peces == 0 && bolas == 0 && rapidos == 0 && lentos == 0) {
                sb.append("- Nada (Vacío)");
            } else {
                if (peces > 0) sb.append("- ").append(peces).append("x Pez\n");
                if (bolas > 0) sb.append("- ").append(bolas).append("x Bola de Nieve\n");
                if (rapidos > 0) sb.append("- ").append(rapidos).append("x Dado Rápido\n");
                if (lentos > 0) sb.append("- ").append(lentos).append("x Dado Lento\n");
            }
            
            ((Label)card.getChildren().get(2)).setText(sb.toString().trim());
        }
    }

    private void setControlesBloqueados(boolean status) {
        btnDado.setDisable(status);
        btnDadoLento.setDisable(status);
        btnDadoRapido.setDisable(status);
    }

    private void log(String msg) {
        gameLogArea.appendText("► " + msg + "\n");
    }

    private void mostrarAlertaVictoria(String nombre) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("¡Fin de la Partida!");
            alert.setHeaderText(null);
            alert.setContentText("¡Enhorabuena " + nombre + ", has llegado a la meta!");
            
            // Estilo para la alerta
            try {
                alert.getDialogPane().getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
                alert.getDialogPane().getStyleClass().add("glass-panel");
            } catch (Exception e) {}
            
            alert.showAndWait();
            handleBack(null);
        });
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
        syncOptionsOverlayValues(); // Precarga opciones por si acaso
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
        confirmMessage.setText("¿Seguro que quieres volver al menú principal?");
        
        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    @FXML
    private void handleExitGame(ActionEvent event) {
        util.SoundManager.playConfirm();
        pendingAction = GameContext.ActionConfirmType.QUIT;
        confirmTitle.setText("SALIR DEL JUEGO");
        confirmMessage.setText("¿Seguro que quieres cerrar el juego?");
        
        animateOut(menuPausa, () -> animateIn(confirmOverlay));
    }

    @FXML
    private void handleConfirmYes(ActionEvent event) {
        util.SoundManager.playConfirm();
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

    private void animateIn(Node content) {
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
        SoundManager.setVolume(sm.getSfxVolume());
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
}
