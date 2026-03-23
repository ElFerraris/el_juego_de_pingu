import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
        String usr = "DW2526_GR08_PINGU";
        String pwd = "ADGAIBS";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, usr, pwd);
            System.out.println("Conectado!");
            
            // Check procedure/function arguments for INSERTAR_PARTIDA
            String sql = "SELECT argument_name, data_type, in_out FROM all_arguments " +
                         "WHERE object_name = 'INSERTAR_PARTIDA' AND owner = 'DW2526_GR08_PINGU'";
            
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("Arguments for INSERTAR_PARTIDA:");
                while (rs.next()) {
                    System.out.println("Arg: " + rs.getString(1) + " | Type: " + rs.getString(2) + " | Dir: " + rs.getString(3));
                }
            }
            
            // Also check INSERTAR_JUGADOR
            sql = "SELECT argument_name, data_type, in_out FROM all_arguments " +
                  "WHERE object_name = 'INSERTAR_JUGADOR' AND owner = 'DW2526_GR08_PINGU'";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("\nArguments for INSERTAR_JUGADOR:");
                while (rs.next()) {
                    System.out.println("Arg: " + rs.getString(1) + " | Type: " + rs.getString(2) + " | Dir: " + rs.getString(3));
                }
            }

            // Also check INSERTAR_PARTICIPACION
            sql = "SELECT argument_name, data_type, in_out FROM all_arguments " +
                  "WHERE object_name = 'INSERTAR_PARTICIPACION' AND owner = 'DW2526_GR08_PINGU'";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                System.out.println("\nArguments for INSERTAR_PARTICIPACION:");
                while (rs.next()) {
                    System.out.println("Arg: " + rs.getString(1) + " | Type: " + rs.getString(2) + " | Dir: " + rs.getString(3));
                }
            }
            
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
