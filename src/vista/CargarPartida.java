package vista;

import java.io.IOException;
import java.util.ArrayList;

import controlador.Juego;
import datos.BBDD;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CargarPartida {

    @FXML private TableView<String[]>          tabla_partidas;
    @FXML private TableColumn<String[], String> col_id;
    @FXML private TableColumn<String[], String> col_seed;
    @FXML private TableColumn<String[], String> col_turno;
    @FXML private TableColumn<String[], String> col_fecha;
    @FXML private Label lbl_error;

    private ObservableList<String[]> datos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Asociar columnas con los índices del array
        col_id.setCellValueFactory(cd    -> new SimpleStringProperty(cd.getValue()[0]));
        col_seed.setCellValueFactory(cd  -> new SimpleStringProperty(cd.getValue()[1]));
        col_turno.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[2]));
        col_fecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[3]));

        tabla_partidas.setItems(datos);

        // Cargar datos desde la BD
        cargarDatos();
    }

    private void cargarDatos() {
        datos.clear();
        BBDD bbdd = new BBDD();
        ArrayList<String[]> lista = bbdd.getPartidasPendientes();
        datos.addAll(lista);

        if (lista.isEmpty()) {
            lbl_error.setText("No hay partidas guardadas disponibles.");
        }
    }

    @FXML
    void handleCargar(ActionEvent event) {
        String[] seleccion = tabla_partidas.getSelectionModel().getSelectedItem();
        if (seleccion == null) {
            lbl_error.setText("Selecciona una partida de la lista.");
            return;
        }

        int idPartida;
        try {
            idPartida = Integer.parseInt(seleccion[0]);
        } catch (NumberFormatException e) {
            lbl_error.setText("ID de partida inválido.");
            return;
        }

        GameState.getInstance().resetJuego();
        Juego juego = GameState.getInstance().getJuego();
        boolean ok = juego.getBaseDatos().cargarDatosPartida(idPartida, juego);

        if (ok) {
            cambiarEscena(event, "/vista/JuegoVista.fxml", true);
        } else {
            lbl_error.setText("Error al cargar la partida. Inténtalo de nuevo.");
        }
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
            lbl_error.setText("Error al cargar la pantalla.");
        }
    }
}
