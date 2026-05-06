package controlador;

import modelo.Jugador;
import modelo.Foca;
import modelo.Inventario;
import modelo.Tablero;
import modelo.Casilla;
import java.util.ArrayList;
import java.util.Random;

/**
 * Clase que gestiona la lógica de decisión de la Inteligencia Artificial (IA).
 * 
 * <p>
 * Proporciona un sistema de toma de decisiones para la Foca (CPU),
 * permitiéndole
 * analizar el entorno próximo en el tablero y decidir si debe avanzar
 * normalmente,
 * recolectar recursos o utilizar dados especiales para evitar peligros o
 * aprovechar beneficios.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Cpu {

    /**
     * Enumeración de las acciones posibles que puede realizar la CPU.
     */
    public static class Accion {
        /** El jugador lanza un dado estándar de 1-6. */
        public static final int LANZAR_DADO = 0;
        /** El jugador se queda en la casilla actual para buscar bolas de nieve. */
        public static final int RECOLECTAR_BOLAS = 1;
        /** El jugador utiliza un dado rápido (rango 3-8). */
        public static final int USAR_DADO_RAPIDO = 2;
        /** El jugador utiliza un dado lento (rango 1-3). */
        public static final int USAR_DADO_LENTO = 3;
    }

    private static final Random random = new Random();

    /**
     * Motor de decisión de la CPU.
     * 
     * <p>
     * Analiza factores como la cantidad de bolas de nieve, la distancia a la meta
     * y la presencia de obstáculos (agujeros) o ayudas (trineos) en las próximas
     * 6 casillas para determinar la acción más óptima.
     * </p>
     * 
     * @param foca    La instancia de {@link Foca} que está ejecutando el turno.
     * @param tablero El {@link Tablero} de juego para realizar el análisis de
     *                entorno.
     * @return Un entero que representa la {@link Accion} decidida.
     */
    public static int decidirAccion(Foca foca, Tablero tablero) {
        Inventario inv = foca.getInventario();
        int posActual = foca.getPosicion();
        int bolas = inv.getCantidad("BolaNieve");

        // --- 1. PRIORIDAD: RECOLECTAR BOLAS ---
        // Si tiene pocas bolas y no está al final, recolecta (pero solo si no tiene el
        // máximo)
        if (bolas < 10 && posActual < Tablero.TAMANYO_TABLERO - 10) {
            if (bolas < Inventario.MAX_BOLAS_NIEVE) {
                // 70% de probabilidad de recolectar si le faltan bolas
                if (random.nextDouble() < 0.7) {
                    return Accion.RECOLECTAR_BOLAS;
                }
            }
        }

        // --- 2. ANÁLISIS DEL TABLERO PRÓXIMO (6 casillas adelante) ---
        boolean hayAgujeroCerca = false;
        boolean hayTrineoCerca = false;
        int distAgujero = -1;
        int distTrineo = -1;

        for (int i = 1; i <= 6; i++) {
            Casilla c = tablero.getCasilla(posActual + i);
            if (c != null) {
                if (c.getTipo().equals("Casilla AGUJERO") && !hayAgujeroCerca) {
                    hayAgujeroCerca = true;
                    distAgujero = i;
                }
                if (c.getTipo().equals("Casilla TRINEO") && !hayTrineoCerca) {
                    hayTrineoCerca = true;
                    distTrineo = i;
                }
            }
        }

        // --- 3. USO DE DADOS ESPECIALES ---

        // Si hay un trineo muy cerca (1-3), intentar caer en él con dado lento
        if (hayTrineoCerca && distTrineo <= 3 && inv.tieneObjetoEspecifico("Lento")) {
            return Accion.USAR_DADO_LENTO;
        }

        // Si hay un agujero a media distancia (3-5), intentar saltarlo con dado rápido
        if (hayAgujeroCerca && distAgujero >= 3 && distAgujero <= 5 && inv.tieneObjetoEspecifico("Rapido")) {
            return Accion.USAR_DADO_RAPIDO;
        }

        // Si hay un agujero muy cerca (1-2), intentar evitarlo con dado lento (sacar un
        // 3+ para saltar o 1 para quedar antes)
        // O simplemente si el dado rápido nos aleja lo suficiente
        if (hayAgujeroCerca && distAgujero <= 2) {
            if (inv.tieneObjetoEspecifico("Rapido"))
                return Accion.USAR_DADO_RAPIDO;
            if (inv.tieneObjetoEspecifico("Lento"))
                return Accion.USAR_DADO_LENTO;
        }

        // --- 4. DECISIÓN POR DEFECTO ---
        // Si tiene dados rápidos y está lejos de la meta, usarlos a veces para avanzar
        if (inv.tieneObjetoEspecifico("Rapido") && posActual < Tablero.TAMANYO_TABLERO - 15) {
            if (random.nextDouble() < 0.3)
                return Accion.USAR_DADO_RAPIDO;
        }

        return Accion.LANZAR_DADO;
    }
}
