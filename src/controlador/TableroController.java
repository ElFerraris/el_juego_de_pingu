package controlador;

import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.*;

/**
 * TableroController
 * 
 * Orquestador principal de la vista del juego.
 * Maneja el renderizado del tablero, el movimiento de los jugadores y la lógica de turnos.
 */
public class TableroController {

    @FXML private GridPane boardGrid;
    @FXML private Label turnLabel;
    @FXML private Label statusLabel;
    @FXML private ListView<String> gameLog;
    
    // Paneles de jugadores
    @FXML private VBox player1Card;
    @FXML private VBox player2Card;
    // ... más si hay más jugadores

    private Tablero tablero;
    private List<Jugador> jugadores;
    private int turnoActual = 0;
    
    // Mapeo para encontrar rápidamente el nodo visual de una casilla por su índice
    private Map<Integer, StackPane> casillaNodes = new HashMap<>();
    private Map<Jugador, Circle> playerTokens = new HashMap<>();

    @FXML
    public void initialize() {
        // Inicializamos los datos desde el contexto
        this.tablero = new Tablero();
        String seed = GameContext.getInstance().getSeed();
        
        System.out.println("► Generando tablero con seed: " + seed);
        tablero.introducirSeed(seed);
        
        this.jugadores = GameContext.getInstance().getConfiguredPlayers();
        
        dibujarTablero();
        crearFichasJugadores();
        actualizarUI();
        
        log("¡La partida ha comenzado!");
    }

    private void dibujarTablero() {
        boardGrid.getChildren().clear();
        casillaNodes.clear();

        for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
            Casilla c = tablero.getCasilla(i);
            if (c == null) {
                System.err.println("Error: Casilla " + i + " es nula. La semilla probablemente falló.");
                continue;
            }
            StackPane cellNode = crearNodoCasilla(c);
            
            // Lógica de Serpiente (10 columnas x 5 filas)
            int row = 4 - (i / 10); // Empezamos desde abajo
            int col = i % 10;
            
            // Si la fila es impar (contando desde abajo), invertimos el orden de las columnas
            if ((4 - row) % 2 != 0) {
                col = 9 - col;
            }

            boardGrid.add(cellNode, col, row);
            casillaNodes.put(i, cellNode);
        }
    }

    private StackPane crearNodoCasilla(Casilla c) {
        StackPane pane = new StackPane();
        pane.getStyleClass().addAll("casilla-base", getStyleClassForCasilla(c.getTipo()));
        
        Label numLabel = new Label(String.valueOf(c.getPosicion()));
        numLabel.getStyleClass().add("label-num-casilla");
        StackPane.setAlignment(numLabel, Pos.TOP_LEFT);
        
        Label iconLabel = new Label(getEmojiForCasilla(c.getTipo()));
        iconLabel.setStyle("-fx-font-size: 30px;");

        pane.getChildren().addAll(numLabel, iconLabel);
        return pane;
    }

    private void crearFichasJugadores() {
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            Circle token = new Circle(15);
            token.setFill(getColorFromString(j.getColor()));
            token.getStyleClass().add("player-token");
            
            playerTokens.put(j, token);
            posicionarToken(j);
        }
    }

    private void posicionarToken(Jugador j) {
        StackPane cell = casillaNodes.get(j.getPosicion());
        if (cell != null) {
            Circle token = playerTokens.get(j);
            if (!cell.getChildren().contains(token)) {
                cell.getChildren().add(token);
            }
        }
    }

    @FXML
    private void handleRollSlow(ActionEvent event) {
        realizarTurno(1 + (int)(Math.random() * 3)); // Dado lento: 1-3
    }

    @FXML
    private void handleRollFast(ActionEvent event) {
        realizarTurno(1 + (int)(Math.random() * 6)); // Dado rápido: 1-6
    }

    private void realizarTurno(int pasos) {
        Jugador jActual = jugadores.get(turnoActual);
        log(jActual.getNombre() + " saca un " + pasos);
        
        moverJugador(jActual, pasos);
    }

    private void moverJugador(Jugador j, int pasos) {
        // En una implementación real, aquí haríamos una animación paso a paso
        int antiguaPos = j.getPosicion();
        int nuevaPos = antiguaPos + pasos;
        
        if (nuevaPos >= Tablero.TAMANYO_TABLERO) {
            nuevaPos = Tablero.TAMANYO_TABLERO - 1;
        }
        
        // Animación simple (simulada con PauseTransition)
        j.setPosicion(nuevaPos);
        
        // Limpiamos de la casilla antigua
        StackPane oldCell = casillaNodes.get(antiguaPos);
        oldCell.getChildren().remove(playerTokens.get(j));
        
        posicionarToken(j);
        
        // Aplicamos efecto de casilla
        tablero.aplicarEfectoCasilla(j);
        
        // Comprobar si ha ganado o terminado el turno
        finalizarTurno();
    }

    private void finalizarTurno() {
        turnoActual = (turnoActual + 1) % jugadores.size();
        actualizarUI();
    }

    private void actualizarUI() {
        Jugador jActual = jugadores.get(turnoActual);
        turnLabel.setText("Turno de: " + jActual.getNombre());
        
        // Marcamos la card del jugador actual (CSS)
        // [Lógica para cambiar estilos de player1Card, player2Card...]
    }

    private void log(String msg) {
        gameLog.getItems().add(0, "► " + msg);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml");
    }

    // --- Helpers Visuales ---

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

    private String getEmojiForCasilla(String tipo) {
        switch (tipo) {
            case "Casilla OSO": return "🐻";
            case "Casilla AGUJERO": return "🕳️";
            case "Casilla TRINEO": return "🛷";
            case "Casilla INTERROGANTE": return "❓";
            case "Casilla ROMPEDIZAS": return "🧊";
            default: return "";
        }
    }

    private Color getColorFromString(String colorRef) {
        try {
            return Color.web(colorRef);
        } catch (Exception e) {
            return Color.BLUE;
        }
    }
}
