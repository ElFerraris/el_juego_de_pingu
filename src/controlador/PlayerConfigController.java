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
import javafx.animation.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import modelo.Jugador;
import modelo.Foca;
import modelo.Pinguino;
import datos.BBDD;

/**
 * PlayerConfigController
 * 
 * Gestiona la configuración de los jugadores antes de empezar una partida.
 */
public class PlayerConfigController {

    @FXML private HBox playerSlotsContainer;
    @FXML private Label errorLabel;

    private List<Slot> slots = new ArrayList<>();
    private List<String> allPlayerNames = new ArrayList<>();
    private static final int MAX_PLAYERS = 4;
    private static final String[] COLORS = {"Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"};
    private BBDD db = new BBDD();

    @FXML
    public void initialize() {
        try {
            // Carga síncrona de jugadores
            List<String> names = db.obtenerTodosLosJugadores();
            allPlayerNames = names;
            setupFixedSlots();
        } catch (Exception e) {
            System.err.println("Error cargando jugadores: " + e.getMessage());
        }
    }

    private void setupFixedSlots() {
        playerSlotsContainer.getChildren().clear();
        slots.clear();
        
        for (int i = 0; i < MAX_PLAYERS; i++) {
            Slot slot = new Slot(i);
            slots.add(slot);
            Node cardNode = slot.getRoot();
            cardNode.setOpacity(0);
            cardNode.setScaleX(0.8);
            cardNode.setScaleY(0.8);
            playerSlotsContainer.getChildren().add(cardNode);
            
            // Animación de entrada escalonada
            FadeTransition ft = new FadeTransition(Duration.millis(500), cardNode);
            ft.setToValue(1);
            ft.setDelay(Duration.millis(i * 150));
            
            ScaleTransition st = new ScaleTransition(Duration.millis(500), cardNode);
            st.setToX(1);
            st.setToY(1);
            st.setDelay(Duration.millis(i * 150));
            st.setInterpolator(Interpolator.EASE_OUT);
            
            new ParallelTransition(ft, st).play();
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
            if (s.getType() == SlotType.NONE) continue;

            if (s.getType() == SlotType.PLAYER) {
                String name = s.getName();
                if (name == null || name.trim().isEmpty()) {
                    errorLabel.setText("Todos los jugadores seleccionados deben tener nombre");
                    return;
                }
                configured.add(new Pinguino(-1, name, s.getColor()));
            } else if (s.getType() == SlotType.FOCA) {
                configured.add(new Foca(-1, "Foca Loca " + (configured.size() + 1)));
            }
        }
        
        if (configured.size() < 2) {
            errorLabel.setText("Mínimo 2 jugadores (incluyendo CPU) para empezar");
            return;
        }

        GameContext.getInstance().setConfiguredPlayers(configured);
        NavigationController.navigateTo(event, "SeedSelectionView.fxml", NavigationController.Direction.FORWARD);
    }

