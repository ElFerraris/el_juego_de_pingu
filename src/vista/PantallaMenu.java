package vista;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.Cursor;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.PopupWindow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Map;

import datos.BBDD;
import controlador.Juego;
import modelo.Jugador;
import modelo.CPU;
import modelo.Tablero;

public class PantallaMenu {

    @FXML private VBox splashContainer;
    @FXML private VBox loginContainer;
    @FXML private VBox createPlayerContainer;
    @FXML private VBox mainMenuContainer;
    @FXML private VBox playerConfigContainer;
    @FXML private VBox loadGameContainer;
    @FXML private VBox seedSelectionContainer;

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private TextField newUserNameField;
    @FXML private PasswordField newUserPassField;
    @FXML private Text welcomeText;
    @FXML private Label playerConfigErrorLabel;
    @FXML private ListView<String> savedGamesListView;
    @FXML private TextField seedField;
    @FXML private Label seedErrorLabel;
    @FXML private Label loginErrorLabel;
    @FXML private Label fullScreenHint;
    @FXML private CheckBox focaCheckbox;
    @FXML private HBox playerSlotsContainer;

    private BBDD db = new BBDD();
    private String currentUserName;
    private int currentUserId = -1;
    private ArrayList<Jugador> configuredPlayers = new ArrayList<>();
    
    // Internal state for slots
    private static class Slot {
        String name;
        int id;
        String color;
        boolean active;
    }
    private Slot[] slots = new Slot[4];

