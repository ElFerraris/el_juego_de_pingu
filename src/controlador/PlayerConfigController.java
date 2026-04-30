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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.List;
import modelo.Jugador;
import modelo.Foca;
import modelo.Pinguino;
import datos.BBDD;

/**
 * Controlador para la vista de configuración de jugadores.
 * 
 * <p>
 * Esta clase gestiona la selección de los participantes de una partida (mínimo
 * 2,
 * máximo 4). Permite asignar a cada hueco un jugador humano registrado en la
 * base de
 * datos o un bot (Foca Loca), y personalizar el color de su ficha.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class PlayerConfigController {

    @FXML
    private HBox playerSlotsContainer;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox focaCard;
    @FXML
    private ImageView focaIcon;
    @FXML
    private CheckBox focaEnabledCheck;

    private List<Slot> slots = new ArrayList<>();
    private List<String> allPlayerNames = new ArrayList<>();
    private static final int MAX_PLAYERS = 4;
    private static final String[] COLORS = { "Rojo", "Azul", "Verde", "Amarillo", "Naranja", "Morado", "Rosa" };
    private BBDD db = new BBDD();

    /**
     * Inicializa la vista de configuración.
     * 
     * <p>
     * Carga de forma síncrona los nombres de todos los jugadores registrados
     * en la base de datos y configura los huecos de la interfaz.
     * </p>
     */
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

    /**
     * Prepara y renderiza visualmente los 4 huecos de jugador en pantalla.
     * <p>
     * Aplica animaciones de escala y opacidad escalonadas (efecto cascada)
     * al mostrar las tarjetas por primera vez.
     * </p>
     */
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
        updateFocaStyle();
    }

    /**
     * Alterna el estado de inclusión de la Foca en la partida.
     */
    @FXML
    private void toggleFoca() {
        util.SoundManager.playConfirm();
        focaEnabledCheck.setSelected(!focaEnabledCheck.isSelected());
        updateFocaStyle();
    }

    private void updateFocaStyle() {
        if (focaEnabledCheck.isSelected()) {
            focaCard.getStyleClass().remove("player-card-empty");
            focaCard.setOpacity(1.0);
            javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
            glow.setColor(Color.web("#546e7a"));
            glow.setRadius(20);
            focaIcon.setEffect(glow);
        } else {
            if (!focaCard.getStyleClass().contains("player-card-empty")) {
                focaCard.getStyleClass().add("player-card-empty");
            }
            focaCard.setOpacity(0.6);
            focaIcon.setEffect(null);
        }
    }

    private boolean isUpdatingAll = false;

    /**
     * Refresca el contenido visual de todos los ComboBox de la interfaz.
     * <p>
     * Utiliza la bandera isUpdatingAll para evitar llamadas recursivas infinitas
     * debido a los listeners en los properties de JavaFX.
     * </p>
     */
    private void updateAllComboCells() {
        if (!isUpdatingAll) {
            isUpdatingAll = true;
            try {
                for (Slot s : slots) {
                    if (s.isEnabled()) {
                        s.refreshCombo();
                    }
                }
            } finally {
                isUpdatingAll = false;
            }
        }
    }

    /**
     * Valida la configuración actual y avanza a la selección de semilla (tablero).
     * 
     * <p>
     * Comprueba que haya al menos dos participantes, que todos los humanos
     * tengan nombre asignado y guarda la lista de jugadores en el GameContext.
     * </p>
     * 
     * @param event El evento del botón "Continuar".
     */
    @FXML
    private void showSeedSelection(ActionEvent event) {
        List<Jugador> configured = new ArrayList<>();
        boolean errorEncontrado = false;

        // 1. Añadir Jugadores Humanos
        for (Slot s : slots) {
            if (!errorEncontrado && s.isEnabled()) {
                String name = s.getName();
                if (name == null || name.trim().isEmpty()) {
                    errorLabel.setText("Todos los jugadores seleccionados deben tener nombre");
                    errorEncontrado = true;
                } else {
                    configured.add(new Pinguino(-1, name, s.getColor()));
                }
            }
        }

        // 2. Añadir Foca si está activada
        if (!errorEncontrado && focaEnabledCheck.isSelected()) {
            configured.add(new Foca(-1, "FOCA LOCA"));
        }

        if (!errorEncontrado) {
            if (configured.size() < 2) {
                errorLabel.setText("Mínimo 2 participantes para empezar");
            } else {
                GameContext.getInstance().setConfiguredPlayers(configured);
                NavigationController.navigateTo(event, "SeedSelectionView.fxml",
                        NavigationController.Direction.FORWARD);
            }
        }
    }

    /**
     * Retrocede al Menú Principal.
     * 
     * @param event El evento del botón "Atrás".
     */
    @FXML
    private void showMainMenu(ActionEvent event) {
        NavigationController.navigateTo(event, "MainMenuView.fxml", NavigationController.Direction.RIGHT);
    }



    /**
     * Representa un hueco de jugador en la interfaz.
     * 
     * <p>
     * Gestiona la lógica visual y de selección para un único participante,
     * incluyendo su tipo (Humano/CPU), su nombre y el color de su ficha.
     * </p>
     */
    private class Slot {
        private StackPane root;
        private VBox card;
        private Region flashOverlay;
        private Label titleLabel;
        private CheckBox slotEnabledCheck;
        private ComboBox<String> nameCombo;
        private Label fixedNameLabel;
        private ImageView colorIcon;
        private String selectedColor;
        private GridPane pickerGrid;
        private int index;

        /**
         * Constructor del hueco de jugador.
         * 
         * @param index Índice del hueco (0 para el jugador actual, 1-3 para el resto).
         */
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

        /**
         * Configura el hueco inicial (índice 0) obligatoriamente para el jugador actual
         * logueado.
         */
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

            card.getChildren().addAll(titleLabel, fixedNameLabel, colorIcon);
        }

        /**
         * Configura los huecos restantes (1 al 2) con un CheckBox para activarlos.
         */
        private void setupWithDropdowns() {
            slotEnabledCheck = new CheckBox("ACTIVAR P" + (index + 1));
            slotEnabledCheck.getStyleClass().add("premium-checkbox");
            slotEnabledCheck.setSelected(index == 1); // El P2 activado por defecto

            nameCombo = new ComboBox<>();
            nameCombo.getItems().addAll(allPlayerNames);
            nameCombo.setPromptText("Elegir jugador...");
            nameCombo.setPrefWidth(160);
            nameCombo.setCellFactory(lv -> new PlayerCell());
            nameCombo.setButtonCell(new PlayerCell());
            nameCombo.valueProperty().addListener((obs, old, nw) -> updateAllComboCells());

            selectedColor = (index == 1) ? "Rojo" : "Verde";
            setupColorPicker();

            slotEnabledCheck.selectedProperty().addListener((obs, old, nw) -> {
                flashCard();
                updateVisibility();
                updateCardStyle();
                updateAllComboCells();
            });

            card.getChildren().addAll(titleLabel, slotEnabledCheck, nameCombo, colorIcon);
            updateVisibility();
        }

        /**
         * Prepara el selector visual de colores (un menú emergente con iconos).
         */
        private void setupColorPicker() {
            colorIcon = new ImageView();
            colorIcon.setFitWidth(60);
            colorIcon.setFitHeight(60);
            colorIcon.setPreserveRatio(true);
            colorIcon.setCursor(Cursor.HAND);

            actualizarIcono();

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

            colorIcon.setOnMouseClicked(e -> {
                if (isEnabled()) {
                    colorMenu.show(colorIcon, Side.BOTTOM, 0, 0);
                }
            });
        }

        /**
         * Actualiza el icono de pingüino mostrado según el color seleccionado.
         */
        private void actualizarIcono() {
            try {
                String colorName = selectedColor.toLowerCase();
                Image img = new Image(
                        getClass().getResourceAsStream("/assets/ico_jugadores/ico_" + colorName + ".png"));
                colorIcon.setImage(img);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono");
            }

            javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
            glow.setColor(colorDesdeNombre(selectedColor));
            glow.setRadius(15);
            colorIcon.setEffect(glow);
        }

        /**
         * Realiza una pequeña animación de destello visual sobre la tarjeta al cambiar
         * un ajuste.
         */
        private void flashCard() {
            FadeTransition ft = new FadeTransition(Duration.millis(300), flashOverlay);
            ft.setFromValue(0.8);
            ft.setToValue(0);
            ft.play();
        }

        /**
         * Muestra u oculta elementos de la interfaz (combobox de nombres, etiqueta
         * fija, icono)
         * dependiendo de si el hueco es para un Jugador, una Foca o está Vacío.
         */
        private void updateVisibility() {
            boolean enabled = isEnabled();
            if (nameCombo != null) {
                nameCombo.setVisible(enabled);
                nameCombo.setManaged(enabled);
            }
            if (colorIcon != null) {
                colorIcon.setVisible(enabled);
                colorIcon.setManaged(enabled);
                if (enabled) actualizarIcono();
            }
        }

        /**
         * Actualiza el color del borde y de las sombras de la tarjeta según el tipo
         * de jugador y su color elegido.
         */
        private void updateCardStyle() {
            card.getStyleClass().removeAll("player-card-empty", "player-card-foca");
            boolean enabled = isEnabled();

            if (!enabled) {
                card.getStyleClass().add("player-card-empty");
                card.setStyle("-fx-border-color: #90a4ae;");
            } else {
                Color c = colorDesdeNombre(selectedColor);
                String hex = String.format("#%02x%02x%02x",
                        (int) (c.getRed() * 255),
                        (int) (c.getGreen() * 255),
                        (int) (c.getBlue() * 255));
                card.setStyle("-fx-border-color: " + hex + ";");
                if (colorIcon != null) {
                    ((javafx.scene.effect.DropShadow) colorIcon.getEffect()).setColor(c);
                }
            }
        }

        /**
         * Celda personalizada para la lista desplegable de nombres de jugadores.
         * Deshabilita los nombres que ya han sido seleccionados en otros huecos.
         */
        private class PlayerCell extends ListCell<String> {
            /**
             * Actualiza visualmente el elemento de la celda de la lista desplegable.
             * 
             * @param item  El nombre del jugador a mostrar en la celda.
             * @param empty Verdadero si la celda debe mostarse vacía, falso de lo
             *              contrario.
             */
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    boolean taken = false;
                    for (Slot s : slots) {
                        if (s != Slot.this && item.equals(s.getName())) {
                            taken = true;
                            break;
                        }
                    }
                    if (taken) {
                        setDisable(true);
                        setOpacity(0.4);
                    } else {
                        setDisable(false);
                        setOpacity(1.0);
                    }
                }
            }
        }

        public Node getRoot() {
            return root;
        }

        public boolean isEnabled() {
            return (slotEnabledCheck == null) ? true : slotEnabledCheck.isSelected();
        }

        public String getName() {
            return (nameCombo != null) ? nameCombo.getValue() : null;
        }

        public String getColor() {
            return selectedColor;
        }

        /**
         * Refresca el combobox de nombres para forzar a la interfaz a re-evaluar
         * qué nombres están disponibles y cuáles no.
         */
        public void refreshCombo() {
            if (nameCombo != null) {
                var factory = nameCombo.getCellFactory();
                nameCombo.setCellFactory(null);
                nameCombo.setCellFactory(factory);
            }
        }

        /**
         * Llena la cuadrícula del selector de color desplegable.
         * Deshabilita los colores que ya están siendo usados por otros jugadores.
         * 
         * @param parent El menú contextual que contiene esta cuadrícula.
         */
        private void populateColorGrid(ContextMenu parent) {
            pickerGrid.getChildren().clear();
            int col = 0;
            int row = 0;
            for (String cName : COLORS) {
                ImageView c = new ImageView();
                try {
                    String colorName = cName.toLowerCase();
                    Image img = new Image(
                            getClass().getResourceAsStream("/assets/ico_jugadores/ico_" + colorName + ".png"));
                    c.setImage(img);
                } catch (Exception e) {
                }
                c.setFitWidth(30);
                c.setFitHeight(30);
                c.setPreserveRatio(true);

                boolean taken = false;
                for (Slot s : slots) {
                    if (s != this && s.isEnabled() && cName.equals(s.getColor())) {
                        taken = true;
                        break;
                    }
                }

                if (taken) {
                    c.setOpacity(0.2);
                    c.setCursor(Cursor.DEFAULT);
                } else {
                    c.setCursor(Cursor.HAND);
                    c.setOnMouseClicked(e -> {
                        flashCard();
                        selectedColor = cName;
                        actualizarIcono();
                        updateCardStyle();
                        parent.hide();
                    });
                }
                pickerGrid.add(c, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        }

        /**
         * Obtiene un objeto {@code Color} estándar de JavaFX a partir de su nombre en
         * español.
         * 
         * @param nombre El nombre del color (ej. "Rojo").
         * @return El color correspondiente o gris si no coincide.
         */
        private Color colorDesdeNombre(String nombre) {
            switch (nombre.toLowerCase()) {
                case "rojo":
                    return Color.web("#f44336");
                case "azul":
                    return Color.web("#2196f3");
                case "verde":
                    return Color.web("#4caf50");
                case "amarillo":
                    return Color.web("#ffeb3b");
                case "naranja":
                    return Color.web("#ff9800");
                case "morado":
                    return Color.web("#9c27b0");
                case "rosa":
                    return Color.web("#e91e63");
                default:
                    return Color.GRAY;
            }
        }
    }
}
