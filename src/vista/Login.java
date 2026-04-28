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

/**
 * Controlador de la interfaz de acceso (Login) y registro del juego.
 * 
 * <p>
 * Gestiona la autenticación de usuarios contra la base de datos, el registro de
 * nuevos
 * jugadores, la retroalimentación visual de errores y la reproducción de
 * efectos
 * sonoros vinculados a la interacción. Una vez autenticado, establece el
 * usuario
 * en el {@link GameContext} y redirige a la escena de introducción.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Login {

	/** Botón para ejecutar la acción de login o registro. */
	@FXML
	private Button btn_login;
	/** Campo de entrada para la contraseña (enmascarado). */
	@FXML
	private PasswordField text_pswd;
	/** Campo de entrada para el nombre de usuario. */
	@FXML
	private TextField text_user;
	/** Etiqueta para mostrar mensajes de error o éxito al usuario. */
	@FXML
	private Label text_error;
	/** Enlace para alternar entre los modos de Login y Registro. */
	@FXML
	private Hyperlink link_regis;

	/** Flag que determina si el formulario actúa como registro o como acceso. */
	private boolean modoRegistro = false;

	/** Efecto sonoro para clics en botones. */
	private AudioClip soundButton;
	/** Efecto sonoro para acciones exitosas. */
	private AudioClip soundCorrect;
	/** Efecto sonoro para errores de validación o acceso. */
	private AudioClip soundError;

	/**
	 * Método de inicialización automática de JavaFX.
	 * Limpia el estado de la vista y carga los recursos de audio.
	 */
	@FXML
	public void initialize() {
		limpiarErrores();
		cargarSonidos();
	}

	/**
	 * Carga los archivos de audio desde los recursos y ajusta su volumen
	 * según la configuración actual del usuario.
	 */
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

	/**
	 * Reproduce un clip de audio de forma segura.
	 * 
	 * @param clip El {@link AudioClip} a reproducir.
	 */
	private void reproducir(AudioClip clip) {
		if (clip != null) {
			clip.play();
		}
	}

	/**
	 * Maneja la pulsación del botón principal.
	 * 
	 * <p>
	 * Valida que los campos no estén vacíos y deriva la acción a
	 * {@code iniciarSesion} o {@code registrarUsuario} según el modo activo.
	 * </p>
	 * 
	 * @param event El evento de acción disparado por el botón.
	 */
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

	/**
	 * Intenta autenticar al usuario consultando la base de datos.
	 * 
	 * @param user  Nombre de usuario.
	 * @param pswd  Contraseña.
	 * @param event Evento para la transición de escena.
	 */
	private void iniciarSesion(String user, String pswd, ActionEvent event) {
		if (BBDD.loginJugador(user, pswd)) {
			reproducir(soundCorrect);
			abrirIntro(event);
		} else {
			reproducir(soundError);
			mostrarError("Usuario o contraseña incorrectos", true, true);
		}
	}

	/**
	 * Intenta dar de alta un nuevo usuario en el sistema.
	 * 
	 * @param user  Nombre de usuario deseado.
	 * @param pswd  Contraseña.
	 * @param event Evento de acción.
	 */
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

	/**
	 * Cambia la interfaz entre el modo Login y el modo Registro.
	 * Actualiza los textos de los botones y enlaces.
	 * 
	 * @param event El evento disparado por el hipervínculo.
	 */
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

	/**
	 * Muestra un mensaje de error y aplica estilos visuales a los campos afectados.
	 * 
	 * @param mensaje   Texto explicativo.
	 * @param errorUser Si se debe marcar el campo de usuario como erróneo.
	 * @param errorPswd Si se debe marcar el campo de contraseña como erróneo.
	 */
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

	/**
	 * Restablece los mensajes y estilos de error de la interfaz.
	 */
	private void limpiarErrores() {
		text_error.setText(" ");
		text_error.getStyleClass().remove("success-label");
		text_user.getStyleClass().remove("field-error");
		text_pswd.getStyleClass().remove("field-error");
	}

	/**
	 * Finaliza el proceso de acceso, inicializa el contexto de juego y navega a la
	 * introducción.
	 * 
	 * @param event Evento de acción para la navegación.
	 */
	private void abrirIntro(ActionEvent event) {
		try {
			// 1. Obtenemos los datos del jugador de la BD para la sesión
			String username = text_user.getText().trim();
			Connection con = BBDD.conectarBD();
			int id = BBDD.obtenerIdJugador(con, username);
			if (con != null)
				con.close();

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