    @FXML
    private void showMainMenu(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.BACKWARD);
    }

    private enum SlotType { PLAYER, FOCA, NONE }

    private class Slot {
        private StackPane root;
        private VBox card;
        private Region flashOverlay;
        private Label titleLabel;
        private ComboBox<SlotType> typeCombo;
        private ComboBox<String> nameCombo;
        private Label fixedNameLabel;
        private Circle colorCircle;
        private String selectedColor;
        private GridPane pickerGrid;
        private int index;

        public Slot(int index) {
            this.index = index;
            root = new StackPane();
            
            card = new VBox(15);
            card.getStyleClass().add("player-card");
            
            flashOverlay = new Region();
            flashOverlay.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
            flashOverlay.setOpacity(0);
            flashOverlay.setMouseTransparent(true);
            
            root.getChildren().addAll(card, flashOverlay);
            
            titleLabel = new Label("P" + (index + 1));
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");

            if (index == 0) {
                setupAsCurrentUser();
            } else {
                setupWithDropdowns();
            }

            updateCardStyle();
        }

        private void setupAsCurrentUser() {
            Jugador current = GameContext.getInstance().getCurrentUser();
            String name = (current != null) ? current.getNombre() : "Jugador 1";
            
            nameCombo = new ComboBox<>();
            nameCombo.getItems().add(name);
            nameCombo.setValue(name);
            nameCombo.setVisible(false);
            nameCombo.setManaged(false);

            fixedNameLabel = new Label(name.toUpperCase());
            fixedNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            selectedColor = "Azul";
            setupColorPicker();

            card.getChildren().addAll(titleLabel, fixedNameLabel, colorCircle);
        }

        private void setupWithDropdowns() {
            typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll(SlotType.PLAYER, SlotType.FOCA, SlotType.NONE);
            typeCombo.setValue(index == 1 ? SlotType.PLAYER : SlotType.NONE);
            typeCombo.setPrefWidth(160);

            nameCombo = new ComboBox<>();
            nameCombo.getItems().addAll(allPlayerNames);
            nameCombo.setPromptText("Elegir jugador...");
            nameCombo.setPrefWidth(160);
            nameCombo.setCellFactory(lv -> new PlayerCell());
            nameCombo.setButtonCell(new PlayerCell());
            nameCombo.valueProperty().addListener((obs, old, nw) -> updateAllComboCells());

            fixedNameLabel = new Label("FOCA LOCA");
            fixedNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #546e7a;");
            fixedNameLabel.setVisible(false);
            fixedNameLabel.setManaged(false);

            selectedColor = (index == 1) ? "Rojo" : "Verde";
            setupColorPicker();

            typeCombo.valueProperty().addListener((obs, old, nw) -> {
                flashCard();
                updateVisibility();
                updateCardStyle();
            });

            card.getChildren().addAll(titleLabel, typeCombo, nameCombo, fixedNameLabel, colorCircle);
            updateVisibility();
        }

        private void setupColorPicker() {
            colorCircle = new Circle(30, colorDesdeNombre(selectedColor));
            colorCircle.getStyleClass().add("color-circle");
            colorCircle.setCursor(Cursor.HAND);
            colorCircle.setStroke(Color.WHITE);
            colorCircle.setStrokeWidth(3);
            
            javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
            glow.setColor(colorDesdeNombre(selectedColor));
            glow.setRadius(15);
            colorCircle.setEffect(glow);

            ContextMenu colorMenu = new ContextMenu();
            pickerGrid = new GridPane();
            pickerGrid.setHgap(10); pickerGrid.setVgap(10);
            pickerGrid.setPadding(new Insets(10));
            pickerGrid.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            
            CustomMenuItem customItem = new CustomMenuItem(pickerGrid);
            customItem.setHideOnClick(false);
            colorMenu.getItems().add(customItem);
            colorMenu.setOnShowing(e -> populateColorGrid(colorMenu));

            colorCircle.setOnMouseClicked(e -> colorMenu.show(colorCircle, Side.BOTTOM, 0, 0));
        }

        private void flashCard() {
            FadeTransition ft = new FadeTransition(Duration.millis(300), flashOverlay);
            ft.setFromValue(0.8);
            ft.setToValue(0);
            ft.play();
        }

        private void updateVisibility() {
            SlotType type = getType();
            if (nameCombo != null) {
                nameCombo.setVisible(type == SlotType.PLAYER);
                nameCombo.setManaged(type == SlotType.PLAYER);
            }
            if (fixedNameLabel != null) {
                fixedNameLabel.setVisible(type == SlotType.FOCA);
                fixedNameLabel.setManaged(type == SlotType.FOCA);
            }
            if (colorCircle != null) {
                colorCircle.setVisible(type == SlotType.PLAYER);
                colorCircle.setManaged(type == SlotType.PLAYER);
            }
        }

        private void updateCardStyle() {
            card.getStyleClass().removeAll("player-card-empty", "player-card-foca");
            SlotType type = getType();
            
            if (type == SlotType.NONE) {
                card.getStyleClass().add("player-card-empty");
                card.setStyle("-fx-border-color: #90a4ae;");
            } else if (type == SlotType.FOCA) {
                card.getStyleClass().add("player-card-foca");
                card.setStyle("-fx-border-color: #546e7a;");
            } else {
                Color c = colorDesdeNombre(selectedColor);
                String hex = String.format("#%02x%02x%02x", 
                    (int)(c.getRed() * 255), 
                    (int)(c.getGreen() * 255), 
                    (int)(c.getBlue() * 255));
                card.setStyle("-fx-border-color: " + hex + ";");
                if (colorCircle != null) {
                   ((javafx.scene.effect.DropShadow)colorCircle.getEffect()).setColor(c);
                }
            }
        }

        private class PlayerCell extends ListCell<String> {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    boolean taken = false;
                    for (Slot s : slots) {
                        if (s != Slot.this && item.equals(s.getName())) { taken = true; break; }
                    }
                    if (taken) {
                        setDisable(true); setOpacity(0.4);
                    } else {
                        setDisable(false); setOpacity(1.0);
                    }
                }
            }
        }

        public Node getRoot() { return root; }
        public SlotType getType() { return (typeCombo == null) ? SlotType.PLAYER : typeCombo.getValue(); }
        public String getName() { return (nameCombo != null) ? nameCombo.getValue() : null; }
        public String getColor() { return selectedColor; }
        
        public void refreshCombo() {
            if (nameCombo == null) return;
            var factory = nameCombo.getCellFactory();
            nameCombo.setCellFactory(null);
            nameCombo.setCellFactory(factory);
        }

        private void populateColorGrid(ContextMenu parent) {
            pickerGrid.getChildren().clear();
            int col = 0; int row = 0;
            for (String cName : COLORS) {
                Color color = colorDesdeNombre(cName);
                Circle c = new Circle(15, color);
                boolean taken = false;
                for (Slot s : slots) {
                    if (s != this && s.getType() == SlotType.PLAYER && cName.equals(s.getColor())) { taken = true; break; }
                }

                if (taken) {
                    c.setOpacity(0.2); c.setCursor(Cursor.DEFAULT);
                } else {
                    c.setCursor(Cursor.HAND);
                    c.setOnMouseClicked(e -> {
                        flashCard();
                        selectedColor = cName;
                        colorCircle.setFill(color);
                        updateCardStyle();
                        parent.hide();
                    });
                }
                pickerGrid.add(c, col, row);
                col++; if (col > 2) { col = 0; row++; }
            }
        }

        private Color colorDesdeNombre(String nombre) {
            switch (nombre.toLowerCase()) {
                case "rojo":     return Color.web("#f44336");
                case "azul":     return Color.web("#2196f3");
                case "verde":    return Color.web("#4caf50");
                case "amarillo": return Color.web("#ffeb3b");
                case "naranja":  return Color.web("#ff9800");
                case "morado":   return Color.web("#9c27b0");
                case "rosa":     return Color.web("#e91e63");
                default:         return Color.GRAY;
            }
        }
    }
}
