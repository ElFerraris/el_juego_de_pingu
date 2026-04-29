package datos;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import controlador.Juego;
import modelo.Foca;
import modelo.Pinguino;
import modelo.Jugador;

/**
 * Motor de persistencia y gestión de datos del juego.
 */
public class BBDD {

    public static Connection conectarBD() {
        System.out.println("Estableciendo conexión con la Base de Datos...");
        String url = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
        String usr = "DW2526_GR08_PINGU";
        String pwd = "ADGAIBS";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, usr, pwd);
            return con;
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return null;
    }

    public int guardarNuevaPartida(Juego juego) {
        try (Connection con = conectarBD()) {
            if (con == null) return 0;
            String sql = "{? = call insertar_partida(?, ?)}";
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
                cstmt.setString(2, juego.getTablero().getSeed());
                cstmt.setString(3, juego.getNombrePartida());
                cstmt.execute();
                return cstmt.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(" ERROR en BBDD: " + e.getMessage());
            return 0;
        }
    }

    public boolean existeJugador(Connection con, String nickname) {
        String sql = "SELECT COUNT(*) FROM jugador WHERE nickname = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(" Error al comprobar existencia: " + e.getMessage());
        }
        return false;
    }

    public int registrarJugadorSiNoExiste(Jugador j) {
        int idJugador = -1;
        try (Connection con = conectarBD()) {
            if (con == null) return -1;
            String sqlCheck = "SELECT id_jugador FROM jugador WHERE nickname = ?";
            try (PreparedStatement pstmt = con.prepareStatement(sqlCheck)) {
                pstmt.setString(1, j.getNombre());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) idJugador = rs.getInt("id_jugador");
                }
            }
            if (idJugador == -1) {
                String sqlInsert = "{call insertar_jugador(?, ?, ?)}";
                try (CallableStatement cstmt = con.prepareCall(sqlInsert)) {
                    cstmt.setString(1, j.getNombre());
                    cstmt.setString(2, "pingu123");
                    cstmt.setInt(3, (j instanceof Foca) ? 1 : 0);
                    cstmt.execute();
                    try (PreparedStatement pstmt2 = con.prepareStatement(sqlCheck)) {
                        pstmt2.setString(1, j.getNombre());
                        try (ResultSet rs2 = pstmt2.executeQuery()) {
                            if (rs2.next()) idJugador = rs2.getInt("id_jugador");
                        }
                    }
                }
            }
            return idJugador;
        } catch (SQLException e) {
            System.out.println(" ERROR en registro de jugador: " + e.getMessage());
            return -1;
        }
    }

    public boolean insertarParticipacion(int idPartida, int idJugador, String color) {
        String sql = "{call insertar_participacion(?, ?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setInt(1, idPartida);
                cstmt.setInt(2, idJugador);
                cstmt.setString(3, color);
                cstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            System.out.println(" ERROR en insertarParticipacion: " + e.getMessage());
            return false;
        }
    }

    public static int obtenerIdJugador(Connection con, String nickname) {
        String sql = "SELECT id_jugador FROM jugador WHERE nickname = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_jugador");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID: " + e.getMessage());
        }
        return -1;
    }

    public boolean actualizarParticipacion(int idPartida, Jugador j) {
        String sql = "{call actualizar_participacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setInt(1, idPartida);
                cstmt.setInt(2, j.getId());
                cstmt.setInt(3, j.getPosicion());
                cstmt.setString(4, j.getColor());
                cstmt.setInt(5, j.getInventario().getCantidad("Pez"));
                cstmt.setInt(6, j.getInventario().getCantidad("BolaNieve"));
                cstmt.setInt(7, j.getInventario().getCantidad("DadoLento"));
                cstmt.setInt(8, j.getInventario().getCantidad("DadoRapido"));
                cstmt.setInt(9, j.getTurnosBloqueados());
                cstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean actualizarEstadoPartida(int idPartida, Juego juego) {
        String sql = "{call actualizar_partida(?, ?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setInt(1, idPartida);
                cstmt.setInt(2, juego.getTurnoActual());
                if (juego.getGanador() != null) cstmt.setInt(3, juego.getGanador().getId());
                else cstmt.setNull(3, java.sql.Types.INTEGER);
                cstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean guardarEstadoCompleto(int idPartida, Juego juego) {
        String sqlPartida = "{call actualizar_partida(?, ?, ?)}";
        String sqlJugador = "{call actualizar_participacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            con.setAutoCommit(false);
            try (CallableStatement cstmtP = con.prepareCall(sqlPartida)) {
                cstmtP.setInt(1, idPartida);
                cstmtP.setInt(2, juego.getTurnoActual());
                if (juego.getGanador() != null) cstmtP.setInt(3, juego.getGanador().getId());
                else cstmtP.setNull(3, java.sql.Types.INTEGER);
                cstmtP.execute();
            }
            try (CallableStatement cstmtJ = con.prepareCall(sqlJugador)) {
                for (Jugador j : juego.getJugadores()) {
                    cstmtJ.setInt(1, idPartida);
                    cstmtJ.setInt(2, j.getId());
                    cstmtJ.setInt(3, j.getPosicion());
                    cstmtJ.setString(4, j.getColor());
                    cstmtJ.setInt(5, j.getInventario().getCantidad("Pez"));
                    cstmtJ.setInt(6, j.getInventario().getCantidad("BolaNieve"));
                    cstmtJ.setInt(7, j.getInventario().getCantidad("DadoLento"));
                    cstmtJ.setInt(8, j.getInventario().getCantidad("DadoRapido"));
                    cstmtJ.setInt(9, j.getTurnosBloqueados());
                    cstmtJ.execute();
                }
            }
            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean cargarDatosPartida(int idPartida, Juego juego) {
        String sqlPartida = "SELECT seed, torn_actual FROM partida WHERE num_partida = ?";
        String sqlJugadores = "SELECT j.id_jugador, j.nickname, j.es_cpu, p.posicion_actual, p.color, " +
                "p.num_peces, p.num_bolas_nieve, p.num_dados_lentos, p.num_dados_rapidos, p.turnos_bloqueado " +
                "FROM participacion_jugadores p " +
                "JOIN jugador j ON p.id_jugador = j.id_jugador " +
                "WHERE p.id_partida = ? ORDER BY j.id_jugador ASC";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (PreparedStatement pstmt = con.prepareStatement(sqlPartida)) {
                pstmt.setInt(1, idPartida);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        juego.getTablero().introducirSeed(rs.getString("seed"));
                        juego.setTurnoActual(rs.getInt(("torn_actual")));
                        juego.getTablero().setIdPartida(idPartida);
                    } else return false;
                }
            }
            juego.getJugadores().clear();
            try (PreparedStatement pstmt = con.prepareStatement(sqlJugadores)) {
                pstmt.setInt(1, idPartida);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Jugador nuevo;
                        int id = rs.getInt("id_jugador");
                        String nombre = rs.getString("nickname");
                        String color = rs.getString("color");
                        if (rs.getInt("es_cpu") == 1) nuevo = new Foca(id, nombre);
                        else nuevo = new Pinguino(id, nombre, color);
                        nuevo.setPosicion(rs.getInt("posicion_actual"));
                        nuevo.setTurnosBloqueados(rs.getInt("turnos_bloqueado"));
                        nuevo.getInventario().agregarObjetos("Pez", rs.getInt("num_peces"));
                        nuevo.getInventario().agregarObjetos("BolaNieve", rs.getInt("num_bolas_nieve"));
                        nuevo.getInventario().agregarObjetos("DadoLento", rs.getInt("num_dados_lentos"));
                        nuevo.getInventario().agregarObjetos("DadoRapido", rs.getInt("num_dados_rapidos"));
                        juego.agregarJugador(nuevo);
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void mostrarPartidasPendientes() {
        String sql = "SELECT num_partida, seed, hora_partida FROM partida WHERE ganador IS NULL ORDER BY hora_partida DESC";
        try (Connection con = conectarBD(); PreparedStatement pstmt = con.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                System.out.printf("ID: %d | Seed: %s | Fecha: %s\n", rs.getInt("num_partida"), rs.getString("seed"), rs.getString("hora_partida"));
            }
        } catch (SQLException e) {
            System.out.println(" ERROR: " + e.getMessage());
        }
    }

    public ArrayList<modelo.PartidaGuardada> obtenerPartidasPendientes(int idUsuario) {
        Map<Integer, modelo.PartidaGuardada> mapa = new LinkedHashMap<>();
        String sql = "SELECT p.num_partida, p.seed, p.nombre, TO_CHAR(p.hora_partida, 'DD/MM/YYYY HH24:MI') as hora_partida, pj.color " +
                "FROM partida p LEFT JOIN participacion_jugadores pj ON p.num_partida = pj.id_partida " +
                "WHERE p.ganador IS NULL AND p.num_partida IN (SELECT id_partida FROM participacion_jugadores WHERE id_jugador = ?) " +
                "ORDER BY p.hora_partida DESC";
        try (Connection con = conectarBD(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("num_partida");
                    if (!mapa.containsKey(id)) {
                        List<String> colores = new ArrayList<>();
                        if (rs.getString("color") != null) colores.add(rs.getString("color"));
                        mapa.put(id, new modelo.PartidaGuardada(id, rs.getString("seed"), rs.getString("hora_partida"), rs.getString("nombre"), colores));
                    } else if (rs.getString("color") != null) mapa.get(id).getColoresJugadores().add(rs.getString("color"));
                }
            }
        } catch (SQLException e) {
            System.out.println(" ERROR: " + e.getMessage());
        }
        return new ArrayList<>(mapa.values());
    }

    public void mostrarRankingMasPartidas() {
        String sql = "SELECT j.nickname, COUNT(p.id_partida) AS total_partidas FROM jugador j " +
                "JOIN participacion_jugadores p ON j.id_jugador = p.id_jugador " +
                "WHERE j.nickname != 'Foca Loca' GROUP BY j.nickname ORDER BY total_partidas DESC";
        try (Connection con = conectarBD(); ResultSet rs = con.prepareStatement(sql).executeQuery()) {
            while (rs.next()) System.out.printf("%s: %d partidas\n", rs.getString("nickname"), rs.getInt("total_partidas"));
        } catch (SQLException e) {
            System.out.println(" ERROR: " + e.getMessage());
        }
    }

    public static boolean loginJugador(String nickname, String password) {
        String sql = "{? = call verificar_password(?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
                cstmt.setString(2, nickname);
                cstmt.setString(3, password);
                cstmt.execute();
                return cstmt.getInt(1) == 1;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public static int registrarNuevoJugador(String nickname, String password, boolean esCpu) {
        String sql = "{call insertar_jugador(?, ?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return -1;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setString(1, nickname);
                cstmt.setString(2, password);
                cstmt.setInt(3, esCpu ? 1 : 0);
                cstmt.execute();
                return obtenerIdJugador(con, nickname);
            }
        } catch (SQLException e) {
            return -1;
        }
    }

    public ArrayList<String> obtenerTodosLosJugadores() {
        ArrayList<String> jugadores = new ArrayList<>();
        String sql = "SELECT nickname FROM jugador WHERE es_cpu = 0 ORDER BY nickname";
        try (Connection con = conectarBD(); ResultSet rs = con.prepareStatement(sql).executeQuery()) {
            while (rs.next()) jugadores.add(rs.getString("nickname"));
        } catch (SQLException e) {
            System.out.println(" ERROR: " + e.getMessage());
        }
        return jugadores;
    }

    public boolean eliminarPartida(int idPartida) {
        String sql = "DELETE FROM partida WHERE num_partida = ?";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, idPartida);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean cambiarContrasenaJugador(int idJugador, String nuevaContrasena) {
        String sql = "{call actualizar_jugador(?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setInt(1, idJugador);
                cstmt.setString(2, nuevaContrasena);
                cstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    // --- NUEVOS MÉTODOS DBMS ---

    private String leerBufferDBMS(Connection con) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (CallableStatement fetchStmt = con.prepareCall("begin dbms_output.get_line(?, ?); end;")) {
            fetchStmt.registerOutParameter(1, java.sql.Types.VARCHAR);
            fetchStmt.registerOutParameter(2, java.sql.Types.INTEGER);
            int status = 0;
            while (status == 0) {
                fetchStmt.execute();
                String line = fetchStmt.getString(1);
                status = fetchStmt.getInt(2);
                if (status == 0 && line != null) sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private void habilitarDBMS(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.execute("begin dbms_output.enable(); end;");
        }
    }

    public String obtenerRankingYErroresDBMS() {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("begin sp_ranking_y_errores(); end;");
            }
            return leerBufferDBMS(con);
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String obtenerJugadoresConRecordDBMS() {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            int record = 0;
            try (CallableStatement cstmt = con.prepareCall("{? = call fun_record_absoluto()}")) {
                cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
                cstmt.execute();
                record = cstmt.getInt(1);
            }
            try (CallableStatement cstmt = con.prepareCall("begin p_jugadores_con_record(?); end;")) {
                cstmt.setInt(1, record);
                cstmt.execute();
            }
            return leerBufferDBMS(con);
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String obtenerJugadoresEncimaMediaDBMS() {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("begin p_jugadores_encima_media(); end;");
            }
            return leerBufferDBMS(con);
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public String obtenerPorcentajeJugadorDBMS(int victorias) {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            double pct = 0;
            try (CallableStatement cstmt = con.prepareCall("{? = call fun_pct_inferior(?)}")) {
                cstmt.registerOutParameter(1, java.sql.Types.DOUBLE);
                cstmt.setInt(2, victorias);
                cstmt.execute();
                pct = cstmt.getDouble(1);
            }
            return "El nivel de victorias (" + victorias + ") supera al " + String.format("%.2f", pct) + "% de los jugadores.";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }
}
