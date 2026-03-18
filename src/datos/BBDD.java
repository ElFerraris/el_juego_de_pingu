package datos;

import java.sql.*;
import java.util.ArrayList;

import controlador.Juego;
import modelo.CPU;
import modelo.Jugador;

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
    
    /**
     * Llama al procedimiento almacenado para insertar un único jugador.
     */
    public boolean insertarUnJugador(Connection con, Jugador j) {
        String sql = "{call insertar_jugador(?, ?, ?)}";
        
        try (CallableStatement cstmt = con.prepareCall(sql)) {
            // 1. Nickname
            cstmt.setString(1, j.getNombre());
            
            // 2. Contraseña (usamos una por defecto si el modelo no tiene)
            cstmt.setString(2, "pingu123"); 
            
            // 3. Es_CPU (Comprobamos si es instancia de la clase CPU)
            int esCpu = (j instanceof CPU) ? 1 : 0;
            cstmt.setInt(3, esCpu);
            
            cstmt.execute();
            return true;
            
        } catch (SQLException e) {
            // Si el error es porque el nickname ya existe (Unique Constraint), 
            // aquí es donde capturaríamos el error de Oracle.
            System.out.println("► Error al insertar a " + j.getNombre() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean existeJugador(Connection con, String nickname) {
        // Buscamos si hay algún registro con ese nombre
        String sql = "SELECT COUNT(*) FROM jugador WHERE nickname = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si el conteo es mayor a 0, el jugador ya existe
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("► Error al comprobar existencia: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Comprueba si el jugador existe y, si no, lo inserta en la base de datos.
     * @param j El objeto Jugador (o CPU) a registrar.
     * @return true si se insertó o ya existía, false si hubo un error crítico.
     */
    public boolean registrarJugadorSiNoExiste(Jugador j) {
        // Usamos try-with-resources para manejar la conexión automáticamente
        try (Connection con = conectarBD()) {
            if (con == null) return false;

            // 1. PASO: Comprobar si existe
            String sqlCheck = "SELECT COUNT(*) FROM jugador WHERE nickname = ?";
            boolean existe = false;
            
            try (PreparedStatement pstmt = con.prepareStatement(sqlCheck)) {
                pstmt.setString(1, j.getNombre());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        existe = true;
                    }
                }
            }

            // 2. PASO: Si no existe, llamar al procedimiento de inserción
            if (!existe) {
                String sqlInsert = "{call insertar_jugador(?, ?, ?)}";
                try (CallableStatement cstmt = con.prepareCall(sqlInsert)) {
                    cstmt.setString(1, j.getNombre());
                    cstmt.setString(2, "pingu123"); // Contraseña por defecto
                    
                    int esCpu = (j instanceof CPU) ? 1 : 0;
                    cstmt.setInt(3, esCpu);
                    
                    cstmt.execute();
                    System.out.println("► Nuevo jugador registrado: " + j.getNombre());
                }
            } else {
                System.out.println("► El jugador " + j.getNombre() + " ya está en la BD.");
            }
            
            return true;

        } catch (SQLException e) {
            System.out.println("► ERROR en registro de jugador: " + e.getMessage());
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
