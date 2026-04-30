package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import datos.BBDD;

/**
 * Controlador para la vista unificada de Rankings y Estadísticas.
 * 
 * <p>
 * Esta clase gestiona la interacción del usuario en la pantalla de rankings, 
 * permitiendo solicitar diferentes tipos de informes estadísticos procesados 
 * directamente en el servidor de base de datos Oracle.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class RankingsController {

    /** Área de texto donde se vuelcan los reportes formateados. */
    @FXML
    private TextArea outputArea;

    /** Instancia de acceso a datos. */
    private BBDD bbdd = new BBDD();

    /**
     * Método de inicialización automática de JavaFX.
     * Carga por defecto el ranking general de participación.
     */
    @FXML
    public void initialize() {
        handleRankingGeneral(null);
    }

    /**
     * Solicita y muestra el ranking general de partidas jugadas por cada usuario.
     * 
     * @param event El evento disparado por el botón (puede ser null en el init).
     */
    @FXML
    private void handleRankingGeneral(ActionEvent event) {
        outputArea.setText(">>> CARGANDO RANKING GENERAL DE PARTICIPACIÓN <<<\n\n" + bbdd.obtenerRankingYErroresDBMS());
    }

    /**
     * Solicita y muestra los jugadores que ostentan el récord actual de victorias.
     */
    @FXML
    private void handleRecordAbsoluto(ActionEvent event) {
        outputArea.setText(">>> CARGANDO JUGADORES CON EL RÉCORD ABSOLUTO <<<\n\n" + bbdd.obtenerJugadoresConRecordDBMS());
    }

    /**
     * Solicita y muestra los jugadores que superan la media de victorias del sistema.
     */
    @FXML
    private void handleEncimaMedia(ActionEvent event) {
        outputArea.setText(">>> JUGADORES POR ENCIMA DE LA MEDIA DE VICTORIAS <<<\n\n" + bbdd.obtenerJugadoresEncimaMediaDBMS());
    }

    /**
     * Calcula el nivel del usuario actual basado en sus victorias (simulado para este ejemplo).
     */
    @FXML
    private void handleMiNivel(ActionEvent event) {
        // En una versión final, obtendríamos las victorias reales del GameContext
        int victoriasSimuladas = 5; 
        outputArea.setText(">>> CÁLCULO DE NIVEL COMPARATIVO <<<\n\n" + bbdd.obtenerPorcentajeJugadorDBMS(victoriasSimuladas));
    }

    /**
     * Regresa al menú principal del juego.
     * 
     * @param event El evento disparado por el botón de navegación.
     */
    @FXML
    private void handleVolver(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.RIGHT);
    }
}
