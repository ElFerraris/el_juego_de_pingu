package el_juego_de_pingu;

import java.util.ArrayList;

public class Juego extends BBDD{

	private Tablero tablero;
    ArrayList<Jugador> jugadores = new ArrayList<Jugador>();
    private int turnoActual;
    private boolean partidaFinalizada;
    private Jugador ganador;
    
    public Juego() {
        this.tablero = new Tablero(); 
        this.jugadores = new ArrayList<>();
        this.partidaFinalizada =false;
    }
    
    public void iniciarPartida() {
    	
    	System.out.println("--- Iniciando El Juego de Pingu ---");
    	

    	this.turnoActual = 0;
    }
    
    public void guardarPartida() {
    	//Base de datos
    }
    
    public void cargarPartida() {
    	//Base de datos
    }
    
    
    public int getTurnoActual() {
		return turnoActual;
	}

	public void setTurnoActual(int turnoActual) {
		this.turnoActual = turnoActual;
	}
	
	
	
/*
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
    */
	
	public Tablero getTablero() {
		return tablero;
	}

	public void setTablero(Tablero tablero) {
		this.tablero = tablero;
	}

	public boolean cambiarTurno() {
	    boolean finRonda = false;

	    // Si el turno actual es el del último jugador de la lista, la ronda va a acabar
	    if (turnoActual == jugadores.size() - 1) {
	        finRonda = true;
	    }

	    // Tu lógica original con el módulo % (mantiene el turno entre 0 y el máximo)
	    turnoActual = (turnoActual + 1) % jugadores.size();

	    Jugador j = jugadores.get(turnoActual);
	    
	    // Si el jugador está bloqueado, restamos turno y saltamos al siguiente recursivamente
	    if (j.estaBloqueado()) {
	        System.out.println(j.getNombre() + " está bloqueado. Turnos restantes: " + (j.getTurnosBloqueados() - 1));
	        j.setTurnosBloqueados(j.getTurnosBloqueados() - 1);
	        return cambiarTurno(); // Recursividad: pasamos al siguiente
	    }

	    System.out.println("Es el turno de: " + j.getNombre());
	    return finRonda; 
	}
	
   
    public boolean comprobarGanador() {
    	for (Jugador j : jugadores) {
            if (j.getPosicion() >= 50) { // Casilla meta según el dossier
                System.out.println("¡Victoria! El ganador es: " + j.getNombre());
                partidaFinalizada = true;
                ganador = j;
                return true;
            }
        }
        return false;
    }
    
    
    public void comprobarGuerra(Jugador jugadorActual) {
        for (Jugador oponente : jugadores) {
            // 1. No pelear contra uno mismo y que estén en la misma casilla
            if (!oponente.equals(jugadorActual) && oponente.getPosicion() == jugadorActual.getPosicion() && jugadorActual.getPosicion() != 0) {
                
                System.out.println("\n¡COINCIDENCIA EN CASILLA " + jugadorActual.getPosicion() + "!");

                // 2. ¿Uno de los dos es la Foca (CPU)?
                if (jugadorActual instanceof CPU || oponente instanceof CPU) {
                    
                    // Buscamos quién es el pingüino humano
                    Jugador humano = (jugadorActual instanceof CPU) ? oponente : jugadorActual;
                    
                    System.out.println("¡La Foca te ha visto!");

                    // Regla del Pez: Si el humano tiene pez, bloquea a la foca
                    if (humano.getInventario().tieneObjeto("Pez")) {
                        System.out.println(humano.getNombre() + " usa un Pez para distraer a la foca. ¡Foca bloqueada 2 turnos!");
                        humano.getInventario().usarObjeto("Pez", humano);
                        
                        // Bloqueamos a la foca (buscándola en la lista)
                        if (jugadorActual instanceof CPU) jugadorActual.setTurnosBloqueados(2);
                        else oponente.setTurnosBloqueados(2);
                        
                    } else {
                        // Si no hay pez, golpe de cola y al forat anterior
                        System.out.println("¡ZAS! Golpe de cola. " + humano.getNombre() + " vuelve al forat anterior.");
                        // Aquí llamarías a: tablero.irAlForatAnterior(humano);
                    }

                } else {
                    // 3. Guerra normal entre humanos
                    System.out.println("¡Guerra de bolas de nieve entre " + jugadorActual.getNombre() + " y " + oponente.getNombre() + "!");
                    Guerra combate = new Guerra();
                    combate.iniciarGuerra(jugadorActual, oponente);
                }

                return; // Finaliza el método porque ya hemos resuelto el conflicto de esta casilla
            }
        }
    }
    
    
}