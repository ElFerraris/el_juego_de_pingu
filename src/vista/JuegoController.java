package vista;

import java.io.IOException;
import java.util.ArrayList;

import controlador.Juego;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import modelo.CPU;
import modelo.DadoLento;
import modelo.DadoRapido;
import modelo.Jugador;
import modelo.Objeto;
import modelo.Tablero;
import modelo.Casilla;

public class JuegoController {

    @FXML private GridPane grid_tablero;
    @FXML private Label     lbl_turno;
    @FXML private Label     lbl_posicion;
    @FXML private Label     lbl_inventario;
    @FXML private ComboBox<String> combo_dado;
    @FXML private Button    btn_dado;
    @FXML private TextArea  txt_log;

    // Colores para los tipos de casilla
    private static final String STYLE_NORMAL      = "-fx-background-color: #D6F9FF; -fx-border-color: #1A2B3C; -fx-border-width: 2;";
    private static final String STYLE_OSO         = "-fx-background-color: #8B4513; -fx-border-color: #1A2B3C; -fx-border-width: 2;";
    private static final String STYLE_AGUJERO     = "-fx-background-color: #1E3A5F; -fx-border-color: #87CEEB; -fx-border-width: 2;";
    private static final String STYLE_TRINEO      = "-fx-background-color: #2ECC71; -fx-border-color: #1A2B3C; -fx-border-width: 2;";
    private static final String STYLE_INTERROGANTE= "-fx-background-color: #9B59B6; -fx-border-color: #1A2B3C; -fx-border-width: 2;";
    private static final String STYLE_ROMPEDIZA   = "-fx-background-color: #E74C3C; -fx-border-color: #1A2B3C; -fx-border-width: 2;";

    private Juego juego;
    // Array de Labels para las 50 casillas (índice = posición del tablero)
    private Label[] casillasUI = new Label[Tablero.TAMANYO_TABLERO + 1];

    @FXML
    public void initialize() {
        juego = GameState.getInstance().getJuego();
        construirTablero();
        actualizarUI();
        // Si el primer turno es CPU, ejecutarlo automáticamente
        ejecutarTurnoCPUSiToca();
    }

    // ─────────────────────────────────────────────────────────────
    //  CONSTRUCCIÓN DEL TABLERO
    // ─────────────────────────────────────────────────────────────

    private void construirTablero() {
        grid_tablero.getChildren().clear();
        int cols = 10;
        int rows = (Tablero.TAMANYO_TABLERO / cols) + 1; // 5 filas

        for (int pos = 0; pos < Tablero.TAMANYO_TABLERO; pos++) {
            int fila  = pos / cols;
            int col   = (fila % 2 == 0) ? (pos % cols) : (cols - 1 - pos % cols); // serpentina

            Label lbl = new Label();
            lbl.setMinSize(110, 80);
            lbl.setMaxSize(110, 80);
            lbl.setAlignment(Pos.CENTER);
            lbl.setWrapText(true);
            lbl.setStyle(estiloParaCasilla(pos));

            // Texto: número de casilla + tipo abreviado
            String tipo = obtenerTipoCorto(pos);
            lbl.setText(pos + "\n" + tipo);
            lbl.setStyle(lbl.getStyle() + "-fx-font-size:10px; -fx-text-alignment:center; -fx-padding:4;");

            casillasUI[pos] = lbl;
            grid_tablero.add(lbl, col, fila);
        }
    }

    private String estiloParaCasilla(int pos) {
        Casilla c = juego.getTablero().getCasilla(pos);
        if (c == null) return STYLE_NORMAL;
        switch (c.getTipo()) {
            case "Casilla OSO":         return STYLE_OSO;
            case "Casilla AGUJERO":     return STYLE_AGUJERO;
            case "Casilla TRINEO":      return STYLE_TRINEO;
            case "Casilla INTERROGANTE":return STYLE_INTERROGANTE;
            case "Casilla ROMPEDIZAS":  return STYLE_ROMPEDIZA;
            default:                    return STYLE_NORMAL;
        }
    }

