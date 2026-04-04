package controlador;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import modelo.Jugador;
import modelo.CPU;
import datos.BBDD;

/**
 * PlayerConfigController
 * 
 * Gestiona la configuración de los jugadores antes de empezar una partida.
 * Permite seleccionar nombres de la BD, elegir colores y añadir la Foca Loca.
 */
public class PlayerConfigController {

    @FXML private HBox playerSlotsContainer;
    @FXML private CheckBox focaCheckbox;
    @FXML private Label errorLabel;

    private List<Slot> slots = new ArrayList<>();
    private List<String> allPlayerNames = new ArrayList<>();
    private static final int MAX_PLAYERS = 4;
    private static final String[] COLORS = {"Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"};
    private BBDD db = new BBDD();

    @FXML
    public void initialize() {
        // Cargamos los nombres de la BD en un hilo secundario para no bloquear la animación
        new Thread(() -> {
            try {
                // Notamos que necesitamos la escena para mostrar el loading
                Platform.runLater(() -> {
                    if (playerSlotsContainer.getScene() != null) {
                        NavigationController.showLoading(playerSlotsContainer.getScene());
                    }
                });

                List<String> names = db.obtenerTodosLosJugadores();
                
                Platform.runLater(() -> {
                    allPlayerNames = names;
                    setupInitialSlots();
                    NavigationController.hideLoading();
                });
            } catch (Exception e) {
                Platform.runLater(NavigationController::hideLoading);
                System.err.println("Error cargando jugadores en segundo plano: " + e.getMessage());
            }
        }).start();
    }

    private void setupInitialSlots() {
        playerSlotsContainer.getChildren().clear();
        slots.clear();
        
        // Al menos 2 jugadores obligatorios
        for (int i = 0; i < 2; i++) {
            addSlot(true);
        }
        updateSlotsUI();
    }

    private void addSlot(boolean mandatory) {
        Slot slot = new Slot(mandatory);
        slots.add(slot);
    }

    private void updateSlotsUI() {
        playerSlotsContainer.getChildren().clear();
        for (int i = 0; i < slots.size(); i++) {
            Slot s = slots.get(i);
            // Re-indexamos el título (P1, P2...)
            Label title = (Label) ((HBox) s.getCard().getChildren().get(0)).getChildren().get(0);
            title.setText("P" + (i + 1));
            
            playerSlotsContainer.getChildren().add(s.getCard());
        }

        // Si hay menos del máximo, mostramos el botón de añadir "+"
        if (slots.size() < MAX_PLAYERS) {
            Button addButton = new Button("+");
            addButton.getStyleClass().add("add-player-button");
            addButton.setPrefSize(180, 220);
            addButton.setOnAction(e -> {
                addSlot(false);
                updateSlotsUI();
            });
            util.UIUtils.applyHoverAnimation(addButton);
            playerSlotsContainer.getChildren().add(addButton);
        }
        updateAllComboCells();
    }

    private boolean isUpdatingAll = false;

    private void updateAllComboCells() {
        if (isUpdatingAll) return;
        isUpdatingAll = true;
        try {
            for (Slot s : slots) {
                s.refreshCombo();
            }
        } finally {
            isUpdatingAll = false;
        }
    }

    @FXML
    private void showSeedSelection(ActionEvent event) {
        List<Jugador> configured = new ArrayList<>();
        for (Slot s : slots) {
            String name = s.getName();
            if (name == null || name.trim().isEmpty()) {
                errorLabel.setText("Todos los jugadores deben tener nombre");
                return;
            }
            // Color por defecto en el constructor, se actualizará según el Slot
            configured.add(new Jugador(-1, name, s.getColor()));
        }
        
        if (focaCheckbox.isSelected()) {
            configured.add(new CPU(-1, "Foca Loca"));
        }

        if (configured.size() < 2) {
            errorLabel.setText("Mínimo 2 jugadores para empezar");
            return;
        }

        // Guardamos los jugadores configurados en el contexto global
        GameContext.getInstance().setConfiguredPlayers(configured);
        
        // Navegamos a la selección de SEED (Mundo) con transición hacia adelante (sube)
        NavigationController.navigateTo(event, "SeedSelectionView.fxml", NavigationController.Direction.FORWARD);
    }

    @FXML
    private void showMainMenu(ActionEvent event) {
        // Volvemos al menú principal con transición hacia atrás (baja)
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }

    // --- Clase interna para la gestión de cada "Tarjeta" de jugador ---
    private class Slot {
        private VBox card;
        private ComboBox<String> nameCombo;
        private Circle colorCircle;
        private String selectedColor;
        private GridPane pickerGrid;

