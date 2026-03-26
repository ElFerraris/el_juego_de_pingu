package vista;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import controlador.Juego;
import datos.BBDD;

public class NuevaPartida {

    @FXML private RadioButton rb_seed_random;
    @FXML private RadioButton rb_seed_manual;
    @FXML private TextField   txt_seed;
    @FXML private Spinner<Integer> spinner_jugadores;

    @FXML private ComboBox<String> combo_nombre1;
    @FXML private ComboBox<String> combo_color1;
    @FXML private HBox row1;

    @FXML private ComboBox<String> combo_nombre2;
    @FXML private ComboBox<String> combo_color2;
    @FXML private HBox row2;

    @FXML private ComboBox<String> combo_nombre3;
    @FXML private ComboBox<String> combo_color3;
    @FXML private HBox row3;

    @FXML private ComboBox<String> combo_nombre4;
    @FXML private ComboBox<String> combo_color4;
    @FXML private HBox row4;

    @FXML private Label lbl_form_error;
    @FXML private CheckBox chk_cpu;

    private static final List<String> COLORES_DISPONIBLES = Arrays.asList(
        "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa", "Gris"
    );

    private List<String> jugadoresBBDD;
    
    // Arrays para facilitar iteración
    private HBox[] rows;
    private ComboBox<String>[] combosNombre;
    private ComboBox<String>[] combosColor;

    @SuppressWarnings("unchecked")
    @FXML
    public void initialize() {
        spinner_jugadores.valueProperty().addListener((obs, old, nv) -> actualizarFilas(nv));
        actualizarFilas(2); // Empezamos con 2
    }

    private void initCombo(ComboBox<String> cb) {
        cb.getItems().setAll(TODOS_LOS_COLORES);
    }

    @FXML
    void handleSeedMode(ActionEvent event) {
        boolean manual = rb_manual.isSelected();
        txt_seed.setDisable(!manual);
        lbl_seed_error.setText(" ");
    }

    @FXML
    void handleNumJugadores(ActionEvent event) {
        actualizarFilas(spinner_jugadores.getValue());
    }

    private void actualizarFilas(int n) {
        setRowVisible(row3, n >= 3);
        setRowVisible(row4, n >= 4);
    }

    private void setRowVisible(HBox row, boolean visible) {
        row.setVisible(visible);
        row.setManaged(visible);
    }

    @FXML
    void handleJugar(ActionEvent event) {
        lbl_seed_error.setText(" ");
        lbl_form_error.setText(" ");

        // 1. Obtener / generar seed
        String seed;
        Juego juego = GameState.getInstance().getJuego();

        if (rb_aleatorio.isSelected()) {
            juego.getTablero().generarSeedAleatoria();
            seed = juego.getTablero().getSeed();
        } else {
            seed = txt_seed.getText().trim();
            if (!juego.getTablero().validarSeed(seed)) {
                lbl_seed_error.setText("Seed inválida (necesita al menos 2 agujeros y 2 trineos, 48 dígitos 0-5).");
                return;
            }
        }

        // 2. Recoger datos de jugadores
        int numJugadores = spinner_jugadores.getValue();
        TextField[] nombres = {txt_nombre1, txt_nombre2, txt_nombre3, txt_nombre4};
        ComboBox<?>[] combos = {combo_color1, combo_color2, combo_color3, combo_color4};

        ArrayList<String> nombresUsados = new ArrayList<>();
        ArrayList<String> coloresUsados = new ArrayList<>();
        String[][] datos = new String[numJugadores][2];

        for (int i = 0; i < numJugadores; i++) {
            String nombre = nombres[i].getText().trim();
            String color  = (String) combos[i].getValue();

            if (nombre.isEmpty()) {
                lbl_form_error.setText("El nombre del jugador " + (i + 1) + " no puede estar vacío.");
                return;
            }
            if (nombresUsados.contains(nombre)) {
                lbl_form_error.setText("El nombre '" + nombre + "' está repetido.");
                return;
            }
            if (color == null || coloresUsados.contains(color)) {
                lbl_form_error.setText("El color del jugador " + (i + 1) + " ya está en uso.");
                return;
            }

            nombresUsados.add(nombre);
            coloresUsados.add(color);
            datos[i][0] = nombre;
            datos[i][1] = color;
        }

        // 3. Configurar la partida (sin Scanner)
        GameState.getInstance().resetJuego();
        Juego nuevaPartida = GameState.getInstance().getJuego();
        nuevaPartida.configurarPartidaUI(seed, datos);
        nuevaPartida.iniciarPartida();

        // 4. Ir a la pantalla de juego
        cambiarEscena(event, "/vista/JuegoVista.fxml", true);
    }

    @FXML
    void handleVolver(ActionEvent event) {
        cambiarEscena(event, "/vista/MenuPrincipal.fxml", false);
    }

    private void cambiarEscena(ActionEvent event, String fxmlPath, boolean fullscreen) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setFullScreen(fullscreen);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lbl_form_error.setText("Error al cargar la pantalla.");
        }
    }
}