    private String obtenerTipoCorto(int pos) {
        Casilla c = juego.getTablero().getCasilla(pos);
        if (c == null) return "";
        switch (c.getTipo()) {
            case "Casilla OSO":         return "🐻";
            case "Casilla AGUJERO":     return "🕳️";
            case "Casilla TRINEO":      return "🛷";
            case "Casilla INTERROGANTE":return "❓";
            case "Casilla ROMPEDIZAS":  return "❄️";
            default:                    return "";
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  ACTUALIZACIÓN DE LA UI
    // ─────────────────────────────────────────────────────────────

    private void actualizarUI() {
        // Limpiar fichas de las casillas
        limpiarFichas();

        // Pintar fichas de cada jugador en su casilla
        for (Jugador j : juego.getJugadores()) {
            int pos = Math.min(j.getPosicion(), Tablero.TAMANYO_TABLERO - 1);
            if (casillasUI[pos] != null) {
                String emoji = (j instanceof CPU) ? "🦭" : "🐧";
                String actual = casillasUI[pos].getText();
                // Añadir el emoji del jugador al texto de la casilla
                if (!actual.contains(emoji + j.getColor().charAt(0))) {
                    casillasUI[pos].setText(actual + "\n" + emoji + colorAEmoji(j.getColor()));
                }
            }
        }

        // Actualizar HUD con el jugador del turno actual
        Jugador actual = juego.getJugador(juego.getTurnoActual());
        lbl_turno.setText("🐧 " + actual.getNombre()
            + (actual instanceof CPU ? " (CPU)" : "")
            + (actual.estaBloqueado() ? " 🔒" : ""));
        lbl_posicion.setText("Casilla " + actual.getPosicion() + " / " + (Tablero.TAMANYO_TABLERO - 1));

        // Inventario
        int peces  = actual.getInventario().getCantidad("Pez");
        int bolas  = actual.getInventario().getCantidad("BolaNieve");
        int rapidos= actual.getInventario().getCantidad("DadoRapido");
        int lentos = actual.getInventario().getCantidad("DadoLento");
        lbl_inventario.setText(
            "🐟 Peces: " + peces + "   ❄️ Bolas: " + bolas + "\n" +
            "⚡ Dado Rápido: " + rapidos + "   🐢 Dado Lento: " + lentos
        );

        // ComboBox de dados
        combo_dado.getItems().clear();
        combo_dado.getItems().add("🎲 Dado Normal");
        if (rapidos > 0) combo_dado.getItems().add("⚡ Dado Rápido");
        if (lentos  > 0) combo_dado.getItems().add("🐢 Dado Lento");
        combo_dado.getSelectionModel().selectFirst();

        // Deshabilitar botón si es CPU o está bloqueado
        boolean esCPU = (actual instanceof CPU);
        btn_dado.setDisable(esCPU);
        combo_dado.setDisable(esCPU);
    }

    private void limpiarFichas() {
        // Reconstruir texto base (número + tipo) sin fichas
        for (int pos = 0; pos < Tablero.TAMANYO_TABLERO; pos++) {
            if (casillasUI[pos] != null) {
                String tipo = obtenerTipoCorto(pos);
                casillasUI[pos].setText(pos + "\n" + tipo);
            }
        }
    }

    private String colorAEmoji(String color) {
        switch (color) {
            case "Rojo":    return "🔴";
            case "Azul":    return "🔵";
            case "Verde":   return "🟢";
            case "Amarillo":return "🟡";
            case "Naranja": return "🟠";
            case "Morado":  return "🟣";
            case "Rosa":    return "🌸";
            case "Gris":    return "⚫";
            default:        return "⚪";
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  LÓGICA DE TURNO
    // ─────────────────────────────────────────────────────────────

    @FXML
    void handleTirarDado(ActionEvent event) {
        Jugador jugadorActual = juego.getJugador(juego.getTurnoActual());

        // Aplicar turno según dado seleccionado
        String dadoElegido = combo_dado.getValue();
        int resultado = 0;

        if (dadoElegido != null && dadoElegido.contains("Rápido")) {
            jugadorActual.getInventario().usarDadoEspecifico("Rapido", jugadorActual);
            resultado = jugadorActual.getPosicion(); // posición ya actualizada
            log("⚡ " + jugadorActual.getNombre() + " usó Dado Rápido → posición " + jugadorActual.getPosicion());
        } else if (dadoElegido != null && dadoElegido.contains("Lento")) {
            jugadorActual.getInventario().usarDadoEspecifico("Lento", jugadorActual);
            log("🐢 " + jugadorActual.getNombre() + " usó Dado Lento → posición " + jugadorActual.getPosicion());
        } else {
            resultado = jugadorActual.tirarDado();
            log("🎲 " + jugadorActual.getNombre() + " sacó un " + resultado + " → posición " + jugadorActual.getPosicion());
        }

        // Guerra si hay coincidencia
        juego.comprobarGuerra(jugadorActual);

        // Efecto de casilla
        juego.getTablero().aplicarEfectoCasilla(jugadorActual);
        log("📍 Casilla: " + jugadorActual.getPosicion());

        actualizarUI();

        // Comprobar ganador
        Jugador ganador = juego.comprobarGanador();
        if (ganador != null) {
            irAVictoria(ganador);
            return;
        }

        // Cambiar turno y ejecutar CPU si toca
        juego.cambiarTurno();
        actualizarUI();
        ejecutarTurnoCPUSiToca();
    }

    private void ejecutarTurnoCPUSiToca() {
        Jugador actual = juego.getJugador(juego.getTurnoActual());
        if (actual instanceof CPU) {
            btn_dado.setDisable(true);
            // Pequeña pausa para que el jugador vea el cambio de turno
            PauseTransition pausa = new PauseTransition(Duration.millis(1200));
            pausa.setOnFinished(e -> {
                CPU cpu = (CPU) actual;
                if (cpu.estaBloqueado()) {
                    log("🔒 " + cpu.getNombre() + " está bloqueada " + cpu.getTurnosBloqueados() + " turno(s).");
                    cpu.setTurnosBloqueados(cpu.getTurnosBloqueados() - 1);
                } else {
                    cpu.decidirAccion(juego.getTablero(), juego.getJugadores());
                    log("🦭 " + cpu.getNombre() + " avanza → posición " + cpu.getPosicion());
                    juego.comprobarGuerra(cpu);
                    juego.getTablero().aplicarEfectoCasilla(cpu);
                    log("📍 CPU en casilla: " + cpu.getPosicion());
                }
                actualizarUI();

                // Comprobar si la CPU ganó
                Jugador ganador = juego.comprobarGanador();
                if (ganador != null) {
                    irAVictoria(ganador);
                    return;
                }
                juego.cambiarTurno();
                actualizarUI();
                // Por si el siguiente también es CPU (no debería pasar, pero por seguridad)
                ejecutarTurnoCPUSiToca();
            });
            pausa.play();
        }
    }

    private void irAVictoria(Jugador ganador) {
        log("🏆 ¡" + ganador.getNombre() + " ha ganado!");
        btn_dado.setDisable(true);
        // Pequeña pausa antes de navegar
        PauseTransition pausa = new PauseTransition(Duration.seconds(2));
        pausa.setOnFinished(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/Victoria.fxml"));
                Parent root = loader.load();
                // Pasar ganador al controlador de victoria
                Victoria ctrl = loader.getController();
                ctrl.setGanador(ganador);

                Scene scene = new Scene(root);
                Stage stage = (Stage) btn_dado.getScene().getWindow();
                stage.setFullScreen(false);
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        pausa.play();
    }

    // ─────────────────────────────────────────────────────────────
    //  BOTONES AUXILIARES
    // ─────────────────────────────────────────────────────────────

    @FXML
    void handleGuardar(ActionEvent event) {
        juego.guardarPartida();
        log("💾 Partida guardada.");
    }

    @FXML
    void handleMenu(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/vista/MenuPrincipal.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setFullScreen(false);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void log(String mensaje) {
        txt_log.appendText(mensaje + "\n");
    }
}
