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

    /**
     * Cierra la conexión con la Base de Datos.
     * @param con Conexión a cerrar.
     */
    public static void desconectarBD(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("►Conexión cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.out.println("►Error al cerrar la conexión: " + e.getMessage());
        }
    }

    /**
     * Guarda una nueva partida en la base de datos.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public boolean guardarNuevaPartida(Connection con, Juego juego) {
        String sql = "DECLARE insertar_partida(?, ?); END;";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, juego.getTablero().getSeed());
            pstmt.setInt(2, juego.getTurnoActual());
            pstmt.execute(); // FIX: faltaba ejecutar la sentencia
            return true;
        } catch (SQLException e) {
            System.out.println("Error al guardar nueva partida: " + e.getMessage());
        }
        return false;
    }

    /**
     * Guarda la puntuación de un jugador en el ranking.
     */
    public static void guardarPuntuacion(Connection con, String nombre, int puntos) {
        String sql = "INSERT INTO RANKING VALUES (?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, puntos);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al guardar puntuación: " + e.getMessage());
        }
    }

    /**
     * Muestra el ranking de jugadores ordenado por puntos.
     */
    public static void mostrarRanking(Connection con) {
        String sql = "SELECT nombre, puntos FROM ranking ORDER BY puntos DESC";

        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n--- RANKING DE PINGÜINOS ---");
            System.out.println("NOMBRE\t\tPUNTOS");
            System.out.println("---------------------------");

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int puntos = rs.getInt("puntos");
                System.out.println(nombre + "\t\t" + puntos);
            }
            System.out.println("---------------------------\n");

        } catch (SQLException e) {
            System.out.println("► Error al leer el ranking: " + e.getMessage());
        }
    }
}