    // Humans cannot pick Gris (reserved for CPU)
    private static final String[] COLORES_VALIDOS = {"Azul", "Rojo", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"};
    
    private static final java.util.Map<String, String> COLOR_MAP = java.util.Map.of(
        "Azul", "#2196f3",
        "Rojo", "#f44336",
        "Verde", "#4caf50",
        "Amarillo", "#ffeb3b",
        "Naranja", "#ff9800",
        "Morado", "#9c27b0",
        "Rosa", "#e91e63",
        "Gris", "#9e9e9e"
    );

    @FXML
    public void initialize() {
        System.out.println("PantallaMenu: Inicializando...");
        hideAllContainers();
        splashContainer.setVisible(true);
        
        // Cargar fondo del splash programáticamente para mayor fiabilidad
        try {
            String imagePath = "imagenes/TITULO%20CON%20FONDO.png";
            // Intentar cargar como recurso del sistema (funciona si está en el classpath o relativo al WD)
            java.io.File file = new java.io.File("imagenes/TITULO CON FONDO.png");
            if (file.exists()) {
                String url = file.toURI().toString();
                javafx.scene.image.Image img = new javafx.scene.image.Image(url);
                javafx.scene.layout.BackgroundImage bImg = new javafx.scene.layout.BackgroundImage(
                    img,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundPosition.CENTER,
                    new javafx.scene.layout.BackgroundSize(1.0, 1.0, true, true, false, true)
                );
                splashContainer.setBackground(new javafx.scene.layout.Background(bImg));
            }
        } catch (Exception e) {
            System.err.println("Error cargando imagen de fondo: " + e.getMessage());
        }

        // Nota: La fuente se carga globalmente en Main.java

        // Initialize slots
        for(int i=0; i<4; i++) {
            slots[i] = new Slot();
            slots[i].active = (i < 2); // First 2 are active by default
            slots[i].color = COLORES_VALIDOS[i % COLORES_VALIDOS.length];
        }

        // Seed field: only numbers, max 48 chars
        if (seedField != null) {
            seedField.setTextFormatter(new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("[0-9]*") && newText.length() <= 48) {
                    return change;
                }
                return null;
            }));
        }

        showFullScreenHint();
    }

    private void showFullScreenHint() {
        if (fullScreenHint == null) return;
        fullScreenHint.setVisible(true);
        fullScreenHint.setOpacity(1.0);
        
        FadeTransition fade = new FadeTransition(Duration.seconds(2), fullScreenHint);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1));
        fade.setOnFinished(e -> fullScreenHint.setVisible(false));
        fade.play();
    }

    private void renderPlayerSlots() {
        if (playerSlotsContainer == null) return;
        playerSlotsContainer.getChildren().clear();

        for (int i = 0; i < 4; i++) {
            if (slots[i].active) {
                playerSlotsContainer.getChildren().add(createPlayerCard(i));
            } else {
                // Only show "+" button for the first inactive slot
                playerSlotsContainer.getChildren().add(createAddButton(i));
                break; 
            }
        }
    }

    private Node createPlayerCard(int index) {
        VBox card = new VBox(15);
        card.getStyleClass().add("player-card");
        
        Label title = new Label("JUGADOR " + (index + 1));
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #90a4ae; -fx-font-size: 12px;");

        // Player Name Slot
        Node nameNode;
        if (index == 0) {
            // P1 is the logged in user
            slots[0].name = currentUserName;
            slots[0].id = currentUserId;
            nameNode = new Label(currentUserName);
            ((Label)nameNode).setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        } else {
            // P2, P3, P4 can be selection from DB
            ComboBox<String> nameCombo = new ComboBox<>();
            nameCombo.setPromptText("Seleccionar...");
            ObservableList<String> options = FXCollections.observableArrayList();
            ArrayList<String> all = db.obtenerTodosLosJugadores();
            for (String p : all) {
                boolean alreadyTaken = false;
                for(int j=0; j<index; j++) if(slots[j].active && p.equals(slots[j].name)) alreadyTaken = true;
                if (!alreadyTaken && !p.equals(currentUserName)) options.add(p);
            }
            nameCombo.setItems(options);
            if (slots[index].name != null) nameCombo.setValue(slots[index].name);
            
            nameCombo.setOnAction(e -> {
                slots[index].name = nameCombo.getValue();
                slots[index].id = db.obtenerIdJugador(null, slots[index].name);
            });
            nameNode = nameCombo;
        }

        // Color Selection (Circle + Arrow)
        HBox colorBox = new HBox(8);
        colorBox.setAlignment(Pos.CENTER);
        colorBox.setCursor(Cursor.HAND);

        Circle colorCircle = new Circle(22);
        colorCircle.setStroke(Color.WHITE);
        colorCircle.setStrokeWidth(3);
        updateCircleColor(colorCircle, slots[index].color);

        Label arrow = new Label("▼");
        arrow.setStyle("-fx-font-size: 10px; -fx-text-fill: #90a4ae;");

        colorBox.getChildren().addAll(colorCircle, arrow);
        colorBox.setOnMouseClicked(e -> showColorPicker(colorCircle, index));

        card.getChildren().addAll(title, nameNode, colorBox);

        // Remove button for P3 and P4
        if (index >= 2) {
            Button removeBtn = new Button("✕");
            removeBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c; -fx-background-radius: 50; -fx-font-size: 10px;");
            removeBtn.setOnAction(e -> {
                // Deactivate this slot and all subsequent ones to ensure contiguity
                for (int k = index; k < 4; k++) {
                    slots[k].active = false;
                    slots[k].name = null;
                }
                renderPlayerSlots();
            });
            card.getChildren().add(removeBtn);
        }

        return card;
    }

    private Node createAddButton(int index) {
        Button addBtn = new Button("+");
        addBtn.getStyleClass().add("add-player-button");
        addBtn.setPrefSize(180, 200);
        addBtn.setOnAction(e -> {
            slots[index].active = true;
            renderPlayerSlots();
        });
        
        VBox container = new VBox(10, addBtn, new Label("Añadir Jugador"));
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private void updateCircleColor(Circle circle, String colorName) {
        String hex = COLOR_MAP.getOrDefault(colorName, "#000000");
        circle.setFill(Color.web(hex));
    }

    private void showColorPicker(Circle targetCircle, int slotIndex) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        int col = 0;
        int row = 0;
        for (String colorName : COLORES_VALIDOS) {
            Circle c = new Circle(15);
            c.setFill(Color.web(COLOR_MAP.get(colorName)));
            c.setCursor(Cursor.HAND);
            c.setOnMouseClicked(ev -> {
                slots[slotIndex].color = colorName;
                updateCircleColor(targetCircle, colorName);
                ((PopupWindow)grid.getScene().getWindow()).hide();
            });

            grid.add(c, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }

        ContextMenu contextMenu = new ContextMenu();
        CustomMenuItem item = new CustomMenuItem(grid);
        item.setHideOnClick(false);
        contextMenu.getItems().add(item);
        contextMenu.show(targetCircle, Side.BOTTOM, 0, 0);
    }

    private void hideAllContainers() {
        VBox[] containers = {splashContainer, loginContainer, createPlayerContainer, 
                             mainMenuContainer, playerConfigContainer, loadGameContainer, 
                             seedSelectionContainer};
        for (VBox c : containers) {
            if (c != null) c.setVisible(false);
        }
    }

    @FXML
    private void handleStartSplash() {
        showLogin();
    }

    @FXML
    private void showLogin() {
        hideAllContainers();
        loginContainer.setVisible(true);
    }

    @FXML
    private void showCreatePlayer() {
        hideAllContainers();
        createPlayerContainer.setVisible(true);
    }

    @FXML
    private void handleCreatePlayer() {
        String name = newUserNameField.getText();
        String pass = newUserPassField.getText();
        if (!name.isEmpty() && !pass.isEmpty()) {
            if (db.registrarNuevoJugador(name, pass)) {
                currentUserName = name;
                currentUserId = db.obtenerIdJugador(null, name);
                showMainMenu();
            }
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText();
        String password = passField.getText();
        if (!username.isEmpty()) {
            int id = db.verificarCredenciales(username, password);
            if (id != -1) {
                currentUserId = id;
                currentUserName = username;
                loginErrorLabel.setVisible(false);
                showMainMenu();
            } else {
                loginErrorLabel.setText("Usuario o contraseña incorrectos.");
                loginErrorLabel.setVisible(true);
            }
        } else {
            loginErrorLabel.setText("Por favor, introduce tu usuario.");
            loginErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void showMainMenu() {
        hideAllContainers();
        welcomeText.setText("Usuario: " + currentUserName);
        mainMenuContainer.setVisible(true);
    }

    @FXML
    private void showPlayerConfig() {
        hideAllContainers();
        renderPlayerSlots();
        playerConfigContainer.setVisible(true);
    }

    @FXML
    private void showLoadGame() {
        hideAllContainers();
        loadGameContainer.setVisible(true);
        refreshSavedGames();
    }

    private void refreshSavedGames() {
        ArrayList<String> games = db.obtenerPartidasGuardadas(currentUserName);
        savedGamesListView.getItems().clear();
        savedGamesListView.getItems().addAll(games);
    }

    @FXML
    private void handleConfirmLoadGame(ActionEvent event) {
        String selected = savedGamesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // "Partida #ID (Seed: ...)"
                String idStr = selected.substring(selected.indexOf("#") + 1).split(" ")[0];
                int id = Integer.parseInt(idStr);
                cargarPantallaJuego(event, null, id);
            } catch (Exception e) {
                System.err.println("Error parsing ID: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showSeedSelection() {
        ArrayList<Jugador> players = new ArrayList<>();
        playerConfigErrorLabel.setText("");

        for (int i = 0; i < 4; i++) {
            if (slots[i].active) {
                if (slots[i].name == null || slots[i].name.isEmpty()) {
                    playerConfigErrorLabel.setText("Jugador " + (i+1) + " debe tener un usuario.");
                    return;
                }
                players.add(new Jugador(slots[i].id, slots[i].name, slots[i].color));
            }
        }

        if (players.size() < 2) {
            playerConfigErrorLabel.setText("Se requieren al menos 2 jugadores.");
            return;
        }

        // Foca CPU is Optional
        if (focaCheckbox.isSelected()) {
            players.add(new CPU(players.size() + 1, "Foca Loca"));
        }

        // All good, prepare the game
        configuredPlayers = players;
        
        hideAllContainers();
        seedSelectionContainer.setVisible(true);
        seedErrorLabel.setText("");
        seedField.setText("");
    }


    @FXML
    private void handleRandomSeed(ActionEvent event) {
        cargarPantallaJuego(event, null, -1);
    }

    @FXML
    private void handleManualSeed(ActionEvent event) {
        String seed = seedField.getText();
        if (seed.length() == 48) {
            // Check business rules (min 2 holes, 2 sleds)
            Tablero dummy = new Tablero();
            if (dummy.validarSeed(seed)) {
                cargarPantallaJuego(event, seed, -1);
            } else {
                seedErrorLabel.setText("Seed inválida: se requieren al menos 2 agujeros (2) y 2 trineos (3).");
            }
        } else {
            seedErrorLabel.setText("La seed debe tener exactamente 48 números.");
        }
    }

    @FXML
    private void showMainMenuFromConfig() {
        showMainMenu();
    }

    private void cargarPantallaJuego(ActionEvent event, String seed, int idPartida) {
        System.out.println("► Intentando cargar pantalla de juego. ID Partida: " + idPartida);
        try {
            java.net.URL fxmlUrl = getClass().getResource("/PantallaJuego.fxml");
            if (fxmlUrl == null) {
                System.err.println("► ERROR: No se ha encontrado PantallaJuego.fxml en el classpath.");
                return;
            }
            System.out.println("► Cargando FXML desde: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            PantallaJuego controller = loader.getController();
            System.out.println("► Controlador de juego obtenido: " + controller);
            Juego nuevoJuego = new Juego();
            if (idPartida != -1) {
                boolean exito = db.cargarDatosPartida(idPartida, nuevoJuego);
                if (!exito) {
                    System.err.println("Error al cargar partida desde DB");
                    return;
                }
            } else {
                if (seed != null) {
                    nuevoJuego.getTablero().introducirSeed(seed);
                } else {
                    nuevoJuego.getTablero().generarSeedAleatoria();
                }
                // Register configured players
                for (Jugador j : configuredPlayers) {
                    nuevoJuego.agregarJugador(j);
                }
                nuevoJuego.iniciarPartida();
            }
            
            controller.setJuego(nuevoJuego);
            controller.inicializarActual();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
            stage.show();
            System.out.println("► Pantalla de juego mostrada.");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al Cargar Partida");
            alert.setHeaderText("No se pudo iniciar el juego.");
            alert.setContentText("Detalle: " + e.toString());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleQuitGame() {
        System.exit(0);
    }
}