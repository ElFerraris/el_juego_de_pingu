package vista;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
	void handleLogin(ActionEvent event) {
		// 1. Limpiar estados previos
		limpiarErrores();
		
		String user = text_user.getText().trim();
		String pswd = text_pswd.getText().trim();
		
		// 2. Validación de campos vacíos
		if (user.isEmpty() && pswd.isEmpty()) {
			mostrarError("Faltan usuario y contraseña", true, true);
			return;
		}
		if (user.isEmpty()) {
			mostrarError("Falta el usuario", true, false);
			return;
		}
		if (pswd.isEmpty()) {
			mostrarError("Falta la contraseña", false, true);
			return;
		}
		
		// 3. Comprobación de BBDD (Marcador de posición)
		/*
		 * TODO: Implementar la comprobación real con la base de datos aquí.
		 * Ejemplo:
		 * if (!BBDD.validarUsuario(user, pswd)) {
		 *     mostrarError("Usuario o contraseña incorrectos", true, true);
		 *     return;
		 * }
		 */
		
		// Para la prueba, aceptamos "admin" / "admin"
		if (user.equals("admin") && pswd.equals("admin")) {
			abrirIntro(event);
		} else {
			// Si no es admin/admin, simulamos error de BBDD
			mostrarError("Usuario o contraseña incorrectos", true, true);
		}
	}

	private void mostrarError(String mensaje, boolean errorUser, boolean errorPswd) {
		text_error.setText(mensaje);
		if (errorUser) {
			text_user.getStyleClass().add("field-error");
		}
		if (errorPswd) {
			text_pswd.getStyleClass().add("field-error");
		}
	}

	private void limpiarErrores() {
		text_error.setText(" ");
		text_user.getStyleClass().remove("field-error");
		text_pswd.getStyleClass().remove("field-error");
	}

	private void abrirIntro(ActionEvent event) {
		try {
			// Cargar la vista del Intro (Video)
			Parent root = FXMLLoader.load(getClass().getResource("/vista/Intro.fxml"));
			Scene scene = new Scene(root);
			
			// Obtener el Stage actual desde el evento
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
			
			// Cambiar la escena
			stage.setScene(scene);
			stage.centerOnScreen();
			stage.show();
			
		} catch (IOException e) {
			e.printStackTrace();
			text_error.setText("Error al cargar la introducción");
		}
	}
}
