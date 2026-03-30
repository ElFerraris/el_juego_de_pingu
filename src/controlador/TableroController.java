package controlador;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.*;
import datos.BBDD;

/**
 * TableroController
 * 
 * Gestiona el renderizado, movimiento animado y lógica de turnos.
 */
public class TableroController {

    @FXML private GridPane boardGrid;
    @FXML private Label turnoLabel;
    @FXML private Label dadoResultadoLabel;
    @FXML private TextArea gameLogArea;
    @FXML private VBox playersStatusContainer;
    
    @FXML private Button btnDado;
    @FXML private Button btnDadoLento;
    @FXML private Button btnDadoRapido;

    private Tablero tablero;
    private List<Jugador> jugadores;
    private int turnoActual = 0;
    private boolean animacionEnCurso = false;
    
    private Map<Integer, StackPane> casillaNodes = new HashMap<>();
    private Map<Jugador, Circle> playerTokens = new HashMap<>();
    private Map<Jugador, VBox> playerStatusCards = new HashMap<>();

    private BBDD bbdd = new BBDD();
    private Juego juegoSimulado = new Juego();

    @FXML
    public void initialize() {
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
        
        actualizarUI();
        
        // Verificamos si el primer jugador está ya bloqueado (raro pero por si acaso)
        comprobarBloqueoInicioTurno();
    }

    private void dibujarTablero() {
        boardGrid.getChildren().clear();
        casillaNodes.clear();
        for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
            Casilla c = tablero.getCasilla(i);
            if (c == null) continue;
            StackPane cellNode = crearNodoCasilla(c);
            int row = 4 - (i / 10);
            int col = i % 10;
            if ((4 - row) % 2 != 0) col = 9 - col;
            boardGrid.add(cellNode, col, row);
            casillaNodes.put(i, cellNode);
        }
    }

    private StackPane crearNodoCasilla(Casilla c) {
        StackPane pane = new StackPane();
        pane.getStyleClass().addAll("casilla-base", getStyleClassForCasilla(c.getTipo()));
        pane.setPrefSize(80, 80);
        Label numLabel = new Label(String.valueOf(c.getPosicion()));
        numLabel.getStyleClass().add("label-num-casilla");
        StackPane.setAlignment(numLabel, Pos.TOP_LEFT);
        
        String nombreCasilla = c.getTipo().replace("Casilla ", "");
        if (nombreCasilla.equals("NORMAL")) nombreCasilla = ""; // Dejar vacío si es normal
        
        Label textLabel = new Label(nombreCasilla);
        textLabel.getStyleClass().add("label-nombre-casilla");
        
        pane.getChildren().addAll(numLabel, textLabel);
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
        StackPane cellNueva = casillaNodes.get(posNueva);
        
        if (cellAntigua != null) cellAntigua.getStyleClass().remove("casilla-active");
        if (cellNueva != null) cellNueva.getStyleClass().add("casilla-active");
        
        cellAntigua.getChildren().remove(playerTokens.get(j));
        posicionarToken(j);
        actualizarTarjetaJugador(j);

        // Pequeña pausa antes del siguiente paso
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            if (pasosRestantes <= 1 || posNueva >= Tablero.TAMANYO_TABLERO - 1) {
                if (cellNueva != null) cellNueva.getStyleClass().remove("casilla-active");
            }
            moverJugadorAnimado(j, pasosRestantes - 1, onComplete);
        });
        pause.play();
    }

    private void procesarEfectosCasilla(Jugador j) {
        int posAntes = j.getPosicion();
        tablero.aplicarEfectoCasilla(j);
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
            if (p != jActual && p.getPosicion() == jActual.getPosicion()) {
                oponente = p;
                break;
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
        boolean esFoca = (atacante instanceof CPU || defensor instanceof CPU);
        String titulo = esFoca ? "¡Ataque de la Foca Loca!" : "¡Guerra de Bolas de Nieve!";
        StringBuilder mensaje = new StringBuilder();

        if (esFoca) {
            Jugador humano = (atacante instanceof CPU) ? defensor : atacante;
            CPU foca = (CPU) ((atacante instanceof CPU) ? atacante : defensor);
            
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
                for (int i = humano.getPosicion() - 1; i >= 0; i--) {
                    if (tablero.getCasilla(i) != null && tablero.getCasilla(i).getTipo().equals("Casilla AGUJERO")) {
                        casillaAgujero = i;
                        break;
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
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje.toString());
        try {
            alert.getDialogPane().getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
        } catch (Exception e) {}
        alert.showAndWait();
        
        finalizarTurno();
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
            bbdd.actualizarEstadoPartida(idPartida, juegoSimulado);
            for (Jugador j : jugadores) {
                bbdd.actualizarParticipacion(idPartida, j);
            }
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
        turnoLabel.setText("TURNO DE: " + jActual.getNombre().toUpperCase());
        
        playerStatusCards.forEach((j, card) -> {
            card.getStyleClass().remove("player-active-card");
            if (j == jActual) card.getStyleClass().add("player-active-card");
        });
    }

    private void actualizarTarjetaJugador(Jugador j) {
        VBox card = playerStatusCards.get(j);
        if (card != null) {
            ((Label)card.getChildren().get(1)).setText("Posición: " + j.getPosicion());
            ((Label)card.getChildren().get(2)).setText("Objetos: " + j.getInventario().getCantidad("Total"));
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("¡Fin de la Partida!");
        alert.setHeaderText(null);
        alert.setContentText("¡Enhorabuena " + nombre + ", has llegado a la meta!");
        alert.showAndWait();
        handleBack(null);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml");
    }

    // --- Helpers ---

    private String getStyleClassForCasilla(String tipo) {
        switch (tipo) {
            case "Casilla OSO": return "casilla-oso";
            case "Casilla AGUJERO": return "casilla-agujero";
            case "Casilla TRINEO": return "casilla-trineo";
            case "Casilla INTERROGANTE": return "casilla-interrogante";
            case "Casilla ROMPEDIZAS": return "casilla-rompediza";
            default: return "casilla-normal";
        }
    }


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
