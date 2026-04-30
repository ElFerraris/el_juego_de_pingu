package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
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

    @FXML
    private Button btnGeneral;
    @FXML
    private Button btnRecord;
    @FXML
    private Button btnMedia;
    @FXML
    private Button btnNivel;

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
     * Resalta visualmente el botón seleccionado y apaga el resto.
     */
    private void setActiveButton(Button activeButton) {
        if (btnGeneral != null) btnGeneral.getStyleClass().remove("button-active");
        if (btnRecord != null) btnRecord.getStyleClass().remove("button-active");
        if (btnMedia != null) btnMedia.getStyleClass().remove("button-active");
        if (btnNivel != null) btnNivel.getStyleClass().remove("button-active");

        if (activeButton != null && !activeButton.getStyleClass().contains("button-active")) {
            activeButton.getStyleClass().add("button-active");
        }
    }

    /**
     * Solicita y muestra el ranking general de partidas jugadas por cada usuario.
     * 
     * @param event El evento disparado por el botón (puede ser null en el init).
     */
    @FXML
    private void handleRankingGeneral(ActionEvent event) {
        setActiveButton(btnGeneral);
        outputArea.setText(">>> CARGANDO RANKING GENERAL DE PARTICIPACIÓN <<<\n\n" + bbdd.obtenerRankingYErroresDBMS());
    }

    /**
     * Solicita y muestra los jugadores que ostentan el récord actual de victorias.
     */
    @FXML
    private void handleRecordAbsoluto(ActionEvent event) {
        setActiveButton(btnRecord);
        outputArea.setText(">>> CARGANDO JUGADORES CON EL RÉCORD ABSOLUTO <<<\n\n" + bbdd.obtenerJugadoresConRecordDBMS());
    }

    /**
     * Solicita y muestra los jugadores que superan la media de victorias del sistema.
     */
    @FXML
    private void handleEncimaMedia(ActionEvent event) {
        setActiveButton(btnMedia);
        outputArea.setText(">>> JUGADORES POR ENCIMA DE LA MEDIA DE VICTORIAS <<<\n\n" + bbdd.obtenerJugadoresEncimaMediaDBMS());
    }

    /**
     * Calcula el nivel del usuario actual basado en sus victorias reales en la base de datos.
     */
    @FXML
    private void handleMiNivel(ActionEvent event) {
        setActiveButton(btnNivel);
        // Obtenemos el usuario logueado actualmente
        modelo.Jugador current = GameContext.getInstance().getCurrentUser();
        
        if (current != null) {
            // Buscamos sus victorias reales en la BD
            int victorias = bbdd.obtenerVictoriasTotales(current.getId());
            
            outputArea.setText(">>> CÁLCULO DE TU NIVEL DE VICTORIAS (" + victorias + ") <<<\n\n" 
                + bbdd.obtenerPorcentajeJugadorDBMS(victorias));
        } else {
            outputArea.setText("No se ha podido identificar al usuario actual.\nPor favor, inicia sesión de nuevo.");
        }
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
