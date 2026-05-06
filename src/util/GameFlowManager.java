package util;

import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import modelo.*;
import java.util.List;

/**
 * Gestor de la lógica de flujo de juego, colisiones y efectos de casillas.
 * 
 * <p>
 * Esta clase extrae la complejidad de las reglas del juego del controlador principal,
 * encargándose de procesar qué ocurre cuando un jugador cae en una casilla y
 * resolviendo los conflictos (guerras) entre jugadores.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class GameFlowManager {

    /**
     * Interfaz para que el gestor pueda comunicarse con la UI del controlador.
     */
    public interface GameUIHandler {
        void log(String msg);
        void notifyEvent(String msg, Jugador j);
        void showEventDialog(String title, String msg, Runnable onComplete, Jugador... players);
        void moveTokenDirect(Jugador j, int from, int to, Runnable onComplete);
        void finishTurn();
        void playShakeAnimation(int position, Runnable onComplete);
    }

    private final Tablero tablero;
    private final List<Jugador> jugadores;
    private final GameUIHandler ui;

    public GameFlowManager(Tablero tablero, List<Jugador> jugadores, GameUIHandler ui) {
        this.tablero = tablero;
        this.jugadores = jugadores;
        this.ui = ui;
    }

    /**
     * Evalúa y aplica las reglas especiales de la casilla donde ha caído el jugador.
     */
    public void processCellEffects(Jugador j) {
        PauseTransition effectDelay = new PauseTransition(Duration.seconds(1));
        effectDelay.setOnFinished(ev -> {
            int posAntes = j.getPosicion();
            String logEfecto = tablero.aplicarEfectoCasilla(j);
            
            if (logEfecto != null && !logEfecto.isEmpty()) {
                ui.log(logEfecto.replace("\n", " "));
            }

            int posDespues = j.getPosicion();

            if (posAntes != posDespues) {
                String tipo = tablero.getCasilla(posAntes).getTipo().replace("Casilla ", "").toUpperCase();
                String msg = "¡" + tipo + "!";
                
                // Si es interrogante, intentamos mostrar el mensaje detallado (ej: Moto de Nieve)
                if (tipo.equals("INTERROGANTE") && logEfecto != null && logEfecto.contains(":")) {
                    msg = logEfecto.substring(logEfecto.indexOf(":") + 1).trim();
                }
                
                ui.notifyEvent(msg, j);
                ui.log("¡EFECTO! " + j.getNombre() + " se mueve a la casilla " + posDespues);
                ui.moveTokenDirect(j, posAntes, posDespues, () -> checkCollision(j));
            } else {
                Casilla c = tablero.getCasilla(posAntes);
                String tipo = c.getTipo().replace("Casilla ", "");
                
                if (tipo.equals("ROMPEDIZAS")) {
                    ui.playShakeAnimation(posAntes, () -> checkCollision(j));
                } else if (tipo.equals("INTERROGANTE") && logEfecto != null && logEfecto.contains(":")) {
                    // Extraer solo la parte del mensaje después del separador :
                    String soloMensaje = logEfecto.substring(logEfecto.indexOf(":") + 1).trim();
                    ui.notifyEvent(soloMensaje, j);
                    checkCollision(j);
                } else {
                    checkCollision(j);
                }
            }
        });
        effectDelay.play();
    }

    /**
     * Comprueba si el jugador debe iniciar una batalla (Guerra) tras aplicar los efectos.
     */
    public void checkCollision(Jugador jActual) {
        if (jActual.getPosicion() <= 0 || jActual.getPosicion() >= Tablero.TAMANYO_TABLERO - 1) {
            ui.finishTurn();
        } else {
            Jugador oponente = null;
            for (Jugador p : jugadores) {
                if (oponente == null && p != jActual && p.getPosicion() == jActual.getPosicion()) {
                    oponente = p;
                }
            }

            if (oponente != null) {
                ui.log("¡COLISIÓN en casilla " + jActual.getPosicion() + "!");
                ui.notifyEvent("¡COLISIÓN!", jActual);
                resolveCombat(jActual, oponente);
            } else {
                ui.finishTurn();
            }
        }
    }

    /**
     * Ejecuta la resolución de un conflicto entre dos jugadores en la misma casilla.
     */
    private void resolveCombat(Jugador atacante, Jugador defensor) {
        boolean esFoca = (atacante instanceof Foca || defensor instanceof Foca);
        String titulo = esFoca ? "ATAQUE FOCA" : "COMBATE";
        StringBuilder mensaje = new StringBuilder();

        if (esFoca) {
            Jugador humano = (atacante instanceof Foca) ? defensor : atacante;
            Foca foca = (Foca) ((atacante instanceof Foca) ? atacante : defensor);

            if (humano.getInventario().tieneObjeto("Pez")) {
                humano.getInventario().usarObjeto("Pez", humano);
                foca.setTurnosBloqueados(foca.getTurnosBloqueados() + 2);
                mensaje.append("DISTRACTOR: PEZ ENTREGADO. FOCA PIERDE 2 TURNOS.");
            } else {
                mensaje.append("SIN PECES: RECIBE ALETAZO.");

                int casillaAgujero = -1;
                for (int i = humano.getPosicion() - 1; i >= 0 && casillaAgujero == -1; i--) {
                    if (tablero.getCasilla(i) != null && tablero.getCasilla(i).getTipo().equals("Casilla AGUJERO")) {
                        casillaAgujero = i;
                    }
                }

                int posAntigua = humano.getPosicion();
                if (casillaAgujero != -1) {
                    humano.setPosicion(casillaAgujero);
                    mensaje.append(" RETROCEDE A CASILLA ").append(casillaAgujero);
                } else {
                    humano.setPosicion(0);
                    mensaje.append(" RETROCEDE AL INICIO");
                }
                ui.moveTokenDirect(humano, posAntigua, humano.getPosicion(), null);
            }
        } else {
            int bolasAtacante = atacante.getInventario().getCantidad("BolaNieve");
            int bolasDefensor = defensor.getInventario().getCantidad("BolaNieve");

            mensaje.append(atacante.getNombre()).append(" (").append(bolasAtacante).append(") VS ")
                    .append(defensor.getNombre()).append(" (").append(bolasDefensor).append(")\n");

            if (bolasAtacante > bolasDefensor) {
                int diff = bolasAtacante - bolasDefensor;
                int posAntigua = defensor.getPosicion();
                defensor.setPosicion(Math.max(0, posAntigua - diff));
                mensaje.append("GANADOR: ").append(atacante.getNombre()).append(". ").append(defensor.getNombre())
                        .append(" RETROCEDE ").append(posAntigua - defensor.getPosicion());
                ui.moveTokenDirect(defensor, posAntigua, defensor.getPosicion(), null);
            } else if (bolasDefensor > bolasAtacante) {
                int diff = bolasDefensor - bolasAtacante;
                int posAntigua = atacante.getPosicion();
                atacante.setPosicion(Math.max(0, posAntigua - diff));
                mensaje.append("GANADOR: ").append(defensor.getNombre()).append(". ").append(atacante.getNombre())
                        .append(" RETROCEDE ").append(posAntigua - atacante.getPosicion());
                ui.moveTokenDirect(atacante, posAntigua, atacante.getPosicion(), null);
            } else {
                mensaje.append("EMPATE: SIN MOVIMIENTO.");
            }

            // Consumir bolas
            while (atacante.getInventario().getCantidad("BolaNieve") > 0)
                atacante.getInventario().eliminarObjeto("BolaNieve");
            while (defensor.getInventario().getCantidad("BolaNieve") > 0)
                defensor.getInventario().eliminarObjeto("BolaNieve");
        }

        ui.log(mensaje.toString().replace("\n", " | "));
        ui.showEventDialog(titulo, mensaje.toString(), ui::finishTurn, atacante, defensor);
    }
}
