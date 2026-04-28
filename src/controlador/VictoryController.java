package controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import modelo.Jugador;
import modelo.Foca;
import util.SoundManager;

/**
 * Controlador para la pantalla de victoria.
 * 
 * <p>
 * Muestra al ganador de la partida de forma destacada y permite navegar
 * de vuelta al menú principal o configurar una nueva partida.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class VictoryController {

    @FXML
    private ImageView winnerImageView;
    @FXML
    private Label winnerNameLabel;

    /**
     * Inicializa la vista con los datos del ganador almacenados en el contexto.
     */
    @FXML
    public void initialize() {
        // Reproducir música de menú para celebrar
        SoundManager.playMenuMusic();

        Jugador winner = GameContext.getInstance().getWinner();
        if (winner != null) {
            winnerNameLabel.setText(winner.getNombre().toUpperCase());
            
            // Cargar imagen grande del jugador
            String colorName = winner.getColor() != null ? winner.getColor() : "Azul";
            if (winner instanceof Foca) {
                colorName = "Gris";
            }
            
            try {
                // El nombre del archivo en /assets/jugadores/ empieza con Mayúscula (ej: Azul.png)
                String formattedColor = colorName.substring(0, 1).toUpperCase() + colorName.substring(1).toLowerCase();
                Image img = new Image(getClass().getResourceAsStream("/assets/jugadores/" + formattedColor + ".png"));
                winnerImageView.setImage(img);
            } catch (Exception e) {
                System.err.println("Error cargando imagen de victoria: " + e.getMessage());
            }
        }
    }

    /**
     * Navega a la pantalla de configuración de una nueva partida.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleNewGame(ActionEvent event) {
        // Limpiamos los datos de la partida anterior pero mantenemos el usuario logueado
        Jugador current = GameContext.getInstance().getCurrentUser();
        GameContext.getInstance().reset();
        GameContext.getInstance().setCurrentUser(current);
        
        NavigationController.navigateTo(event, "PlayerConfigView.fxml", NavigationController.Direction.FORWARD);
    }

    /**
     * Navega de vuelta al menú principal.
     * 
     * @param event El evento de acción.
     */
    @FXML
    private void handleMainMenu(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }
}
