package controlador;

import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import modelo.Jugador;
import modelo.CPU;
import datos.BBDD;

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
        allPlayerNames = db.obtenerTodosLosJugadores();
        setupInitialSlots();
    }

    private void setupInitialSlots() {
        playerSlotsContainer.getChildren().clear();
        slots.clear();
        
        // At least 2 players are mandatory
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
            // Re-index title
            Label title = (Label) ((HBox) s.getCard().getChildren().get(0)).getChildren().get(0);
            title.setText("P" + (i + 1));
            
            playerSlotsContainer.getChildren().add(s.getCard());
        }

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
            configured.add(new Jugador(-1, name, s.getColor()));
        }
        
        if (focaCheckbox.isSelected()) {
            configured.add(new CPU(-1, "Foca Loca"));
        }

        if (configured.size() < 2) {
            errorLabel.setText("Mínimo 2 jugadores para empezar");
            return;
        }

        GameContext.getInstance().setConfiguredPlayers(configured);
        NavigationController.navigateTo(event, "SeedSelectionView.fxml");
    }

    @FXML
    private void showMainMenu(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml");
    }

    // --- Inner class for Slot management ---
    private class Slot {
        private VBox card;
        private ComboBox<String> nameCombo;
        private Circle colorCircle;
        private String selectedColor;
        private GridPane pickerGrid;
        private boolean active = true;

        public Slot(boolean mandatory) {
            card = new VBox(15);
            card.getStyleClass().add("player-card");
            
            // Title will be updated in updateSlotsUI
            Label title = new Label("P?"); 
            title.setStyle("-fx-font-weight: bold;");

            nameCombo = new ComboBox<>();
            nameCombo.getItems().addAll(allPlayerNames);
            nameCombo.setEditable(false); // Restricted to dropdown
            nameCombo.setPromptText("Seleccionar...");
            nameCombo.setPrefWidth(160);
            
            // Custom CellFactory to grey out selected players
            nameCombo.setCellFactory(lv -> new PlayerCell());
            nameCombo.setButtonCell(new PlayerCell());

            // Listener to refresh all slots when this one changes
            nameCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                updateAllComboCells();
            });

            selectedColor = mandatory ? (slots.size() == 0 ? "Azul" : "Rojo") : "Verde";
            colorCircle = new Circle(25, colorDesdeNombre(selectedColor));
            colorCircle.getStyleClass().add("color-circle");
            colorCircle.setCursor(Cursor.HAND);

            // Context Menu for color selection
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

            HBox header = new PosBox(title);
            header.setAlignment(Pos.CENTER);
            if (!mandatory) {
                Button removeBtn = new Button("×");
                removeBtn.getStyleClass().add("remove-slot-button"); // Uses specialized CSS
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

        // Custom Cell to handle greying out
        private class PlayerCell extends ListCell<String> {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setDisable(false);
                    setOpacity(1.0);
                } else {
                    setText(item);
                    boolean alreadySelected = false;
                    // Check if this item is selected by OTHER slots
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
                        setStyle("-fx-text-fill: black;"); // Normal color
                    }
                }
            }
        }

        // Auxiliary class to avoid FXML issues with HBox in some contexts
        private class PosBox extends HBox {
            public PosBox(Node... nodes) {
                super(nodes);
            }
        }

        public VBox getCard() { return card; }
        public String getName() { return nameCombo.getValue(); }
        public String getColor() { return selectedColor; }
        public boolean isActive() { return active; }
        
        public void refreshCombo() {
            // Trick to refresh cells without resetting items (to avoid selection events)
            javafx.util.Callback<ListView<String>, ListCell<String>> cellFactory = nameCombo.getCellFactory();
            nameCombo.setCellFactory(null);
            nameCombo.setCellFactory(cellFactory);
        }

        private void populateColorGrid(ContextMenu parent) {
            pickerGrid.getChildren().clear();
            int col = 0;
            int row = 0;
            for (String cName : COLORS) {
                Color color = colorDesdeNombre(cName);
                Circle c = new Circle(15, color);
                
                // Mutual Exclusion Logic
                boolean taken = false;
                for (Slot s : slots) {
                    if (s != this && cName.equals(s.getColor())) {
                        taken = true;
                        break;
                    }
                }

                if (taken) {
                    c.setOpacity(0.3);
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
                if (col > 2) {
                    col = 0; row++;
                }
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
