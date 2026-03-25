package vista;

import java.io.IOException;

import datos.BBDD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Login {

	@FXML
	private Button btn_login;
	@FXML
	private PasswordField text_pswd;
	@FXML
	private TextField text_user;
	@FXML
	private Label text_error;
	@FXML
	private Hyperlink link_regis;

	private boolean modoRegistro = false;

	@FXML
	public void initialize() {
		limpiarErrores();
	}

	@FXML
	void handleLogin(ActionEvent event) {
		limpiarErrores();

		String user = text_user.getText().trim();
		String pswd = text_pswd.getText().trim();

		// Validación común: campos vacíos
		if (user.isEmpty() || pswd.isEmpty()) {
			if (user.isEmpty() && pswd.isEmpty()) {
				mostrarError("Faltan usuario y contraseña", true, true);
			} else if (user.isEmpty()) {
				mostrarError("Falta el usuario", true, false);
			} else {
				mostrarError("Falta la contraseña", false, true);
			}
			return;
		}

		if (modoRegistro) {
			registrarUsuario(user, pswd, event);
		} else {
			iniciarSesion(user, pswd, event);
		}
	}

	private void iniciarSesion(String user, String pswd, ActionEvent event) {
		if (BBDD.loginJugador(user, pswd)) {
			abrirIntro(event);
		} else {
			mostrarError("Usuario o contraseña incorrectos", true, true);
		}
	}

	private void registrarUsuario(String user, String pswd, ActionEvent event) {
		int resultado = BBDD.registrarNuevoJugador(user, pswd, false);
		if (resultado != -1) {
			alternarModo(null);
			text_error.setText("¡Registro completado! Ya puedes entrar.");
			text_error.getStyleClass().add("success-label");
		} else {
			mostrarError("El nombre '" + user + "' ya está ocupado.", true, false);
		}
	}

	@FXML
	void alternarModo(ActionEvent event) {
		modoRegistro = !modoRegistro;
		limpiarErrores();

		if (modoRegistro) {
			btn_login.setText("REGISTRAR");
			link_regis.setText("¿Ya tienes cuenta? Entra");
		} else {
			btn_login.setText("LOGIN");
			link_regis.setText("¿No tienes cuenta? Regístrate");
		}
	}

	private void mostrarError(String mensaje, boolean errorUser, boolean errorPswd) {
		text_error.setText(mensaje);
		text_error.getStyleClass().remove("success-label");
		if (errorUser) {
			text_user.getStyleClass().add("field-error");
		}
		if (errorPswd) {
			text_pswd.getStyleClass().add("field-error");
		}
	}

	private void limpiarErrores() {
		text_error.setText(" ");
		text_error.getStyleClass().remove("success-label");
		text_user.getStyleClass().remove("field-error");
		text_pswd.getStyleClass().remove("field-error");
	}

	private void abrirIntro(ActionEvent event) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/vista/Intro.fxml"));
			Scene scene = new Scene(root);
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			stage.setScene(scene);
			stage.centerOnScreen();
			stage.setFullScreen(true); // Activar pantalla completa para el vídeo
			stage.setFullScreenExitHint(""); // Quitar el mensaje de "Presione ESC para salir"
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
			text_error.setText("Error al cargar la introducción");
		}
	}
}
