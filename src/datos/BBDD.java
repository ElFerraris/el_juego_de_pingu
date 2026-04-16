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
 * Clase para gestionar la conexión y operaciones con la Base de Datos.
 */
public class BBDD {
    // Gestor de conexión principal para el juego de pingüinos

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
                // System.out.println("►Conexión establecida con BBDD!");
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
	
	
	
    public int guardarNuevaPartida(Juego juego) {
    	// 1. Intentamos conectar. Al ponerlo en el paréntesis del try,
        // se cerrará solo al llegar a la llave final }.
        try (Connection con = conectarBD()) { 
            
            if (con == null) return 0;

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
            System.out.println("► ERROR en BBDD: " + e.getMessage());
            return 0;
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
            
            // 3. Es_CPU (Comprobamos si es instancia de la clase Foca)
            int esCpu = (j instanceof Foca) ? 1 : 0;
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
    public int registrarJugadorSiNoExiste(Jugador j) {
        int idJugador = -1; // Valor por defecto si falla

        try (Connection con = conectarBD()) {
            if (con == null) return -1;

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
                            if (rs2.next()) idJugador = rs2.getInt("id_jugador");
                        }
                    }
                    System.out.println("► Nuevo jugador registrado: " + j.getNombre() + " (ID: " + idJugador + ")");
                }
            } else {
                System.out.println("► El jugador " + j.getNombre() + " ya está en la BD con ID: " + idJugador);
            }
            
