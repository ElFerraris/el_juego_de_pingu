package controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.application.Platform;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import modelo.PartidaGuardada;
import datos.BBDD;
import util.UIUtils;

import java.util.List;
import java.util.Optional;

public class LoadGameController {

    @FXML
    private ListView<PartidaGuardada> listaPartidas;

    @FXML
    private Button btnJugar;

    @FXML
    private Button btnEliminar;

    @FXML
    private StackPane deleteConfirmOverlay;

    private BBDD bbdd = new BBDD();

    @FXML
    public void initialize() {
        if (btnJugar != null) btnJugar.setDisable(true);
        cargarListaDeBD();
        
        // Personalizar la apariencia de cada fila
        listaPartidas.setCellFactory(lv -> new PartidaCell());
        
        // Deshabilitar botón jugar si no hay nada seleccionado
        if (btnJugar != null) {
            btnJugar.disableProperty().bind(listaPartidas.getSelectionModel().selectedItemProperty().isNull());
        }
        if (btnEliminar != null) {
            btnEliminar.disableProperty().bind(listaPartidas.getSelectionModel().selectedItemProperty().isNull());
        }
    }

    /**
     * Celda personalizada para mostrar la partida de forma visual y moderna.
     */
    private class PartidaCell extends ListCell<PartidaGuardada> {
        private HBox content;
        private VBox leftBox;
        private Label nameLabel;
        private Label dateLabel;
        private HBox playerIcons;

        public PartidaCell() {
            super();
            nameLabel = new Label();
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
            
            dateLabel = new Label();
            dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            
            leftBox = new VBox(5, nameLabel, dateLabel);
            
            playerIcons = new HBox(8);
            playerIcons.setAlignment(Pos.CENTER_RIGHT);
            
            content = new HBox(leftBox);
            HBox.setHgrow(leftBox, Priority.ALWAYS);
            content.getChildren().add(playerIcons);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(10, 15, 10, 15));
            
            // Estilo para la celda cuando se selecciona
            this.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    content.setStyle("-fx-background-color: #1e88e5; -fx-background-radius: 5;");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: white;");
                    dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e0e0e0;");
                } else {
                    content.setStyle("-fx-background-color: transparent;");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");
                    dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
                }
            });
        }

        @Override
        protected void updateItem(PartidaGuardada item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                String nombre = item.getNombrePartida();
                if (nombre == null || nombre.isEmpty()) {
                    nombre = "Partida #" + item.getIdPartida();
                }
                nameLabel.setText(nombre.toUpperCase());
                dateLabel.setText("Jugada el: " + item.getHoraPartida());
                
                playerIcons.getChildren().clear();
                if (item.getColoresJugadores() != null) {
                    for (String colorName : item.getColoresJugadores()) {
                        ImageView icon = new ImageView();
                        try {
                            String colorNormalizado = (colorName != null && !colorName.trim().isEmpty()) ? colorName.toLowerCase() : "gris";
                            Image img = new Image(getClass().getResourceAsStream("/assets/ico_jugadores/ico_" + colorNormalizado + ".png"));
                            icon.setImage(img);
                        } catch (Exception e) {}
                        icon.setFitWidth(16);
                        icon.setFitHeight(16);
                        icon.setPreserveRatio(true);
                        
                        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
                        ds.setColor(Color.WHITE);
                        ds.setRadius(2);
                        ds.setSpread(1.0);
                        icon.setEffect(ds);
                        
                        playerIcons.getChildren().add(icon);
                    }
                }
                
                setGraphic(content);
                setText(null);
            }
        }
    }

    private void cargarListaDeBD() {
        try {
            int idUsuarioActual = GameContext.getInstance().getCurrentUser().getId();
            List<PartidaGuardada> pendientes = bbdd.obtenerPartidasPendientes(idUsuarioActual);
            ObservableList<PartidaGuardada> observablePartidas = FXCollections.observableArrayList(pendientes);
            listaPartidas.setItems(observablePartidas);
        } catch (Exception e) {
            System.err.println("Error cargando lista: " + e.getMessage());
        }
    }

    @FXML
    private void handleJugar(ActionEvent event) {
        PartidaGuardada seleccionada = listaPartidas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            try {
                Juego juegoTemp = new Juego();
                boolean ok = bbdd.cargarDatosPartida(seleccionada.getIdPartida(), juegoTemp);

                if (ok) {
                    System.out.println("► Partida cargada con éxito. Redirigiendo a tablero...");
                    GameContext context = GameContext.getInstance();
                    context.setIdPartidaCargar(seleccionada.getIdPartida());
                    context.setSeed(juegoTemp.getTablero().getSeed());
                    context.setConfiguredPlayers(juegoTemp.getJugadores());
                    context.setTurnoCargado(juegoTemp.getTurnoActual());
                    
                    NavigationController.navigateToBoardAsync(event, "TableroJuego.fxml");
                } else {
                    mostrarAlerta("Error de Carga", "No se pudo cargar la partida seleccionada.", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                System.err.println("Error al jugar partida: " + e.getMessage());
            }
        }
    }


    @FXML
    private void handleVolver(ActionEvent event) {
        // Volvemos al menú principal con transición hacia atrás (baja)
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }
    
    @FXML
    private void handleEliminar(ActionEvent event) {
        PartidaGuardada seleccionada = listaPartidas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            deleteConfirmOverlay.toFront();
            deleteConfirmOverlay.setVisible(true);
        }
    }
    
    @FXML
    private void handleCancelarEliminar(ActionEvent event) {
        deleteConfirmOverlay.setVisible(false);
    }
    
    @FXML
    private void handleConfirmarEliminar(ActionEvent event) {
        deleteConfirmOverlay.setVisible(false);
        PartidaGuardada seleccionada = listaPartidas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            boolean exito = bbdd.eliminarPartida(seleccionada.getIdPartida());
            if (exito) {
                // Refrescamos la lista
                cargarListaDeBD();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar la partida.", Alert.AlertType.ERROR);
            }
        }
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
