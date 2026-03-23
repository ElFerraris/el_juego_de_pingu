package vista;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import controlador.Juego;
import modelo.*;

public class PantallaJuego {

	// Menu items
	@FXML
	private MenuItem newGame;
	@FXML
	private MenuItem saveGame;
	@FXML
	private MenuItem loadGame;
	@FXML
	private MenuItem quitGame;

	// Buttons
	@FXML
	private Button dado;
	@FXML
	private Button rapido;
	@FXML
	private Button lento;
	@FXML
	private Button peces;
	@FXML
	private Button nieve;

	// Texts
	@FXML
	private Text dadoResultText;
	@FXML
	private Text rapido_t;
	@FXML
	private Text lento_t;
	@FXML
	private Text peces_t;
	@FXML
	private Text nieve_t;
	@FXML
	private Text eventos;

	// Game board and player pieces
	@FXML
	private GridPane tablero;
	@FXML
	private Circle P1;
	@FXML
	private Circle P2;
	@FXML
	private Circle P3;
	@FXML
	private Circle P4;
	@FXML
	private Circle P5;

    @FXML
    private VBox gameOverOverlay;
    @FXML
    private Text winnerLabel;
    @FXML
    private VBox optionsOverlay;
    @FXML
    private VBox confirmOverlay;
    @FXML
    private VBox legendContainer;
    @FXML
    private Label fullScreenHint;

	private Juego juego;
	private static final int COLUMNS = 5;

	private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
	private final Random rand = new Random();

	@FXML
	private void initialize() {
		eventos.setText("¡El juego ha comenzado!");
        showFullScreenHint();
	}

