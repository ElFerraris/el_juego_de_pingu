package controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import modelo.PartidaGuardada;
import datos.BBDD;

import java.util.List;

public class LoadGameController {

    @FXML
    private ListView<PartidaGuardada> listaPartidas;

    @FXML
    private Button btnJugar;

    private BBDD bbdd = new BBDD();

    @FXML
    public void initialize() {
        if (btnJugar != null) btnJugar.setDisable(true);
        cargarListaDeBD();
        
        // Deshabilitar botón jugar si no hay nada seleccionado (binding posterior)
        if (btnJugar != null) {
            btnJugar.disableProperty().bind(listaPartidas.getSelectionModel().selectedItemProperty().isNull());
        }
    }

    private void cargarListaDeBD() {
        // Obtenemos la escena si ya existe, si no, lo intentaremos después
        Platform.runLater(() -> {
             NavigationController.showLoading(listaPartidas.getScene());
        });

        new Thread(() -> {
            try {
                List<PartidaGuardada> pendientes = bbdd.obtenerPartidasPendientes();
                Platform.runLater(() -> {
                    ObservableList<PartidaGuardada> observablePartidas = FXCollections.observableArrayList(pendientes);
                    listaPartidas.setItems(observablePartidas);
                    NavigationController.hideLoading();
                });
            } catch (Exception e) {
                Platform.runLater(NavigationController::hideLoading);
                System.err.println("Error cargando lista: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleJugar(ActionEvent event) {
        PartidaGuardada seleccionada = listaPartidas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) return;

        NavigationController.showLoading(listaPartidas.getScene());
        
        new Thread(() -> {
            try {
                Juego juegoTemp = new Juego();
                boolean ok = bbdd.cargarDatosPartida(seleccionada.getIdPartida(), juegoTemp);

                Platform.runLater(() -> {
                    if (ok) {
                        System.out.println("► Partida cargada con éxito. Redirigiendo a tablero...");
                        GameContext context = GameContext.getInstance();
                        context.setIdPartidaCargar(seleccionada.getIdPartida());
                        context.setSeed(juegoTemp.getTablero().getSeed());
                        context.setConfiguredPlayers(juegoTemp.getJugadores());
                        context.setTurnoCargado(juegoTemp.getTurnoActual());
                        
                        NavigationController.hideLoading();
                        NavigationController.navigateTo(event, "TableroJuego.fxml", NavigationController.Direction.TO_BOARD);
                    } else {
                        NavigationController.hideLoading();
                        mostrarAlerta("Error de Carga", "No se pudo cargar la partida seleccionada.", Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(NavigationController::hideLoading);
            }
        }).start();
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        // Volvemos al menú principal con transición hacia atrás (baja)
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }
    
    private void mostrarAlerta(String titulo, String contenido, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        try {
            alerta.getDialogPane().getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
        } catch (Exception e) {}
        alerta.showAndWait();
    }
}
