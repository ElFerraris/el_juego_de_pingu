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
 * 
 * <p>
 * Proporciona una interfaz robusta para interactuar con la base de datos
 * Oracle,
 * encargándose de la conectividad, ejecución de procedimientos almacenados y
 * consultas complejas para el guardado y carga de partidas, gestión de usuarios
 * y generación de rankings.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.1
 */
public class BBDD {

    /**
     * Establece una conexión activa con la base de datos Oracle remota.
     * 
     * <p>
     * Utiliza el driver Thin de Oracle y maneja internamente las excepciones
     * de conectividad y credenciales, proporcionando logs de diagnóstico en
     * caso de fallo.
     * </p>
     * 
     * @return Objeto {@link Connection} configurado, o {@code null} si la conexión
     *         falla.
     */
    public static Connection conectarBD() {
        System.out.println("Estableciendo conexión con la Base de Datos...");

        // Datos para la conexión
        String url = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
        String usr = "DW2526_GR08_PINGU";
        String pwd = "ADGAIBS";

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, usr, pwd);

            if (con.isValid(5)) {
                // System.out.println("Conexión establecida con BBDD!");
            } else {
                System.out.println("Conexión perdida (timeout 5s)");
            }

            return con;

        } catch (ClassNotFoundException e) {
            System.out.println("No se ha encontrado el driver de Oracle.");
        } catch (SQLException e) {
            System.out.println("No se pudo conectar, error en las credenciales");
            System.out.println("ERROR: " + e.getMessage());
        }

        return null;
    }

    /**
     * Registra una nueva partida en el sistema y obtiene su identificador único.
     * 
     * @param juego Instancia del {@link Juego} con la información del tablero.
     * @return El ID de la partida generado por la base de datos, o 0 si falla.
     */
    public int guardarNuevaPartida(Juego juego) {
        // 1. Intentamos conectar. Al ponerlo en el paréntesis del try,
        // se cerrará solo al llegar a la llave final }.
        try (Connection con = conectarBD()) {

            if (con == null)
                return 0;

            // 2. Preparamos la llamada al procedimiento de Oracle
            // Ahora recibe dos parámetros: seed y nombre
            String sql = "{? = call insertar_partida(?, ?)}";

            try (CallableStatement cstmt = con.prepareCall(sql)) {
                // Registramos el tipo del primer "?" (el retorno)
                cstmt.registerOutParameter(1, java.sql.Types.INTEGER);

                // 1. Semilla del tablero
                cstmt.setString(2, juego.getTablero().getSeed());

                // 2. Nombre de la partida
                cstmt.setString(3, juego.getNombrePartida());

                cstmt.execute();

                // Recuperamos el ID que nos dio la función
                int idPartida = cstmt.getInt(1);
                return idPartida;
            }

        } catch (SQLException e) {
            System.out.println(" ERROR en BBDD: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Verifica la existencia de un jugador en el sistema basándose en su nickname.
     * 
     * @param con      Conexión activa a la base de datos.
     * @param nickname Nombre de usuario a comprobar.
     * @return {@code true} si el jugador ya está registrado.
     */
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
            System.out.println(" Error al comprobar existencia: " + e.getMessage());
        }
        return false;
    }

    /**
     * Garantiza que un jugador exista en la base de datos antes de iniciar una
     * partida.
     * 
     * <p>
     * Realiza una comprobación de existencia y, en caso negativo, ejecuta el
     * registro del nuevo perfil. Devuelve siempre el ID único de base de datos
     * para su uso posterior en la sesión.
     * </p>
     * 
     * @param j El {@link Jugador} a validar o registrar.
     * @return El ID único del jugador en la base de datos, o -1 en caso de error.
     */
    public int registrarJugadorSiNoExiste(Jugador j) {
        int idJugador = -1; // Valor por defecto si falla

        try (Connection con = conectarBD()) {
            if (con == null)
                return -1;

            // 1. PASO: Comprobar si existe y pillar su ID
            String sqlCheck = "SELECT id_jugador FROM jugador WHERE nickname = ?";

            try (PreparedStatement pstmt = con.prepareStatement(sqlCheck)) {
                pstmt.setString(1, j.getNombre());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        idJugador = rs.getInt("id_jugador");
                    }
                }
            }

            // 2. PASO: Si no existe (idJugador sigue siendo -1), insertar
            if (idJugador == -1) {
                String sqlInsert = "{call insertar_jugador(?, ?, ?)}";
                try (CallableStatement cstmt = con.prepareCall(sqlInsert)) {
                    cstmt.setString(1, j.getNombre());
                    cstmt.setString(2, "pingu123");
                    cstmt.setInt(3, (j instanceof Foca) ? 1 : 0);
                    cstmt.execute();

                    // 3. PASO: Una vez insertado, volvemos a consultar para obtener el ID generado
                    try (PreparedStatement pstmt2 = con.prepareStatement(sqlCheck)) {
                        pstmt2.setString(1, j.getNombre());
                        try (ResultSet rs2 = pstmt2.executeQuery()) {
                            if (rs2.next())
                                idJugador = rs2.getInt("id_jugador");
                        }
                    }
                    System.out.println(" Nuevo jugador registrado: " + j.getNombre() + " (ID: " + idJugador + ")");
                }
            } else {
                System.out.println(" El jugador " + j.getNombre() + " ya está en la BD con ID: " + idJugador);
            }

            return idJugador; // Devolvemos el ID real de la base de datos

        } catch (SQLException e) {
            System.out.println(" ERROR en registro de jugador: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Vincula a un jugador con una partida específica y asigna su color.
     * 
     * @param idPartida ID de la partida activa.
     * @param idJugador ID del jugador participante.
     * @param color     Color representativo elegido.
     * @return {@code true} si la vinculación fue exitosa.
     */
    public boolean insertarParticipacion(int idPartida, int idJugador, String color) {
        // Llamada al procedimiento con los 2 parámetros de entrada (IN)
        String sql = "{call insertar_participacion(?, ?, ?)}";

        try (Connection con = conectarBD()) {

            if (con == null)
                return false;

            try (CallableStatement cstmt = con.prepareCall(sql)) {

                // 1. p_id_partida
                cstmt.setInt(1, idPartida);

                // 2. p_id_jugador
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

    /**
     * Obtiene el identificador numérico único de un jugador a partir de su nombre.
     * 
     * @param con      Conexión activa a la base de datos.
     * @param nickname El nombre de usuario.
     * @return El ID del jugador, o -1 si no se encuentra.
     */
    public static int obtenerIdJugador(Connection con, String nickname) {
        String sql = "SELECT id_jugador FROM jugador WHERE nickname = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_jugador");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ID: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Actualiza el estado persistente de un jugador (posición, inventario,
     * bloqueos)
     * dentro de una partida.
     * 
     * @param idPartida ID de la partida actual.
     * @param j         El {@link Jugador} cuyo estado se desea sincronizar.
     * @return {@code true} si la actualización fue correcta.
     */
    public boolean actualizarParticipacion(int idPartida, Jugador j) {
        String sql = "{call actualizar_participacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null)
                return false;

            try (CallableStatement cstmt = con.prepareCall(sql)) {
                // 1. IDs para el WHERE
                cstmt.setInt(1, idPartida);
                cstmt.setInt(2, j.getId()); // El ID que recuperamos antes

                // 2. Datos de posición y estética
                cstmt.setInt(3, j.getPosicion());
                cstmt.setString(4, j.getColor());

                // 3. Inventario (Accediendo a los contadores de tu clase Jugador/Inventario)
                // Ajusta estos getters según los nombres reales en tu código
                cstmt.setInt(5, j.getInventario().getCantidad("Pez"));
                cstmt.setInt(6, j.getInventario().getCantidad("BolaNieve"));
                cstmt.setInt(7, j.getInventario().getCantidad("DadoLento"));
                cstmt.setInt(8, j.getInventario().getCantidad("DadoRapido"));

                // 4. Estado de bloqueo
                cstmt.setInt(9, j.getTurnosBloqueados());

                cstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            System.out.println(" ERROR al actualizar participación de " + j.getNombre() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza la información global de una partida (turno y ganador).
     * 
     * @param idPartida ID de la partida a actualizar.
     * @param juego     Instancia del {@link Juego} con el estado actual.
     * @return {@code true} si la actualización fue exitosa.
     */
    public boolean actualizarEstadoPartida(int idPartida, Juego juego) {
        String sql = "{call actualizar_partida(?, ?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null)
                return false;

            try (CallableStatement cstmt = con.prepareCall(sql)) {
                // 1. p_num_partida: El ID de la fila que queremos actualizar
                cstmt.setInt(1, idPartida);

                // 2. p_torn_actual: ID del jugador que tiene el turno ahora
                // Obtenemos el jugador actual de la lista y sacamos su ID de BD
                cstmt.setInt(2, juego.getTurnoActual());

                // 3. p_ganador: ID del ganador (si existe)
                if (juego.getGanador() != null) {
                    cstmt.setInt(3, juego.getGanador().getId());
                } else {
                    // Si la partida sigue en curso, enviamos NULL a Oracle
                    cstmt.setNull(3, java.sql.Types.INTEGER);
                }

                cstmt.execute();
                return true;
            }
        } catch (SQLException e) {
            System.out.println(" ERROR al actualizar estado de partida: " + e.getMessage());
            return false;
        }
    }

    /**
     * Realiza un guardado atómico (transaccional) de todo el estado del juego.
     * 
     * <p>
     * Actualiza tanto la información global de la partida (turno, ganador) como
     * el estado individual de cada uno de los jugadores participantes.
     * </p>
     * 
     * @param idPartida ID de la partida a persistir.
     * @param juego     Instancia del {@link Juego} con el estado actual completo.
     * @return {@code true} si se completó la transacción con éxito.
     */
    public boolean guardarEstadoCompleto(int idPartida, Juego juego) {
        String sqlPartida = "{call actualizar_partida(?, ?, ?)}";
        String sqlJugador = "{call actualizar_participacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null)
                return false;

            // Iniciamos transacción para evitar guardados a medias
            con.setAutoCommit(false);

            // 1. Guardar la Partida
            try (CallableStatement cstmtP = con.prepareCall(sqlPartida)) {
                cstmtP.setInt(1, idPartida);
                cstmtP.setInt(2, juego.getTurnoActual());
                if (juego.getGanador() != null) {
                    cstmtP.setInt(3, juego.getGanador().getId());
                } else {
                    cstmtP.setNull(3, java.sql.Types.INTEGER);
                }
                cstmtP.execute();
            }

            // 2. Guardar a todos los Jugadores
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
            // Restaurar a true (aunque la conexión se cierra justo abajo)
            con.setAutoCommit(true);
            return true;

        } catch (SQLException e) {
            System.out.println(" ERROR al guardar estado completo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera y restaura el estado completo de una partida desde la base de datos.
     * 
     * <p>
     * Reconstruye el tablero mediante su semilla, restaura el turno actual y
     * repuebla la lista de jugadores con sus posiciones e inventarios exactos.
     * </p>
     * 
     * @param idPartida ID de la partida a cargar.
     * @param juego     Objeto {@link Juego} donde se volcarán los datos
     *                  recuperados.
     * @return {@code true} si la carga fue íntegra y exitosa.
     */
    public boolean cargarDatosPartida(int idPartida, Juego juego) {
        String sqlPartida = "SELECT seed, torn_actual FROM partida WHERE num_partida = ?";
        String sqlJugadores = "SELECT j.id_jugador, j.nickname, j.es_cpu, p.posicion_actual, p.color, " +
                "p.num_peces, p.num_bolas_nieve, p.num_dados_lentos, p.num_dados_rapidos, p.turnos_bloqueado " +
                "FROM participacion_jugadores p " +
                "JOIN jugador j ON p.id_jugador = j.id_jugador " +
                "WHERE p.id_partida = ?" +
                "ORDER BY j.id_jugador ASC";

        try (Connection con = conectarBD()) {
            if (con == null)
                return false;

            // 1. Cargar datos generales de la partida
            try (PreparedStatement pstmt = con.prepareStatement(sqlPartida)) {
                pstmt.setInt(1, idPartida);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        juego.getTablero().introducirSeed(rs.getString("seed"));
                        juego.setTurnoActual(rs.getInt(("torn_actual")));
                        juego.getTablero().setIdPartida(idPartida);
                        // El turno lo setearemos después de cargar los jugadores para evitar errores de
                        // índice
                    } else {
                        return false; // No existe la partida
                    }
                }
            }

            // 2. Cargar jugadores y sus estados
            juego.getJugadores().clear(); // Limpiamos la lista actual
            try (PreparedStatement pstmt = con.prepareStatement(sqlJugadores)) {
                pstmt.setInt(1, idPartida);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Jugador nuevo;
                        int id = rs.getInt("id_jugador");
                        String nombre = rs.getString("nickname");
                        String color = rs.getString("color");

                        if (rs.getInt("es_cpu") == 1) {
                            nuevo = new Foca(id, nombre);
                        } else {
                            nuevo = new Pinguino(id, nombre, color);
                        }

                        // Restaurar estado físico e inventario
                        nuevo.setPosicion(rs.getInt("posicion_actual"));
                        nuevo.setTurnosBloqueados(rs.getInt("turnos_bloqueado"));

                        // IMPORTANTE: Aquí debes rellenar el inventario de tu objeto Jugador
                        // Ejemplo:
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
            System.out.println(" ERROR al cargar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Imprime por consola un listado de las partidas que no han sido finalizadas
     * aún.
     * Útil para tareas de depuración y monitorización administrativa.
     */
    public void mostrarPartidasPendientes() {
        // Consulta: Solo donde ganador no tiene valor
        String sql = "SELECT num_partida, seed, hora_partida FROM partida WHERE ganador IS NULL ORDER BY hora_partida DESC";

        try (Connection con = conectarBD();
                PreparedStatement pstmt = con.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n=== PARTIDAS PENDIENTES DE FINALIZAR ===");
            System.out.printf("%-12s | %-40s | %-20s %n", "ID", "SEED", "ULTIMA CONEXION");
            System.out.println("------------------------------------------------------------");

            boolean hayPartidas = false;
            while (rs.next()) {
                hayPartidas = true;
                int id = rs.getInt("num_partida");
                String seed = rs.getString("seed");
                String fecha = rs.getString("hora_partida");

                System.out.printf("%-10d | %-20s | %-20s %n", id, seed, fecha);
            }

            if (!hayPartidas) {
                System.out.println("No hay partidas pendientes en la base de datos.");
            }
            System.out.println("------------------------------------------------------------\n");

        } catch (SQLException e) {
            System.out.println(" ERROR al listar partidas: " + e.getMessage());
        }
    }

    /**
     * Obtiene una lista de resúmenes de partidas en curso para un usuario
     * específico.
     * 
     * @param idUsuario ID del jugador que solicita sus partidas.
     * @return Lista de objetos {@link modelo.PartidaGuardada} con los datos de
     *         previsualización.
     */
    public ArrayList<modelo.PartidaGuardada> obtenerPartidasPendientes(int idUsuario) {
        // Usamos LinkedHashMap para mantener el orden de la consulta (ORDER BY
        // hora_partida DESC)
        Map<Integer, modelo.PartidaGuardada> mapa = new LinkedHashMap<>();

        String sql = "SELECT p.num_partida, p.seed, p.nombre, " +
                "TO_CHAR(p.hora_partida, 'DD/MM/YYYY HH24:MI') as hora_partida, " +
                "pj.color " +
                "FROM partida p " +
                "LEFT JOIN participacion_jugadores pj ON p.num_partida = pj.id_partida " +
                "WHERE p.ganador IS NULL " +
                "AND p.num_partida IN (SELECT id_partida FROM participacion_jugadores WHERE id_jugador = ?) " +
                "ORDER BY p.hora_partida DESC";

        try (Connection con = conectarBD();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("num_partida");
                    String color = rs.getString("color");

                    if (!mapa.containsKey(id)) {
                        List<String> colores = new ArrayList<>();
                        if (color != null)
                            colores.add(color);

                        mapa.put(id, new modelo.PartidaGuardada(
                                id,
                                rs.getString("seed"),
                                rs.getString("hora_partida"),
                                rs.getString("nombre"),
                                colores));
                    } else {
                        if (color != null) {
                            mapa.get(id).getColoresJugadores().add(color);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(" ERROR al obtener lista partidas pendientes: " + e.getMessage());
        }
        return new ArrayList<>(mapa.values());
    }

    /**
     * Genera y muestra un ranking de los jugadores humanos más activos en el
     * sistema,
     * excluyendo a las entidades controladas por la CPU.
     */
    public void mostrarRankingMasPartidas() {
        // Consulta: Unimos jugador con participación y contamos cuántas veces aparece
        // cada uno
        String sql = "SELECT j.nickname, COUNT(p.id_partida) AS total_partidas " +
                "FROM jugador j " +
                "JOIN participacion_jugadores p ON j.id_jugador = p.id_jugador " +
                "WHERE j.nickname != 'Foca Loca'" + // Quitamos la Foca
                "GROUP BY j.nickname " +
                "ORDER BY total_partidas DESC";

        try (Connection con = conectarBD();
                PreparedStatement pstmt = con.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n" + "=".repeat(45));
            System.out.println("       RANKING: PINGÜINOS MÁS ACTIVOS        ");
            System.out.println("=".repeat(45));

            System.out.printf("| %-25s | %-12s |%n", "NOMBRE JUGADOR", "PARTIDAS");
            System.out.println("-".repeat(45));

            boolean hayDatos = false;
            int puesto = 1;

            while (rs.next()) {
                hayDatos = true;
                String nombre = rs.getString("nickname");
                int total = rs.getInt("total_partidas");

                // Resaltamos el primer puesto con un emoji o marca
                String medalla = (puesto == 1) ? "🏆 " : (puesto + ". ");
                System.out.printf("| %-25s | %-12d |%n", medalla + nombre, total);
                puesto++;
            }

            if (!hayDatos) {
                System.out.println("| " + String.format("%-41s", "Aún no hay registros de participación.") + " |");
            }
            System.out.println("=".repeat(45) + "\n");

        } catch (SQLException e) {
            System.out.println(" ERROR al generar ranking: " + e.getMessage());
        }
    }

    /**
     * Realiza el login utilizando la función de encriptación de la base de datos.
     * 
     * @param nickname El nombre del jugador.
     * @param password La contraseña en texto plano.
     * @return {@code true} si las credenciales son válidas; {@code false} en caso
     *         contrario.
     */
    public static boolean loginJugador(String nickname, String password) {
        // Llamada a la función PL/SQL: {? = call nombre_funcion(?, ?)}
        String sql = "{? = call verificar_password(?, ?)}";
        try (Connection con = conectarBD()) {
            if (con == null)
                return false;
            try (CallableStatement cstmt = con.prepareCall(sql)) {
                // Registramos el tipo de salida (el RETURN de la función)
                cstmt.registerOutParameter(1, java.sql.Types.INTEGER);

                // Seteamos los parámetros de entrada
                cstmt.setString(2, nickname);
                cstmt.setString(3, password);
                cstmt.execute();
                // Recuperamos el resultado (1 = OK, 0 = FALLO)
                int resultado = cstmt.getInt(1);
                if (resultado == 1) {
                    System.out.println(" Acceso concedido para: " + nickname);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println(" ERROR crítico en el login seguro: " + e.getMessage());
        }
        System.out.println(" Credenciales inválidas.");
        return false;
    }

    /**
     * Crea una nueva cuenta de usuario con contraseña encriptada.
     * 
     * @param nickname Nombre único de usuario.
     * @param password Contraseña en texto plano.
     * @param esCpu    Indica si la entidad es controlada por la IA.
     * @return El ID generado para el nuevo usuario, o -1 si el registro falla.
     */
    public static int registrarNuevoJugador(String nickname, String password, boolean esCpu) {
        String sql = "{call insertar_jugador(?, ?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null)
                return -1;

            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setString(1, nickname);
                cstmt.setString(2, password);
                cstmt.setInt(3, esCpu ? 1 : 0);

                cstmt.execute();

                // Una vez insertado, recuperamos el ID que le ha tocado
                return obtenerIdJugador(con, nickname);
            }
        } catch (SQLException e) {
            System.out.println(" ERROR: El nombre '" + nickname + "' ya está ocupado.");
            return -1;
        }
    }

    /*
     * public boolean guardarPartida(Connection con, Juego juego) {
     * 
     * String sql = "";
     * try (PreparedStatement pstmt = con.prepareStatement(sql)) {
     * 
     * } catch (SQLException e) {
     * System.out.println("Error al guardar: " + e.getMessage());
     * }
     * }
     * 
     * public Juego cargarPartida(Connection conConnection con) {
     * 
     * String sql = "";
     * try (PreparedStatement pstmt = con.prepareStatement(sql)) {
     * 
     * } catch (SQLException e) {
     * System.out.println("Error al guardar: " + e.getMessage());
     * }
     * }
     * 
     * public String encriptarDatos(Connection con, String datos) {
     * 
     * String sql = "";
     * try (PreparedStatement pstmt = con.prepareStatement(sql)) {
     * 
     * } catch (SQLException e) {
     * System.out.println("Error al guardar: " + e.getMessage());
     * }
     * 
     * }
     * 
     * public String desencriptarDatos(Connection con, String datos) {
     * 
     * String sql = "";
     * try (PreparedStatement pstmt = con.prepareStatement(sql)) {
     * 
     * } catch (SQLException e) {
     * System.out.println("Error al guardar: " + e.getMessage());
     * }
     * 
     * }
     * 
     * public boolean existePartidaGuardada(Connection con) {
     * 
     * String sql = "";
     * try (PreparedStatement pstmt = con.prepareStatement(sql)) {
     * 
     * } catch (SQLException e) {
     * System.out.println("Error al guardar: " + e.getMessage());
     * }
     * 
     * }
     */

    /**
     * Recupera el listado completo de nombres de jugadores registrados en el
     * sistema
     * que no son controlados por la IA.
     * 
     * @return Una lista de cadenas con los nicknames de los jugadores humanos.
     */
    public ArrayList<String> obtenerTodosLosJugadores() {
        ArrayList<String> jugadores = new ArrayList<>();
        String sql = "SELECT nickname FROM jugador WHERE es_cpu = 0 ORDER BY nickname";
        try (Connection con = conectarBD();
                PreparedStatement pstmt = con.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                jugadores.add(rs.getString("nickname"));
            }
        } catch (SQLException e) {
            System.out.println(" ERROR al listar jugadores: " + e.getMessage());
        }
        return jugadores;
    }

    /**
     * Elimina permanentemente una partida de la base de datos.
     * 
     * <p>
     * Debido a la integridad referencial (ON DELETE CASCADE), esta acción
     * también elimina automáticamente todas las participaciones asociadas.
     * </p>
     * 
     * @param idPartida El identificador de la partida a borrar.
     * @return {@code true} si se eliminó al menos un registro.
     */
    public boolean eliminarPartida(int idPartida) {
        String sql = "DELETE FROM partida WHERE num_partida = ?";

        try (Connection con = conectarBD()) {

            if (con == null) {
                return false;
            }

            try (PreparedStatement pstmt = con.prepareStatement(sql)) {
                pstmt.setInt(1, idPartida);

                int filasAfectadas = pstmt.executeUpdate();

                if (filasAfectadas > 0) {
                    System.out.println(" Partida " + idPartida + " eliminada.");
                    return true;
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            System.out.println(" ERROR al eliminar partida: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Actualiza la contraseña y el estado de CPU de un jugador existente.
     * 
     * @param idJugador      ID único del jugador en la base de datos.
     * @param nuevaContrasena La nueva contraseña en texto plano (el procedimiento se encarga del Hash y el Salt).
     * @return {@code true} si la actualización fue exitosa.
     */
    public boolean cambiarContrasenaJugador(int idJugador, String nuevaContrasena) {
        // Llamada al procedimiento: {call actualizar_jugador(p_id, p_pas)}
        String sql = "{call actualizar_jugador(?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null) return false;

            try (CallableStatement cstmt = con.prepareCall(sql)) {
                // 1. p_id_jugador
                cstmt.setInt(1, idJugador);
                
                // 2. p_contrasena (El procedimiento generará el nuevo Salt y MD5)
                cstmt.setString(2, nuevaContrasena);
                
                cstmt.execute();
                
                System.out.println(" Seguridad: Contraseña actualizada para el jugador ID: " + idJugador);
                return true;
            }
        } catch (SQLException e) {
            System.out.println(" ERROR al cambiar contraseña: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // --- NUEVOS MÉTODOS DBMS (REPORTES Y ESTADÍSTICAS) ---
    // =========================================================================

    /**
     * Lee el buffer de salida de DBMS_OUTPUT de la sesión de Oracle.
     * 
     * @param con Conexión activa a la base de datos.
     * @return El contenido acumulado en el buffer como una cadena de texto.
     * @throws SQLException Si ocurre un error al acceder al buffer.
     */
    private String leerBufferDBMS(Connection con) throws SQLException {
        StringBuilder sb = new StringBuilder();
        try (CallableStatement fetchStmt = con.prepareCall("begin dbms_output.get_line(?, ?); end;")) {
            fetchStmt.registerOutParameter(1, java.sql.Types.VARCHAR);
            fetchStmt.registerOutParameter(2, java.sql.Types.INTEGER);
            int status = 0;
            // status = 0 indica que hay más líneas en el buffer
            while (status == 0) {
                fetchStmt.execute();
                String line = fetchStmt.getString(1);
                status = fetchStmt.getInt(2);
                if (status == 0 && line != null) sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Habilita el buffer de DBMS_OUTPUT para la conexión actual.
     * 
     * @param con Conexión activa a la base de datos.
     * @throws SQLException Si no se puede habilitar el buffer.
     */
    private void habilitarDBMS(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.execute("begin dbms_output.enable(); end;");
        }
    }

    /**
     * Obtiene el ranking general de partidas jugadas mediante un reporte DBMS.
     * 
     * @return Texto formateado con el ranking o mensaje de error.
     */
    public String obtenerRankingYErroresDBMS() {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("begin p_ranking_partidas_jugadas(); end;");
            }
            return leerBufferDBMS(con);
        } catch (SQLException e) {
            return "Error al obtener ranking: " + e.getMessage();
        }
    }

    /**
     * Identifica el récord absoluto y muestra los jugadores que lo ostentan.
     * 
     * @return Informe con los jugadores que tienen el récord de victorias.
     */
    public String obtenerJugadoresConRecordDBMS() {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            int record = 0;
            // 1. Obtener el valor del récord mediante función
            try (CallableStatement cstmt = con.prepareCall("{? = call fun_record_absoluto()}")) {
                cstmt.registerOutParameter(1, java.sql.Types.INTEGER);
                cstmt.execute();
                record = cstmt.getInt(1);
            }
            // 2. Ejecutar procedimiento que genera el reporte con ese récord
            try (CallableStatement cstmt = con.prepareCall("begin p_jugadores_con_record(?); end;")) {
                cstmt.setInt(1, record);
                cstmt.execute();
            }
            return leerBufferDBMS(con);
        } catch (SQLException e) {
            return "Error al obtener récords: " + e.getMessage();
        }
    }

    /**
     * Muestra el reporte de jugadores que están por encima de la media de victorias.
     * 
     * @return Informe estadístico DBMS.
     */
    public String obtenerJugadoresEncimaMediaDBMS() {
        try (Connection con = conectarBD()) {
            if (con == null) return "Error de conexión.";
            habilitarDBMS(con);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("begin p_jugadores_encima_media(); end;");
            }
            return leerBufferDBMS(con);
        } catch (SQLException e) {
            return "Error al obtener media: " + e.getMessage();
        }
    }

    /**
     * Calcula en qué percentil se encuentra un jugador basado en sus victorias.
     * 
     * @param victorias Cantidad de victorias a comparar.
     * @return Mensaje descriptivo con el porcentaje de superación.
     */
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
            return "RESULTADO: \n\nTu nivel de victorias (" + victorias + ") supera al " 
                   + String.format("%.2f", pct) + "% del total de jugadores registrados.";
        } catch (SQLException e) {
            return "Error al calcular porcentaje: " + e.getMessage();
        }
    }
}