    private void showFullScreenHint() {
        if (fullScreenHint == null) return;
        fullScreenHint.setVisible(true);
        fullScreenHint.setOpacity(1.0);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(2), fullScreenHint);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(javafx.util.Duration.seconds(1));
        fade.setOnFinished(e -> fullScreenHint.setVisible(false));
        fade.play();
    }

    public void setJuego(Juego juego) {
        this.juego = juego;
    }

    public void inicializarActual() {
        if (juego != null) {
            // Apply player-chosen colors to their board circle pieces
            for (int i = 0; i < juego.getJugadores().size(); i++) {
                Jugador j = juego.getJugadores().get(i);
                Circle pieza = getPiezaPorIndice(i);
                if (pieza != null) {
                    pieza.setFill(colorDesdeNombre(j.getColor()));
                }
            }
            mostrarTiposDeCasillasEnTablero(juego.getTablero());
            actualizarEstadoUI();
        }
    }

    private void actualizarEstadoUI() {
        if (juego == null) return;

        // Mover piezas a sus posiciones reales según el modelo
        for (int i = 0; i < juego.getJugadores().size(); i++) {
            Jugador j = juego.getJugadores().get(i);
            Circle pieza = getPiezaPorIndice(i);
            if (pieza != null) {
                int pos = j.getPosicion();
                int row = pos / COLUMNS;
                int col = pos % COLUMNS;
                
                GridPane.setRowIndex(pieza, row);
                GridPane.setColumnIndex(pieza, col);
                pieza.setVisible(true);
            }
        }
        
        // Disable dice if not human turn or game over
        Jugador actual = juego.getJugadores().get(juego.getTurnoActual());
        boolean isGameOver = juego.isPartidaFinalizada();
        
        dado.setDisable(actual instanceof CPU || isGameOver || optionsOverlay.isVisible() || confirmOverlay.isVisible());
        eventos.setText("Turno de: " + actual.getNombre());

        if (isGameOver) {
            comprobarFinDePartida();
        }

        // --- Actualización de Leyenda ---
        legendContainer.getChildren().clear();
        for (int i = 0; i < juego.getJugadores().size(); i++) {
            Jugador j = juego.getJugadores().get(i);
            HBox item = new HBox(10);
            Circle colorDot = new Circle(6, colorDesdeNombre(j.getColor()));
            Label text = new Label(j.getNombre() + (j instanceof CPU ? " (CPU)" : ""));
            text.setStyle("-fx-font-size: 12px;");
            item.getChildren().addAll(colorDot, text);
            legendContainer.getChildren().add(item);
        }

        // Actualizar inventario del jugador actual (si es humano)
        Jugador humano = (actual instanceof CPU) ? juego.getJugadores().get(0) : actual;
        peces_t.setText("x" + humano.getInventario().getCantidad("Pez"));
        nieve_t.setText("x" + humano.getInventario().getCantidad("BolaNieve"));
        rapido_t.setText("x" + humano.getInventario().getCantidad("DadoRapido"));
        lento_t.setText("x" + humano.getInventario().getCantidad("DadoLento"));
    }

    private Circle getPiezaPorIndice(int index) {
        switch (index) {
            case 0: return P1;
            case 1: return P2;
            case 2: return P3;
            case 3: return P4;
            case 4: return P5;
            default: return null;
        }
    }

	private void mostrarTiposDeCasillasEnTablero(Tablero t) {
		tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

		for (int i = 0; i < Tablero.TAMANYO_TABLERO; i++) {
			Casilla casilla = t.getCasilla(i);
			if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                Text texto = new Text(tipo);
                texto.setUserData(TAG_CASILLA_TEXT);
                texto.getStyleClass().add("cell-type");

                int row = i / COLUMNS;
                int col = i % COLUMNS;

                GridPane.setRowIndex(texto, row);
                GridPane.setColumnIndex(texto, col);
                tablero.getChildren().add(texto);
			}
		}
	}

	@FXML
	private void handleDado(ActionEvent event) {
        int turnoActual = juego.getTurnoActual();
		Jugador pingu = juego.getJugadores().get(turnoActual);
        
        if (pingu instanceof CPU) return; // Should not happen if button is disabled

		int resultado = pingu.tirarDado();
		dadoResultText.setText("Ha salido: " + resultado);
		moverPieza(turnoActual, resultado, () -> {
            // Apply effects
            juego.getTablero().aplicarEfectoCasilla(pingu, juego);
            juego.comprobarGuerra(pingu);
            // Show any event message from the Casilla or war
            String logMsg = juego.getLogMessage();
            if (logMsg != null && !logMsg.isEmpty()) {
                eventos.setText(logMsg);
            }
            
            if (comprobarFinDePartida()) return;

            // Change turn
            juego.cambiarTurno();
            actualizarEstadoUI();
            
            // If next is CPU, auto-run
            checkAndRunCPUTurn();
        });
	}

    private void checkAndRunCPUTurn() {
        Jugador actual = juego.getJugadores().get(juego.getTurnoActual());
        if (actual instanceof CPU && !juego.isPartidaFinalizada()) {
            dado.setDisable(true);
            // Small delay for CPU
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                int res = actual.tirarDado();
                dadoResultText.setText(actual.getNombre() + " saca: " + res);
                moverPieza(juego.getTurnoActual(), res, () -> {
                    juego.getTablero().aplicarEfectoCasilla(actual, juego);
                    juego.comprobarGuerra(actual);
                    String logMsg = juego.getLogMessage();
                    if (logMsg != null && !logMsg.isEmpty()) {
                        eventos.setText(logMsg);
                    }
                    if (comprobarFinDePartida()) return;
                    juego.cambiarTurno();
                    actualizarEstadoUI();
                    checkAndRunCPUTurn(); // Recursive check for next CPU
                });
            });
            pause.play();
        }
    }

	private void moverPieza(int playerIndex, int steps, Runnable onFinished) {
	    Circle pieza = getPiezaPorIndice(playerIndex);
        Jugador j = juego.getJugadores().get(playerIndex);
        
        int oldPos = j.getPosicion() - steps; // Position before tirarDado
        if (oldPos < 0) oldPos = 0;
        
        int newPos = j.getPosicion();
	    if (newPos >= 50) newPos = 49;
	    if (newPos < 0) newPos = 0;

	    int oldRow = oldPos / COLUMNS;
	    int oldCol = oldPos % COLUMNS;
	    int newRow = newPos / COLUMNS;
	    int newCol = newPos % COLUMNS;

	    double cellWidth = tablero.getWidth() / COLUMNS;
	    double cellHeight = tablero.getHeight() / 10;

	    double dx = (newCol - oldCol) * cellWidth;
	    double dy = (newRow - oldRow) * cellHeight;

	    TranslateTransition slide = new TranslateTransition(Duration.millis(350), pieza);
	    slide.setByX(dx);
	    slide.setByY(dy);

	    slide.setOnFinished(e -> {
	        pieza.setTranslateX(0);
	        pieza.setTranslateY(0);
	        GridPane.setRowIndex(pieza, newRow);
	        GridPane.setColumnIndex(pieza, newCol);
            if (onFinished != null) onFinished.run();
	    });
	    slide.play();
	}

    @FXML
    private void showOptionsMenu() {
        optionsOverlay.setVisible(true);
        actualizarEstadoUI();
    }

    @FXML
    private void hideOptionsMenu() {
        optionsOverlay.setVisible(false);
        actualizarEstadoUI();
    }

    @FXML
    private void handleConfirmQuitToMenu() {
        optionsOverlay.setVisible(false);
        confirmOverlay.setVisible(true);
    }

    @FXML
    private void hideConfirmOverlay() {
        confirmOverlay.setVisible(false);
        optionsOverlay.setVisible(true);
    }

    @FXML
    private void handleDoQuitToMenu(ActionEvent event) {
        irAlMenu(event);
    }

    @FXML
    private void handleVolverMenu(ActionEvent event) {
        showOptionsMenu();
    }

    private void irAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNewGame(ActionEvent event) {
        irAlMenu(event); // Simplest for now: go back to menu to start over
    }

    @FXML
    private void handleSaveGame(ActionEvent event) {
        if (juego != null) {
            juego.guardarPartida();
            eventos.setText("¡Partida guardada correctamente!");
        }
    }

    @FXML
    private void handleLoadGame(ActionEvent event) {
        irAlMenu(event); // Go back to menu to select another game
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        javafx.application.Platform.exit();
    }

	@FXML private void handleRapido() { useItem("DadoRapido"); }
	@FXML private void handleLento() { useItem("DadoLento"); }
	@FXML private void handlePeces() { useItem("Pez"); }
	@FXML private void handleNieve() { useItem("BolaNieve"); }

    private void useItem(String itemType) {
        if (juego != null) {
            Jugador h = juego.getJugadores().get(juego.getTurnoActual());
            if (h instanceof CPU) {
                eventos.setText("¡No es tu turno aún!");
                return;
            }
            int cantidad = h.getInventario().getCantidad(itemType);
            if (cantidad > 0) {
                // Pez y BolaNieve no se consumen manualmente: se usan automáticamente
                // por CasillaOso (Pez) y Guerra (BolaNieve)
                String msg;
                switch (itemType) {
                    case "DadoRapido":
                        msg = "🎲 Tienes " + cantidad + " Dado(s) Rápido. Se usarán automáticamente al tirar.";
                        break;
                    case "DadoLento":
                        msg = "🎲 Tienes " + cantidad + " Dado(s) Lento. Se usarán automáticamente al tirar.";
                        break;
                    case "Pez":
                        msg = "🐟 Tienes " + cantidad + " Pez(ces). Te protegerán automáticamente del Oso Polar.";
                        break;
                    case "BolaNieve":
                        msg = "❄️ Tienes " + cantidad + " Bola(s) de Nieve. Se usarán automáticamente en combate.";
                        break;
                    default:
                        msg = "Tienes " + cantidad + " de " + itemType + ".";
                }
                eventos.setText(msg);
            } else {
                eventos.setText("❌ No tienes " + itemType + " en tu inventario.");
            }
        }
    }

    /**
     * Devuelve el objeto Color de JavaFX correspondiente al nombre en español.
     */
    private Color colorDesdeNombre(String nombre) {
        if (nombre == null) return Color.BLUE;
        switch (nombre.toLowerCase()) {
            case "rojo":     return Color.RED;
            case "azul":     return Color.BLUE;
            case "verde":    return Color.GREEN;
            case "amarillo": return Color.YELLOW;
            case "naranja":  return Color.ORANGE;
            case "morado":   return Color.PURPLE;
            case "rosa":     return Color.PINK;
            default:         return Color.GRAY;
        }
    }

    private boolean comprobarFinDePartida() {
        if (juego.isPartidaFinalizada()) {
            winnerLabel.setText("¡GANADOR: " + juego.getGanador().getNombre() + "!");
            gameOverOverlay.setVisible(true);
            dado.setDisable(true);
            return true;
        }
        return false;
    }
}
