package controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import modelo.PartidaGuardada;
import datos.BBDD;

import java.util.List;

public class LoadGameController {

    @FXML
    private ListView<PartidaGuardada> listaPartidas;

    private BBDD bbdd = new BBDD();

    @FXML
    public void initialize() {
        cargarListaDeBD();
    }

    private void cargarListaDeBD() {
        List<PartidaGuardada> pendientes = bbdd.obtenerPartidasPendientes();
        ObservableList<PartidaGuardada> observablePartidas = FXCollections.observableArrayList(pendientes);
        listaPartidas.setItems(observablePartidas);
    }

    @FXML
    private void handleJugar(ActionEvent event) {
        PartidaGuardada seleccionada = listaPartidas.getSelectionModel().getSelectedItem();

        if (seleccionada == null) {
            mostrarAlerta("Atención", "Por favor, selecciona una partida de la lista para continuar.",
                    Alert.AlertType.WARNING);
            return;
        }

        System.out.println("► Intentando cargar partida ID: " + seleccionada.getIdPartida());

        Juego juegoTemp = new Juego();
        boolean ok = bbdd.cargarDatosPartida(seleccionada.getIdPartida(), juegoTemp);

        if (ok) {
            System.out.println("► Partida cargada con éxito. Redirigiendo a tablero...");
            GameContext context = GameContext.getInstance();
            context.setIdPartidaCargar(seleccionada.getIdPartida());
            context.setSeed(juegoTemp.getTablero().getSeed());
            context.setConfiguredPlayers(juegoTemp.getJugadores());
            context.setTurnoCargado(juegoTemp.getTurnoActual());

            // Navegamos al tablero
            NavigationController.navigateTo(event, "TableroJuego.fxml");
        } else {
            mostrarAlerta("Error de Carga", "No se pudo cargar la partida seleccionada desde la base de datos.",
                    Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml");
    }

    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        try {
            alerta.getDialogPane().getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
        } catch (Exception e) {
        }
        alerta.showAndWait();
    }
}
