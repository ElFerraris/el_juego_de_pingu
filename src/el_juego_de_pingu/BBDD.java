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
		String usr = "DW2526_GUE_DANIEL";
		String pwd = "A47122795G";
		
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
	
	
	
	
	
	
	public static void main(String[] args) {
		conectarBD();
	}
	
}
