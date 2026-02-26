package el_juego_de_pingu;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class BBDD {

	/**
	 * ESTABLECER CONEXION CON LA BASE DE DATOS
	 * 
	 * @return Conexion a la BD
	 */
	
	public static Connection conectarBD() {
		
		System.out.println("►Estableciendo conexion con la Base de Datos...");
		
		// 1) Datos para la conexion
		String url = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
		String usr = "DW2526_GR08_PINGU";
		String pwd = "ADGAIBS";
		
		try {
			// 2) Llamamos a jdbc8.jar
			Class.forName("oracle.jdbc.driver.OracleDriver");

			// 3) Establecemos conexion
			Connection con = DriverManager.getConnection(url, usr, pwd);

			// 4) Comprobar que la conexión es válida (timeout 5s)
			if (con.isValid(5)) {
				System.out.println("►Conexion establecida con BBDD!");	
			} else {
				System.out.println("►Conexion perdida (timeout 5s)");
			}

			return con;
			
		// Error si no encuentra el archivo jdbc8.jar
		} catch (ClassNotFoundException e) {
			System.out.println("►No se ha encontrado el driver de Oracle.");
			
		// Error si no se pudo conectar con la BD
		} catch (SQLException e) {
			System.out.println("►No se pudo conectar, error en las credenciales");
			System.out.println("►ERROR: " + e.getMessage());
		}
		
		return null;
	}
	
	/**
	 * DESCONEXION DE LA BASDE DE DATOS
	 * 
	 * @param con Conexion a Base de Datos
	 */
	
	public static void desconectarBD(Connection con) {
		
		try {
			// Comprovar que la Conexion no sea nula ni que ya este cerrada
			if (con != null && !con.isClosed()) {
				con.close();
				System.out.println("►Conexion cerrada correctamente.");
			}
		} catch (SQLException e) {
			System.out.println("►Error al cerrar la conexion: " + e.getMessage());
		}
	}
	
	/* FUNCIONES POR IMPLEMENTAR CON EL RESTO DEL JUEGO */
	
	/*
	public boolean guardarPartida(Connection con, Juego juego) {

	}

	public Juego cargarPartida(Connection conConnection con) {

	}

	public String encriptarDatos(Connection con, String datos) {

	}

	public String desencriptarDatos(Connection con, String datos) {
	
	}

	public boolean existePartidaGuardada(Connection con) {

	}
	*/	
	
	// EJEMPLO DE METODO PARA GUARDAR COSITAS
	/*
	public static void guardarPuntuacion(Connection con, String nombre, int puntos) {
		String sql = "INSERT INTO RANKING VALUES (?, ?)";
		
		// El 'try' aquí cierra automáticamente el PreparedStatement al terminar el método,
		// ¡PERO NO CIERRA 'con' (la conexión)!
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setString(1, nombre);
			pstmt.setInt(2, puntos);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Error al guardar: " + e.getMessage());
		}
	}
	*/
	
	// EJEMPLO DE METODO PARA VER COSITAS
	/*
	public static void mostrarRanking(Connection con) {
		String sql = "SELECT nombre, puntos FROM ranking ORDER BY puntos DESC";
		
		// Preparamos la sentencia y ejecutamos la consulta
		try (PreparedStatement pstmt = con.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) { // <--- executeQuery para SELECT
			
			System.out.println("\n--- RANKING DE PINGÜINOS ---");
			System.out.println("NOMBRE\t\tPUNTOS");
			System.out.println("---------------------------");
			
			// El ResultSet es como un puntero que empieza antes de la primera fila.
			// rs.next() salta a la siguiente fila. Si devuelve 'false', es que ya no hay más datos.
			while (rs.next()) {
				// Sacamos los datos de las columnas por su nombre o posición
				String nombre = rs.getString("nombre");
	            int puntos = rs.getInt("puntos");
	            
	            System.out.println(nombre + "\t\t" + puntos);
			}
			System.out.println("---------------------------\n");
			
		} catch (SQLException e) {
			System.out.println("► Error al leer el ranking: " + e.getMessage());
		}
	}
	*/
	
	
}
