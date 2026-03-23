package vista;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import datos.BBDD;
import controlador.Juego;
import modelo.Jugador;
import modelo.CPU;
import modelo.Tablero;
import java.util.ArrayList;

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
    @FXML private Label p1NameLabel;
    @FXML private ComboBox<String> p2Combo;
    @FXML private ComboBox<String> p3Combo;
    @FXML private ComboBox<String> p4Combo;
    @FXML private ComboBox<String> p1ColorCombo;
    @FXML private ComboBox<String> p2ColorCombo;
    @FXML private ComboBox<String> p3ColorCombo;
    @FXML private ComboBox<String> p4ColorCombo;
    @FXML private Label playerConfigErrorLabel;
    @FXML private ListView<String> savedGamesListView;
    @FXML private TextField seedField;
    @FXML private Label seedErrorLabel;
    @FXML private Label loginErrorLabel;
    @FXML private Label fullScreenHint;
    @FXML private CheckBox focaCheckbox;

    private BBDD db = new BBDD();
    private String currentUserName;
    private int currentUserId = -1;
    private ArrayList<Jugador> configuredPlayers = new ArrayList<>();

    // The 7 valid color names defined in Jugador.validarColor()
    private static final String[] COLORES_VALIDOS = {"Azul", "Rojo", "Verde", "Amarillo", "Naranja", "Morado", "Rosa"};

    @FXML
    private void initialize() {
        System.out.println("PantallaMenu initialized");
        hideAllContainers();
        splashContainer.setVisible(true);

        // Configure player combos
        setupPlayerCombo(p2Combo, false);
        setupPlayerCombo(p3Combo, true);
        setupPlayerCombo(p4Combo, true);

        // Configure color combos
        setupColorCombo(p1ColorCombo, "Azul");
        setupColorCombo(p2ColorCombo, "Rojo");
        setupColorCombo(p3ColorCombo, "Verde");
        setupColorCombo(p4ColorCombo, "Amarillo");

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
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(2), fullScreenHint);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(javafx.util.Duration.seconds(1));
        fade.setOnFinished(e -> fullScreenHint.setVisible(false));
        fade.play();
    }

    private void setupColorCombo(ComboBox<String> combo, String defaultColor) {
        if (combo == null) return;
        combo.getItems().clear();
        combo.getItems().addAll(COLORES_VALIDOS);
        combo.setValue(defaultColor);
    }

    private void setupPlayerCombo(ComboBox<String> combo, boolean allowEmpty) {
        if (combo == null) return;
        ObservableList<String> options = FXCollections.observableArrayList();
        if (allowEmpty) {
            options.add("Vacío");
        }
        
        ArrayList<String> allPlayers = db.obtenerTodosLosJugadores();
        for (String p : allPlayers) {
            if (!p.equals(currentUserName)) { // Don't allow selecting self
                options.add("Jugador: " + p);
            }
        }
        combo.setItems(options);
        if (allowEmpty) combo.setValue("Vacío");
        else if (!options.isEmpty()) combo.getSelectionModel().select(0);
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
                // Refresh combos now that we know the currentUserName
                setupPlayerCombo(p2Combo, false);
                setupPlayerCombo(p3Combo, true);
                setupPlayerCombo(p4Combo, true);
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
        p1NameLabel.setText(currentUserName);
        // Reset colour combos to default values each time config is opened
        setupColorCombo(p1ColorCombo, "Azul");
        setupColorCombo(p2ColorCombo, "Rojo");
        setupColorCombo(p3ColorCombo, "Verde");
        setupColorCombo(p4ColorCombo, "Amarillo");
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
        // Validate player config
        configuredPlayers.clear();
        playerConfigErrorLabel.setText("");

        // Player 1 is ALWAYS the current user
        String p1Color = p1ColorCombo != null ? p1ColorCombo.getValue() : "Azul";
        if (p1Color == null) p1Color = "Azul";
        configuredPlayers.add(new Jugador(currentUserId, currentUserName, p1Color));

        // Player 2 is Mandatory Human
        String p2Val = p2Combo.getValue();
        if (p2Val == null || p2Val.equals("Vacío")) {
            playerConfigErrorLabel.setText("El Jugador 2 es obligatorio.");
            return;
        }
        String p2Color = p2ColorCombo != null && p2ColorCombo.getValue() != null ? p2ColorCombo.getValue() : "Rojo";
        processHumanSlot(p2Val, 2, p2Color);

        // Player 3 & 4 are Optional Humans
        String p3Color = p3ColorCombo != null && p3ColorCombo.getValue() != null ? p3ColorCombo.getValue() : "Verde";
        String p4Color = p4ColorCombo != null && p4ColorCombo.getValue() != null ? p4ColorCombo.getValue() : "Amarillo";
        processHumanSlot(p3Combo.getValue(), 3, p3Color);
        processHumanSlot(p4Combo.getValue(), 4, p4Color);

        // Foca CPU is Optional
        if (focaCheckbox.isSelected()) {
            // Foca color is irrelevant or special
            configuredPlayers.add(new CPU(configuredPlayers.size() + 1, "Foca Loca"));
        }

        if (configuredPlayers.size() < 2) {
            playerConfigErrorLabel.setText("Mínimo 2 jugadores.");
            return;
        }

        hideAllContainers();
        seedSelectionContainer.setVisible(true);
        seedErrorLabel.setText("");
        seedField.setText("");
    }

    private void processHumanSlot(String val, int id, String color) {
        if (val == null || val.equals("Vacío")) return;
        if (val.startsWith("Jugador: ")) {
            String name = val.replace("Jugador: ", "");
            configuredPlayers.add(new Jugador(id, name, color));
        }
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