package vista;

import datos.BBDD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.media.AudioClip;
import controlador.GameContext;
import controlador.NavigationController;
import modelo.Jugador;
import modelo.Pinguino;
import java.sql.Connection;

public class Login {
	// Controlador principal de la vista de acceso

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

	// Sonidos
	private AudioClip soundButton;
	private AudioClip soundCorrect;
	private AudioClip soundError;

	@FXML
	public void initialize() {
		limpiarErrores();
		cargarSonidos();
	}

	private void cargarSonidos() {
		try {
			double sfxVol = util.SettingsManager.getInstance().getSfxVolume();
			
			soundButton = new AudioClip(getClass().getResource("/assets/login/login_button.wav").toExternalForm());
			soundCorrect = new AudioClip(getClass().getResource("/assets/login/login_correct.wav").toExternalForm());
			soundError = new AudioClip(getClass().getResource("/assets/login/login_error.wav").toExternalForm());
			
			// Aplicar volumen actual
			soundButton.setVolume(sfxVol);
			soundCorrect.setVolume(sfxVol);
			soundError.setVolume(sfxVol);
			
		} catch (Exception e) {
			System.err.println("No se pudieron cargar los sonidos de login: " + e.getMessage());
		}
	}

	private void reproducir(AudioClip clip) {
		if (clip != null) {
			clip.play();
		}
	}

	@FXML
	void handleLogin(ActionEvent event) {
		reproducir(soundButton);
		limpiarErrores();

		String user = text_user.getText().trim();
		String pswd = text_pswd.getText().trim();

		if (user.isEmpty() || pswd.isEmpty()) {
			reproducir(soundError);
			if (user.isEmpty() && pswd.isEmpty()) {
				mostrarError("Faltan usuario y contraseña", true, true);
			} else if (user.isEmpty()) {
				mostrarError("Falta el usuario", true, false);
			} else {
				mostrarError("Falta la contraseña", false, true);
			}
		} else {
			if (modoRegistro) {
				registrarUsuario(user, pswd, event);
			} else {
				iniciarSesion(user, pswd, event);
			}
		}
	}

	private void iniciarSesion(String user, String pswd, ActionEvent event) {
		if (BBDD.loginJugador(user, pswd)) {
			reproducir(soundCorrect);
			abrirIntro(event);
		} else {
			reproducir(soundError);
			mostrarError("Usuario o contraseña incorrectos", true, true);
		}
	}

	private void registrarUsuario(String user, String pswd, ActionEvent event) {
		int resultado = BBDD.registrarNuevoJugador(user, pswd, false);
		if (resultado != -1) {
			reproducir(soundCorrect);
			alternarModo(null);
			text_error.setText("¡Registro completado! Ya puedes entrar.");
			text_error.getStyleClass().add("success-label");
		} else {
			reproducir(soundError);
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
			// 1. Obtenemos los datos del jugador de la BD para la sesión
			String username = text_user.getText().trim();
			Connection con = BBDD.conectarBD();
			int id = BBDD.obtenerIdJugador(con, username);
			if (con != null) con.close();

			// 2. Guardamos en el contexto global
			Pinguino jugadorActual = new Pinguino(id, username, "Azul"); // Azul por defecto
			GameContext.getInstance().setCurrentUser(jugadorActual);
			System.out.println("► Sesión iniciada para: " + username + " (ID: " + id + ")");

			// 3. Navegamos a la intro respetando el modo pantalla completa de las opciones
			NavigationController.navigateTo(event, "Intro.fxml", util.SettingsManager.getInstance().isFullscreen());
			
		} catch (Exception e) {
			e.printStackTrace();
			text_error.setText("Error al iniciar la sesión/intro");
		}
	}
}
