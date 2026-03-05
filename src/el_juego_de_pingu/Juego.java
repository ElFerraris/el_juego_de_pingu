package el_juego_de_pingu;

import java.util.ArrayList;

public class Juego extends BBDD{

	private Tablero tablero;
    ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
    private int turnoActual;
    private CPU cpu;
    
    public void iniciarPartida() {
    	
    	System.out.println("--- Iniciando El Juego de Pingu ---");
    	
    	// jugadores.add(new Jugador("P1", "Jugador 1"));
    	this.turnoActual = 0;
    }
    
    public void guardarPartida() {
    	//Base de datos
    }
    
    public void cargarPartida() {
    	//Base de datos
    }
    
    public void cambiarTurno() {
    	
    	// Incrementamos el turno y volvemos a 0 si llegamos al final de la lista
                turnoActual = (turnoActual + 1) % jugadores.size();

        Jugador j = jugadores.get(turnoActual);
        System.out.println("Es el turno de: " + j.getNombre());

        // Si el jugador está bloqueado (por la foca o eventos), salta el turno
        if (j.estaBloqueado()) {
            System.out.println(j.getNombre() + " está bloqueado. Restando turno de bloqueo...");
            j.setTurnosBloqueados(j.getTurnosBloqueados() - 1);
            cambiarTurno();
        }
    	
    	
    }
    
   
    public boolean comprobarGanador() {
    	for (Jugador j : jugadores) {
            if (j.getPosicion() >= 50) { // Casilla meta según el dossier
                System.out.println("¡Victoria! El ganador es: " + j.getNombre());
                return true;
            }
        }
        return false;
    }
    
    
    
}