        public Slot(boolean mandatory) {
            card = new VBox(15);
            card.getStyleClass().add("player-card");
            
            // Etiqueta de título
            Label title = new Label("P?"); 
            title.setStyle("-fx-font-weight: bold;");

            nameCombo = new ComboBox<>();
            nameCombo.getItems().addAll(allPlayerNames);
            nameCombo.setPromptText("Seleccionar...");
            nameCombo.setPrefWidth(160);
            
            // Personalizamos las celdas para deshabilitar nombres ya usados
            nameCombo.setCellFactory(lv -> new PlayerCell());
            nameCombo.setButtonCell(new PlayerCell());

            nameCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateAllComboCells();
            });

            // Color inicial (Azul para P1, Rojo para P2, primer color libre para otros)
            if (mandatory && slots.size() == 0) {
                selectedColor = "Azul";
            } else if (mandatory && slots.size() == 1) {
                selectedColor = "Rojo";
            } else {
                selectedColor = "Verde"; // Fallback
                for (String cName : COLORS) {
                    boolean taken = false;
                    for (Slot existingSlot : slots) {
                        if (cName.equals(existingSlot.getColor())) {
                            taken = true;
                            break;
                        }
                    }
                    if (!taken) {
                        selectedColor = cName;
                        break;
                    }
                }
            }
            colorCircle = new Circle(25, colorDesdeNombre(selectedColor));
            colorCircle.getStyleClass().add("color-circle");
            colorCircle.setCursor(Cursor.HAND);

            // Panel de selección de color (Grid emergente)
            ContextMenu colorMenu = new ContextMenu();
            pickerGrid = new GridPane();
            pickerGrid.setHgap(10);
            pickerGrid.setVgap(10);
            pickerGrid.setPadding(new Insets(10));
            pickerGrid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            
            CustomMenuItem customItem = new CustomMenuItem(pickerGrid);
            customItem.setHideOnClick(false);
            colorMenu.getItems().add(customItem);

            colorMenu.setOnShowing(e -> populateColorGrid(colorMenu));

            colorCircle.setOnMouseClicked(e -> {
                colorMenu.show(colorCircle, Side.BOTTOM, 0, 0);
            });

            HBox header = new HBox(title);
            header.setAlignment(Pos.CENTER);
            if (!mandatory) {
                Button removeBtn = new Button("×");
                removeBtn.getStyleClass().add("remove-slot-button");
                removeBtn.setOnAction(e -> removeThisSlot());
                util.UIUtils.applyHoverAnimation(removeBtn);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                header.getChildren().addAll(spacer, removeBtn);
            }

            card.getChildren().addAll(header, nameCombo, colorCircle);
        }

        private void removeThisSlot() {
            slots.remove(this);
            updateSlotsUI();
        }

        // Celda personalizada para bloquear jugadores ya elegidos
        private class PlayerCell extends ListCell<String> {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setDisable(false);
                } else {
                    setText(item);
                    boolean alreadySelected = false;
                    for (Slot s : slots) {
                        if (s != Slot.this && item.equals(s.getName())) {
                            alreadySelected = true;
                            break;
                        }
                    }
                    if (alreadySelected) {
                        setDisable(true);
                        setOpacity(0.4);
                        setStyle("-fx-text-fill: grey;");
                    } else {
                        setDisable(false);
                        setOpacity(1.0);
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        }

        public VBox getCard() { return card; }
        public String getName() { return nameCombo.getValue(); }
        public String getColor() { return selectedColor; }
        
        public void refreshCombo() {
            // Refrescamos la visualización sin disparar eventos de cambio
            var cellFactory = nameCombo.getCellFactory();
            nameCombo.setCellFactory(null);
            nameCombo.setCellFactory(cellFactory);
        }

        private void populateColorGrid(ContextMenu parent) {
            pickerGrid.getChildren().clear();
            int col = 0; int row = 0;
            for (String cName : COLORS) {
                Color color = colorDesdeNombre(cName);
                Circle c = new Circle(15, color);
                
                boolean taken = false;
                for (Slot s : slots) {
                    if (s != this && cName.equals(s.getColor())) {
                        taken = true; break;
                    }
                }

                if (taken) {
                    c.setOpacity(0.2);
                    c.setCursor(Cursor.DEFAULT);
                } else {
                    c.setCursor(Cursor.HAND);
                    c.setOpacity(1.0);
                    util.UIUtils.applyHoverAnimation(c);
                    c.setOnMouseClicked(e -> {
                        selectedColor = cName;
                        colorCircle.setFill(color);
                        parent.hide();
                    });
                }
                
                pickerGrid.add(c, col, row);
                col++;
                if (col > 2) { col = 0; row++; }
            }
        }

        private Color colorDesdeNombre(String nombre) {
            switch (nombre.toLowerCase()) {
                case "rojo":     return Color.RED;
                case "azul":     return Color.BLUE;
                case "verde":    return Color.GREEN;
                case "amarillo": return Color.YELLOW;
                case "naranja":  return Color.ORANGE;
                case "morado":   return Color.PURPLE;
                case "rosa":     return Color.PINK;
                default:         return Color.GRAY;
            }
        }
    }
}