            return idJugador; // Devolvemos el ID real de la base de datos

        } catch (SQLException e) {
            System.out.println("► ERROR en registro de jugador: " + e.getMessage());
            return -1;
        }
    }
    
    /*
    public boolean ActualizarPartida(Juego juego) {
    	// 1. Intentamos conectar. Al ponerlo en el paréntesis del try,
        // se cerrará solo al llegar a la llave final }.
        try (Connection con = conectarBD()) { 
            
            if (con == null) return false;

            // 2. Preparamos la llamada al procedimiento de Oracle
            String sql = "{call actualizar_partida(?, ?, ?)}"; 

            try (CallableStatement cstmt = con.prepareCall(sql)) {
            	// 1. p_num_partida: El ID de la partida que estamos jugando
            	cstmt.setInt(1, juego.getTablero().getIdPartida());	  
            	
            	// 2. p_torn_actual: El ID del jugador que tiene el turno
            	cstmt.setInt(2, juego.getTurnoActual());
                
                // 3. p_ganador: El ID del ganador (si no hay, pasamos un valor nulo o 0)
                if (juego.getGanador() != null) {
                    cstmt.setInt(3, 1); // Aquí iría el ID real del ganador
                } else {
                    cstmt.setNull(3, java.sql.Types.INTEGER);
                }
                
                cstmt.execute();
                
                // Recuperamos el ID que nos dio la función
                System.out.println("► Partida " + juego.getTablero().getIdPartida() + " actualizada en Oracle.");
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("► ERROR al actualizar partida: " + e.getMessage());
            return false;
        }
    }*/
    
    
    
    
    
    /**
     * Registra que un jugador específico está participando en una partida concreta.
     */
    public boolean insertarParticipacion(int idPartida, int idJugador, String color) {
        // Llamada al procedimiento con los 2 parámetros de entrada (IN)
        String sql = "{call insertar_participacion(?, ?, ?)}";
        
        try (Connection con = conectarBD()) { 
            
            if (con == null) return false;
            
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
            System.out.println("► ERROR en insertarParticipacion: " + e.getMessage());
            return false;
        }
    }
    
    
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
    
    
    public boolean actualizarParticipacion(int idPartida, Jugador j) {
        String sql = "{call actualizar_participacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null) return false;

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
            System.out.println("► ERROR al actualizar participación de " + j.getNombre() + ": " + e.getMessage());
            return false;
        }
    }
    
    public boolean actualizarEstadoPartida(int idPartida, Juego juego) {
        String sql = "{call actualizar_partida(?, ?, ?)}";

        try (Connection con = conectarBD()) {
            if (con == null) return false;

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
            System.out.println("► ERROR al actualizar estado de partida: " + e.getMessage());
            return false;
        }
    }
    
    public boolean guardarEstadoCompleto(int idPartida, Juego juego) {
        String sqlPartida = "{call actualizar_partida(?, ?, ?)}";
        String sqlJugador = "{call actualizar_participacion(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection con = conectarBD()) {
            if (con == null) return false;
            
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
            System.out.println("► ERROR al guardar estado completo: " + e.getMessage());
            return false;
        }
    }
    
    public boolean cargarDatosPartida(int idPartida, Juego juego) {
        String sqlPartida = "SELECT seed, torn_actual FROM partida WHERE num_partida = ?";
        String sqlJugadores = "SELECT j.id_jugador, j.nickname, j.es_cpu, p.posicion_actual, p.color, " +
                              "p.num_peces, p.num_bolas_nieve, p.num_dados_lentos, p.num_dados_rapidos, p.turnos_bloqueado " +
                              "FROM participacion_jugadores p " +
                              "JOIN jugador j ON p.id_jugador = j.id_jugador " +
                              "WHERE p.id_partida = ?"+
                              "ORDER BY j.id_jugador ASC";

        try (Connection con = conectarBD()) {
            if (con == null) return false;

            // 1. Cargar datos generales de la partida
            try (PreparedStatement pstmt = con.prepareStatement(sqlPartida)) {
                pstmt.setInt(1, idPartida);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        juego.getTablero().introducirSeed(rs.getString("seed"));
                        juego.setTurnoActual(rs.getInt(("torn_actual")));
                        juego.getTablero().setIdPartida(idPartida);
                        // El turno lo setearemos después de cargar los jugadores para evitar errores de índice
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
                        nuevo.getInventario().agregarObjetos("Pez",rs.getInt("num_peces"));
                        nuevo.getInventario().agregarObjetos("BolaNieve",rs.getInt("num_bolas_nieve"));
                        nuevo.getInventario().agregarObjetos("DadoLento",rs.getInt("num_dados_lentos"));
                        nuevo.getInventario().agregarObjetos("DadoRapido",rs.getInt("num_dados_rapidos"));                        
                        juego.agregarJugador(nuevo);
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("► ERROR al cargar: " + e.getMessage());
            return false;
        }
    }
    
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
            System.out.println("► ERROR al listar partidas: " + e.getMessage());
        }
    }
    
    public ArrayList<modelo.PartidaGuardada> obtenerPartidasPendientes() {
        // Usamos LinkedHashMap para mantener el orden de la consulta (ORDER BY hora_partida DESC)
        Map<Integer, modelo.PartidaGuardada> mapa = new LinkedHashMap<>();
        
        String sql = "SELECT p.num_partida, p.seed, p.nombre, " +
                     "TO_CHAR(p.hora_partida, 'DD/MM/YYYY HH24:MI') as hora_partida, " +
                     "pj.color " +
                     "FROM partida p " +
                     "LEFT JOIN participacion_jugadores pj ON p.num_partida = pj.id_partida " +
                     "WHERE p.ganador IS NULL " +
                     "ORDER BY p.hora_partida DESC";

        try (Connection con = conectarBD();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("num_partida");
                String color = rs.getString("color");
                
                if (!mapa.containsKey(id)) {
                    List<String> colores = new ArrayList<>();
                    if (color != null) colores.add(color);
                    
                    mapa.put(id, new modelo.PartidaGuardada(
                        id,
                        rs.getString("seed"),
                        rs.getString("hora_partida"),
                        rs.getString("nombre"),
                        colores
                    ));
                } else {
                    if (color != null) {
                        mapa.get(id).getColoresJugadores().add(color);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("► ERROR al obtener lista partidas pendientes: " + e.getMessage());
        }
        return new ArrayList<>(mapa.values());
    }
    
    public void mostrarRankingMasPartidas() {
        // Consulta: Unimos jugador con participación y contamos cuántas veces aparece cada uno
        String sql = "SELECT j.nickname, COUNT(p.id_partida) AS total_partidas " +
                     "FROM jugador j " +
                     "JOIN participacion_jugadores p ON j.id_jugador = p.id_jugador " +
                     "WHERE j.nickname != 'Foca Loca'"+ //Quitamos la Foca
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
            System.out.println("► ERROR al generar ranking: " + e.getMessage());
        }
    }
    
    
    
    /**
     * Comprueba si el nickname y la contraseña coinciden en la base de datos.
     * @param nickname El nombre del jugador.
     * @param password La contraseña introducida.
     * @return true si las credenciales son correctas, false en caso contrario.
     */
    public static boolean loginJugador(String nickname, String password) {
        // Consulta para contar si hay un usuario con ese nombre Y esa contraseña
        String sql = "SELECT COUNT(*) FROM jugador WHERE nickname = ? AND CONTRASENA = ?";

        try (Connection con = conectarBD();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            
            if (con == null) return false;

            // Seteamos los parámetros
            pstmt.setString(1, nickname);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Si el conteo es mayor que 0, es que existe y coincide
                    int coincidencias = rs.getInt(1);
                    if (coincidencias > 0) {
                        System.out.println("► Login correcto para: " + nickname);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("► ERROR en el login: " + e.getMessage());
        }

        System.out.println("► Nickname o contraseña incorrectos.");
        return false;
    }
    
    
    /**
     * Inserta un nuevo jugador en la tabla con su contraseña.
     * @return El ID generado por Oracle, o -1 si hubo un error (ej: el nombre ya existe).
     */
    public static int registrarNuevoJugador(String nickname, String password, boolean esCpu) {
        String sql = "{call insertar_jugador(?, ?, ?)}";
        
        try (Connection con = conectarBD()) {
            if (con == null) return -1;

            try (CallableStatement cstmt = con.prepareCall(sql)) {
                cstmt.setString(1, nickname);
                cstmt.setString(2, password);
                cstmt.setInt(3, esCpu ? 1 : 0);
                
                cstmt.execute();
                
                // Una vez insertado, recuperamos el ID que le ha tocado
                return obtenerIdJugador(con, nickname); 
            }
        } catch (SQLException e) {
            System.out.println("► ERROR: El nombre '" + nickname + "' ya está ocupado.");
            return -1;
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
            System.out.println("► ERROR al listar jugadores: " + e.getMessage());
        }
        return jugadores;
    }
    
    /**
     * Elimina una partida de la base de datos. 
     * Gracias al ON DELETE CASCADE en Oracle, las participaciones se borran solas.
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
                    System.out.println("► Partida " + idPartida + " eliminada.");
                    return true;
                } else {
                    return false;
                }
            }
            
        } catch (SQLException e) {
            System.out.println("► ERROR al eliminar partida: " + e.getMessage());
            return false;
        }
    }

}
