package datos;

import java.sql.*;

import controlador.Juego;

/**
 * Clase para gestionar la conexión y operaciones con la Base de Datos.
 */
public class BBDD {

    /**
     * Establece conexión con la Base de Datos Oracle.
     * @return Conexión a la BD, o null si falla.
     */
    public static Connection conectarBD() {
        System.out.println("►Estableciendo conexión con la Base de Datos...");

        // Datos para la conexión
        String url = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
        String usr = "DW2526_GR08_PINGU";
        String pwd = "ADGAIBS";

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, usr, pwd);

            if (con.isValid(5)) {
                System.out.println("►Conexión establecida con BBDD!");
            } else {
                System.out.println("►Conexión perdida (timeout 5s)");
            }

            return con;

        } catch (ClassNotFoundException e) {
            System.out.println("►No se ha encontrado el driver de Oracle.");
        } catch (SQLException e) {
            System.out.println("►No se pudo conectar, error en las credenciales");
            System.out.println("►ERROR: " + e.getMessage());
        }

        return null;
    }

/* FUNCIONES POR IMPLEMENTAR CON EL RESTO DEL JUEGO */
	
	///
	/// - REGISTRAR JUGADOR
	/// - 
	///
	
	
	
    public boolean guardarNuevaPartida(Juego juego) {
    	// 1. Intentamos conectar. Al ponerlo en el paréntesis del try,
        // se cerrará solo al llegar a la llave final }.
        try (Connection con = conectarBD()) { 
            
            if (con == null) return false;

            // 2. Preparamos la llamada al procedimiento de Oracle
            String sql = "{call insertar_partida(?)}";
            
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                // Pasamos la seed del objeto juego que recibimos
                cstmt.setString(1, juego.getTablero().getSeed());
                
                // 3. Ejecutamos
                cstmt.execute();
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("► ERROR en BBDD: " + e.getMessage());
            return false;
        }
    }
	/*
	public boolean guardarPartida(Connection con, Juego juego) {
		
		String sql = "";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
		
		} catch (SQLException e) {
			System.out.println("Error al guardar: " + e.getMessage());
		}
	}

	public Juego cargarPartida(Connection conConnection con) {

		String sql = "";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
		
		} catch (SQLException e) {
			System.out.println("Error al guardar: " + e.getMessage());
		}
	}

	public String encriptarDatos(Connection con, String datos) {

		String sql = "";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
		
		} catch (SQLException e) {
			System.out.println("Error al guardar: " + e.getMessage());
		}

	}

	public String desencriptarDatos(Connection con, String datos) {
	
		String sql = "";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
		
		} catch (SQLException e) {
			System.out.println("Error al guardar: " + e.getMessage());
		}
	
	}

	public boolean existePartidaGuardada(Connection con) {

		String sql = "";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
		
		} catch (SQLException e) {
			System.out.println("Error al guardar: " + e.getMessage());
		}

	}
	*/
    
    

        
    
}
