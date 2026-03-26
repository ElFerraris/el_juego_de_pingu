package controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class SplashController {

    @FXML private Label fullScreenHint;

    @FXML
    public void initialize() {
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

    @FXML
    private void handleStartSplash(ActionEvent event) {
        NavigationController.navigateTo(event, "LoginView.fxml");
    }

    @FXML
    private void handleQuitGame(ActionEvent event) {
        javafx.application.Platform.exit();
    }
}
