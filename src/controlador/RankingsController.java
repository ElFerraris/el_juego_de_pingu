package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import datos.BBDD;
import modelo.Jugador;

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
        // Obtenemos el usuario logueado actualmente
        Jugador current = GameContext.getInstance().getCurrentUser();
        
        if (current != null) {
            // Buscamos sus victorias reales en la BD
            int victorias = bbdd.obtenerVictoriasTotales(current.getId());
            
            outputArea.setText("Calculando tu nivel de victorias (" + victorias + ")...\n\n" 
                + bbdd.obtenerPorcentajeJugadorDBMS(victorias));
        } else {
            outputArea.setText("No se ha podido identificar al usuario actual.\nPor favor, inicia sesión de nuevo.");
        }
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.RIGHT);
    }
}
