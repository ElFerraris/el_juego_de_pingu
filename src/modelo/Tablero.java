package modelo;

import java.util.ArrayList;

/**
 * Representa el tablero del juego con sus casillas.
 */
public class Tablero {

    public static final int TAMANYO_TABLERO = 50;

    private ArrayList<Casilla> casillas = new ArrayList<>();
    private String seed = "";
    private int idPartida = 0;
 


    /**
     * Genera una seed aleatoria válida y la devuelve como String.
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

    public void generarSeedAleatoria() {
        String genSeed = generarSeedValida();
        generarTablero(genSeed);
    }

    public String getSeed() {
        return seed;
    }

    /**
     * Introduce una seed manual y genera el tablero si es válida.
     */
    public void introducirSeed(String seed) {
        if (validarSeed(seed)) {
            generarTablero(seed);
        } else {
            System.out.println("Error: la seed no es válida.");
        }
    }

    /**
     * Valida que la seed tenga al menos 2 agujeros y 2 trineos, o sea una palabra clave.
     */
    public boolean validarSeed(String seed) {
        if (seed == null) return false;
        
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
     * Genera el tablero a partir de una seed o palabra clave.
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
                if (s.equals("oso")) c = '1';
                else if (s.equals("agujero")) c = '2';
                else if (s.equals("trineo")) c = '3';
                else if (s.equals("interrogante")) c = '4';
                else if (s.equals("rompedizas")) c = '5';
                else if (s.equals("random")) c = (char) ('0' + (int)(Math.random() * 6));
                
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
     * Aplica el efecto de la casilla donde se encuentra el jugador.
     */
    public String aplicarEfectoCasilla(Jugador jugador) {
        int pos = jugador.getPosicion();

        if (pos >= 0 && pos < casillas.size()) {
            Casilla casillaActual = casillas.get(pos);
            String txtTop = "--- EFECTO: " + jugador.getNombre() + " cae en " + casillaActual.getTipo().replace("Casilla ", "") + " ---";

            String efectoTxt = casillaActual.activarEfecto(jugador);

            if (efectoTxt != null && !efectoTxt.isEmpty()) {
                System.out.println(txtTop + "\n  » " + efectoTxt);
                return txtTop + "\n  » " + efectoTxt;
            } else {
                return ""; // Si es normal, no ensuciamos el log visual
            }
        }
        return "";
    }

    /**
     * Devuelve la casilla en la posición indicada.
     */
    public Casilla getCasilla(int posicion) {
        if (posicion >= 0 && posicion < casillas.size()) {
            return casillas.get(posicion);
        }
        return null;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
    }
}
