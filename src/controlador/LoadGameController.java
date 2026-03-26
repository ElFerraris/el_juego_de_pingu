package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.util.Map;
import java.util.ArrayList;
import modelo.Jugador;
import datos.BBDD;
import controlador.Juego;

public class LoadGameController {

    @FXML private ListView<String> savedGamesListView;
    private BBDD db = new BBDD();
    private Map<Integer, String> gamesMap;

    @FXML
    public void initialize() {
        Jugador current = GameContext.getInstance().getCurrentUser();
        if (current != null) {
            ArrayList<String> partidas = db.obtenerPartidasGuardadas(current.getNombre());
            ObservableList<String> items = FXCollections.observableArrayList(partidas);
            savedGamesListView.setItems(items);
        }
    }

    @FXML
    private void handleConfirmLoadGame(ActionEvent event) {
        String selected = savedGamesListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // "Partida #ID (Seed: ...)"
        try {
            int hashPos = selected.indexOf("#");
            int spacePos = selected.indexOf(" ", hashPos);
            String idStr = selected.substring(hashPos + 1, spacePos);
            int idPartida = Integer.parseInt(idStr);
            loadGame(event, idPartida);
        } catch (Exception e) {
            System.err.println("Error al parsear ID de partida: " + e.getMessage());
        }
    }

    @FXML
    private void showMainMenu(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml");
    }

    private void loadGame(ActionEvent event, int idPartida) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/PantallaJuego.fxml"));
            Parent root = loader.load();
            
            JuegoController gameController = loader.getController();
            Juego nuevoJuego = new Juego();
            
            boolean exito = db.cargarDatosPartida(idPartida, nuevoJuego);
            if (!exito) {
                System.err.println("Error al cargar partida desde DB");
                return;
            }
            
            gameController.setJuego(nuevoJuego);
            gameController.inicializarActual();

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/vista/style.css").toExternalForm());
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
