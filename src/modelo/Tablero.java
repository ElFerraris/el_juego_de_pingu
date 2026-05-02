package modelo;

import java.util.ArrayList;

/**
 * Representa el mundo del juego, compuesto por una secuencia lineal de
 * casillas.
 * 
 * <p>
 * Gestiona la generación del tablero mediante semillas (seeds), la validación
 * de
 * las mismas, el mantenimiento de las relaciones entre casillas especiales
 * (agujeros que conectan hacia atrás y trineos hacia adelante) y la ejecución
 * de los efectos de las casillas sobre los jugadores.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Tablero {

    /** Número total de casillas que componen el tablero. */
    public static final int TAMANYO_TABLERO = 50;

    /** Colección ordenada de casillas del tablero. */
    private ArrayList<Casilla> casillas = new ArrayList<>();
    /** Semilla actual utilizada para la generación. */
    private String seed = "";
    /** Identificador de la partida vinculada en base de datos. */
    private int idPartida = 0;

    /**
     * Genera una semilla aleatoria que cumple con los requisitos mínimos de
     * jugabilidad.
     * 
     * <p>
     * Una semilla válida debe contener al menos 2 agujeros y 2 trineos para
     * asegurar
     * dinamismo en la partida.
     * </p>
     * 
     * @return Una cadena de dígitos que representa la configuración del tablero.
     */
    public static String generarSeedValida() {
        String genSeed;
        Tablero temp = new Tablero();
        do {
            genSeed = "";
            for (int i = 0; i < TAMANYO_TABLERO - 2; i++) {
                int numseed = (int) (Math.random() * 10);
                genSeed += numseed;
            }
        } while (!temp.validarSeed(genSeed));
        return genSeed;
    }

    /**
     * Genera un tablero completo utilizando una semilla aleatoria válida.
     */
    public void generarSeedAleatoria() {
        String genSeed = generarSeedValida();
        generarTablero(genSeed);
    }

    /**
     * Obtiene la semilla actual del tablero.
     * 
     * @return Cadena con la semilla.
     */
    public String getSeed() {
        return seed;
    }

    /**
     * Inicializa el tablero basándose en una semilla proporcionada.
     * 
     * @param seed Cadena de configuración o palabra clave.
     */
    public void introducirSeed(String seed) {
        if (validarSeed(seed)) {
            generarTablero(seed);
        } else {
            System.out.println("Error: la seed no es válida.");
        }
    }

    /**
     * Verifica si una semilla cumple con los criterios de formato y contenido.
     * 
     * <p>
     * Admite tanto secuencias numéricas (donde 2 es Agujero y 3 es Trineo)
     * como palabras clave de depuración ("normal", "oso", "random", etc.).
     * </p>
     * 
     * @param seed La semilla a validar.
     * @return {@code true} si es apta para generar un tablero; {@code false} en
     *         caso contrario.
     */
    public boolean validarSeed(String seed) {
        if (seed == null)
            return false;

        // Soporte para palabras clave (Cheat Codes)
        String s = seed.toLowerCase().trim();
        if (s.equals("normal") || s.equals("oso") || s.equals("agujero") ||
                s.equals("trineo") || s.equals("interrogante") || s.equals("rompedizas") || s.equals("random")) {
            return true;
        }

        int contadorAgujero = 0;
        int contadorTrineo = 0;

        for (int i = 0; i < seed.length(); i++) {
            if (seed.charAt(i) == '2') {
                contadorAgujero++;
            } else if (seed.charAt(i) == '3') {
                contadorTrineo++;
            }
        }

        return contadorAgujero >= 2 && contadorTrineo >= 2;
    }

    /**
     * Construye la estructura de casillas del tablero.
     * 
     * <p>
     * Procesa la semilla para instanciar los tipos de casilla correspondientes,
     * asegura que la primera y última posición sean normales, y enlaza
     * lógicamente los elementos de transporte (trineos y agujeros).
     * </p>
     * 
     * @param seed Semilla de generación.
     */
    public void generarTablero(String seed) {
        this.seed = seed;
        casillas.clear();

        String s = seed.toLowerCase().trim();

        // Primera casilla siempre normal
        casillas.add(new CasillaNormal(0, "Casilla NORMAL"));

        if (s.equals("normal") || s.equals("oso") || s.equals("agujero") ||
                s.equals("trineo") || s.equals("interrogante") || s.equals("rompedizas") || s.equals("random")) {

            for (int i = 1; i < TAMANYO_TABLERO - 1; i++) {
                char c = '0'; // Default normal
                if (s.equals("oso"))
                    c = '1';
                else if (s.equals("agujero"))
                    c = '2';
                else if (s.equals("trineo"))
                    c = '3';
                else if (s.equals("interrogante"))
                    c = '4';
                else if (s.equals("rompedizas"))
                    c = '5';
                else if (s.equals("random"))
                    c = (char) ('0' + (int) (Math.random() * 6));

                agregarCasillaPorTipo(i, c);
            }
        } else {
            // Generación normal por números
            for (int i = 1; i < TAMANYO_TABLERO - 1; i++) {
                char c = (i - 1 < seed.length()) ? seed.charAt(i - 1) : '0';
                agregarCasillaPorTipo(i, c);
            }
        }

        // Última casilla siempre normal
        casillas.add(new CasillaNormal(TAMANYO_TABLERO - 1, "Casilla NORMAL"));

        configurarTrineosYAgujeros();
        imprimirTablero();
    }

    /**
     * Instancia una casilla específica según el carácter de tipo.
     * 
     * @param i    Índice de la casilla.
     * @param tipo Carácter identificador ('1' Oso, '2' Agujero, etc.).
     */
    private void agregarCasillaPorTipo(int i, char tipo) {
        switch (tipo) {
            case '1':
                casillas.add(new CasillaOso(i, "Casilla OSO"));
                break;
            case '2':
                casillas.add(new CasillaAgujero(i, "Casilla AGUJERO", 0));
                break;
            case '3':
                casillas.add(new CasillaTrineo(i, "Casilla TRINEO", 0));
                break;
            case '4':
                casillas.add(new CasillaInterrogante(i, "Casilla INTERROGANTE", this));
                break;
            case '5':
                casillas.add(new CasillaRompedizas(i, "Casilla ROMPEDIZAS"));
                break;
            default:
                casillas.add(new CasillaNormal(i, "Casilla NORMAL"));
                break;
        }
    }

    /**
     * Establece los enlaces lógicos de los trineos y agujeros.
     * Los trineos apuntan al siguiente trineo y los agujeros al anterior.
     */
    private void configurarTrineosYAgujeros() {
        // Determinar siguiente casilla de cada trineo
        for (int i = 1; i < TAMANYO_TABLERO - 1; i++) {
            if (casillas.get(i).getTipo().equals("Casilla TRINEO")) {
                boolean trineoSiguienteEncontrado = false;
                for (int y = i + 1; y < TAMANYO_TABLERO - 1 && !trineoSiguienteEncontrado; y++) {
                    if (casillas.get(y).getTipo().equals("Casilla TRINEO")) {
                        casillas.set(i, new CasillaTrineo(i, "Casilla TRINEO", y));
                        trineoSiguienteEncontrado = true;
                    }
                }
            }
            // Si el trineo no tiene siguiente, apunta a sí mismo
            if (casillas.get(i).getTipo().equals("Casilla TRINEO")
                    && ((CasillaTrineo) casillas.get(i)).getPosicionSiguienteTrineo() == 0) {
                casillas.set(i, new CasillaTrineo(i, "Casilla TRINEO", i));
            }
        }

        // Determinar agujero anterior de cada agujero
        for (int i = TAMANYO_TABLERO - 1; i > 0; i--) {
            if (casillas.get(i).getTipo().equals("Casilla AGUJERO")) {
                boolean agujeroAnteriorEncontrado = false;
                for (int y = i - 1; y > 0 && !agujeroAnteriorEncontrado; y--) {
                    if (casillas.get(y).getTipo().equals("Casilla AGUJERO")) {
                        casillas.set(i, new CasillaAgujero(i, "Casilla AGUJERO", y));
                        agujeroAnteriorEncontrado = true;
                    }
                }
            }
        }
    }

    /**
     * Vuelca la configuración del tablero por consola (Debug).
     */
    private void imprimirTablero() {
        for (int i = 0; i < TAMANYO_TABLERO; i++) {
            Casilla casilla = casillas.get(i);
            if (casilla.getTipo().equals("Casilla TRINEO")) {
                System.out.println(
                        i + " " + casilla.getTipo() + " → " + ((CasillaTrineo) casilla).getPosicionSiguienteTrineo());
            } else if (casilla.getTipo().equals("Casilla AGUJERO")) {
                System.out.println(i + " " + casilla.getTipo() + " ← "
                        + ((CasillaAgujero) casilla).getPosicionAgujeroAnterior());
            } else {
                System.out.println(i + " " + casilla.getTipo());
            }
        }
    }

    /**
     * Activa el comportamiento especial de la casilla donde ha aterrizado un
     * jugador.
     * 
     * @param jugador El {@link Jugador} que activa el efecto.
     * @return Un mensaje descriptivo del resultado del efecto para el log del
     *         juego.
     */
    public String aplicarEfectoCasilla(Jugador jugador) {
        int pos = jugador.getPosicion();

        if (pos >= 0 && pos < casillas.size()) {
            Casilla casillaActual = casillas.get(pos);
            String tipo = casillaActual.getTipo().replace("Casilla ", "").toUpperCase();
            String efectoTxt = casillaActual.activarEfecto(jugador);

            if (efectoTxt != null && !efectoTxt.isEmpty()) {
                String logMsg = tipo + ": " + efectoTxt;
                System.out.println(logMsg);
                return logMsg;
            } else {
                return "";
            }
        }
        return "";
    }

    /**
     * Obtiene el objeto casilla en un índice específico.
     * 
     * @param posicion Índice de la casilla (0 a {@link #TAMANYO_TABLERO}-1).
     * @return La instancia de {@link Casilla}, o {@code null} si está fuera de
     *         rango.
     */
    public Casilla getCasilla(int posicion) {
        if (posicion >= 0 && posicion < casillas.size()) {
            return casillas.get(posicion);
        }
        return null;
    }

    /**
     * Obtiene el identificador de la partida en base de datos.
     * 
     * @return El ID de la partida.
     */
    public int getIdPartida() {
        return idPartida;
    }

    /**
     * Asocia el tablero con un ID de partida de la base de datos.
     * 
     * @param idPartida El nuevo identificador.
     */
    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }
}
