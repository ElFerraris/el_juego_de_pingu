package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import datos.BBDD;

/**
 * Controlador para la nueva vista unificada de Rankings.
 */
public class RankingsController {

    @FXML
    private TextArea outputArea;

    private BBDD bbdd = new BBDD();

    @FXML
    public void initialize() {
        // Cargar por defecto el ranking general
        handleRankingGeneral(null);
    }

    @FXML
    private void handleRankingGeneral(ActionEvent event) {
        outputArea.setText("Cargando Ranking General...\n\n" + bbdd.obtenerRankingYErroresDBMS());
    }

    @FXML
    private void handleRecordAbsoluto(ActionEvent event) {
        outputArea.setText("Cargando Jugadores con Récord...\n\n" + bbdd.obtenerJugadoresConRecordDBMS());
    }

    @FXML
    private void handleEncimaMedia(ActionEvent event) {
        outputArea.setText("Cargando Jugadores encima de la media...\n\n" + bbdd.obtenerJugadoresEncimaMediaDBMS());
    }

    @FXML
    private void handleMiNivel(ActionEvent event) {
        // Ejemplo con 5 victorias (sería ideal pasarlo desde el contexto del usuario)
        outputArea.setText("Calculando tu nivel de victorias...\n\n" + bbdd.obtenerPorcentajeJugadorDBMS(5));
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.RIGHT);
    }
}
