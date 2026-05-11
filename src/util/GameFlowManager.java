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

    /**
     * Constructor del gestor de flujo de juego.
     * 
     * @param tablero   El tablero de juego activo.
     * @param jugadores La lista de jugadores que participan en la partida.
     * @param ui        La interfaz de manejo de eventos visuales y de registro.
     */
    public GameFlowManager(Tablero tablero, List<Jugador> jugadores, GameUIHandler ui) {
        this.tablero = tablero;
        this.jugadores = jugadores;
        this.ui = ui;
    }

    /**
     * Evalúa y aplica las reglas especiales de la casilla donde ha caído el jugador.
     * 
     * @param j El jugador que ha caído en la casilla.
     */
    public void processCellEffects(Jugador j) {
        PauseTransition effectDelay = new PauseTransition(Duration.seconds(1));
        effectDelay.setOnFinished(ev -> {
            int posAntes = j.getPosicion();
            
            // Comprobar si hay colisión inmediata para decidir si sonar la casilla o no
            boolean hayColisionInmediata = false;
            for (Jugador p : jugadores) {
                if (p != j && p.getPosicion() == posAntes) {
                    hayColisionInmediata = true;
                }
            }

            Casilla cActual = tablero.getCasilla(posAntes);
            if (!hayColisionInmediata && cActual != null && cActual.getTipo().toUpperCase().contains("INTERROGANTE")) {
                SoundManager.playInterrogante();
            }

            String logEfecto = tablero.aplicarEfectoCasilla(j);
            
            if (logEfecto != null && !logEfecto.isEmpty()) {
                ui.log(logEfecto.replace("\n", " "));
            }

            int posDespues = j.getPosicion();

            if (posAntes != posDespues) {
                String tipoRaw = tablero.getCasilla(posAntes).getTipo().toUpperCase();
                if (tipoRaw.contains("AGUJERO")) SoundManager.playAgujero();
                else if (tipoRaw.contains("TRINEO")) SoundManager.playTrineo();
                else if (tipoRaw.contains("OSO")) SoundManager.playOso();

                String tipo = tipoRaw.replace("CASILLA ", "");
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
                
                if (tipo.equals("ROMPEDIZAS") || tipo.equals("INTERROGANTE")) {
                    String mensaje = (logEfecto != null && logEfecto.contains(":")) 
                                     ? logEfecto.substring(logEfecto.indexOf(":") + 1).trim() 
                                     : logEfecto;
                    
                    if (tipo.equals("ROMPEDIZAS")) {
                        SoundManager.playRompediza();
                        ui.playShakeAnimation(posAntes, () -> {
                            if (mensaje != null && !mensaje.isEmpty()) {
                                ui.notifyEvent(mensaje, j);
                            }
                            checkCollision(j);
                        });
                    } else {
                        if (mensaje != null && !mensaje.isEmpty()) {
                            ui.notifyEvent(mensaje, j);
                        }
                        checkCollision(j);
                    }
                } else {
                    checkCollision(j);
                }
            }
        });
        effectDelay.play();
    }

    /**
     * Procesa efectos que ocurren mientras un jugador se desplaza (sin detenerse).
     * Especialmente diseñado para el ataque de la Foca al pasar sobre otros pingüinos.
     * 
     * @param mover El jugador que se está moviendo.
     * @param pos   La posición actual del paso de movimiento.
     */
    public void processPassingEffects(Jugador mover, int pos) {
        // Solo la Foca ataca al pasar (golpe y robo de inventario)
        if (mover instanceof Foca && pos > 0 && pos < Tablero.TAMANYO_TABLERO - 1) {
            Foca foca = (Foca) mover;
            for (Jugador p : jugadores) {
                // Solo atacamos a pingüinos humanos (no a otras focas)
                if (!(p instanceof Foca) && p.getPosicion() == pos) {
                    SoundManager.playLatigo();
                    ui.log("¡GOLPE! " + foca.getNombre() + " pasa sobre " + p.getNombre() + " y le roba medio inventario.");
                    ui.notifyEvent("¡PIERDES MEDIO INVENTARIO!", p);
                    foca.atacarJugador(p);
                }
            }
        }
    }

    /**
     * Comprueba si el jugador debe iniciar una batalla (Guerra) tras aplicar los efectos.
     * 
     * @param jActual El jugador que acaba de terminar su movimiento y cuyos efectos se han procesado.
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
                SoundManager.playWar();
                ui.log("¡COLISIÓN en casilla " + jActual.getPosicion() + "!");
                // Eliminamos la notificación efímera para no saturar, ya que viene el diálogo
                resolveCombat(jActual, oponente);
            } else {
                ui.finishTurn();
            }
        }
    }

    /**
     * Ejecuta la resolución de un conflicto entre dos jugadores en la misma casilla.
     * 
     * @param atacante El jugador que llega a la casilla e inicia la colisión.
     * @param defensor El jugador que ya se encontraba en la casilla.
     */
    private void resolveCombat(Jugador atacante, Jugador defensor) {
        boolean esFoca = (atacante instanceof Foca || defensor instanceof Foca);
        String titulo = esFoca ? "ATAQUE FOCA" : "COMBATE";
        StringBuilder mensaje = new StringBuilder();
        
        // Variables para la acción diferida (animación)
        Jugador aMover = null;
        int pDesde = -1;
        int pHasta = -1;

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

                pDesde = humano.getPosicion();
                if (casillaAgujero != -1) {
                    humano.setPosicion(casillaAgujero);
                    mensaje.append(" RETROCEDE A CASILLA ").append(casillaAgujero);
                } else {
                    humano.setPosicion(0);
                    mensaje.append(" RETROCEDE AL INICIO");
                }
                pHasta = humano.getPosicion();
                aMover = humano;
            }
        } else {
            int bolasAtacante = atacante.getInventario().getCantidad("BolaNieve");
            int bolasDefensor = defensor.getInventario().getCantidad("BolaNieve");

            mensaje.append(atacante.getNombre()).append(" (").append(bolasAtacante).append(") VS ")
                    .append(defensor.getNombre()).append(" (").append(bolasDefensor).append(")\n");

            if (bolasAtacante > bolasDefensor) {
                int diff = bolasAtacante - bolasDefensor;
                pDesde = defensor.getPosicion();
                defensor.setPosicion(Math.max(0, pDesde - diff));
                mensaje.append("GANADOR: ").append(atacante.getNombre()).append(". ").append(defensor.getNombre())
                        .append(" RETROCEDE ").append(pDesde - defensor.getPosicion());
                aMover = defensor;
                pHasta = defensor.getPosicion();
            } else if (bolasDefensor > bolasAtacante) {
                int diff = bolasDefensor - bolasAtacante;
                pDesde = atacante.getPosicion();
                atacante.setPosicion(Math.max(0, pDesde - diff));
                mensaje.append("GANADOR: ").append(defensor.getNombre()).append(". ").append(atacante.getNombre())
                        .append(" RETROCEDE ").append(pDesde - atacante.getPosicion());
                aMover = atacante;
                pHasta = atacante.getPosicion();
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
        
        // Si hay movimiento, esperamos a que termine para mostrar el diálogo final
        if (aMover != null) {
            ui.moveTokenDirect(aMover, pDesde, pHasta, () -> {
                ui.showEventDialog(titulo, mensaje.toString(), ui::finishTurn, atacante, defensor);
            });
        } else {
            ui.showEventDialog(titulo, mensaje.toString(), ui::finishTurn, atacante, defensor);
        }
    }
}
