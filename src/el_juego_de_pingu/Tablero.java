package el_juego_de_pingu;

import java.util.ArrayList;

public class Tablero{
	
	private int midaTablero = 50;
    private ArrayList<Casilla> casillas = new ArrayList<Casilla>();
    private String seed =""; 
	
    //GENERAR SEED ALEATORIA
    public void generarSeedAleatorioa() {
		String genSeed = "";
		
    	do {
    		genSeed = "";
        	for (int i = 0; i < 48; i++) {
        		int numseed = (int)(Math.random() * 10);
        		genSeed += numseed;
        	}
        	System.out.println("Seed generada: " + genSeed);
    	} while (validarSeed(genSeed) == false);
  
    	
    	generarTablero(genSeed);
    }
    
    // OBTENER SEED DE PARTIDA
    public String getSeed() {
		return seed;
	}


	//INTRODUCIR SEED PARA GENERAR
    public void introducirSeed(String seed) {
    	if (validarSeed(seed)) {
    		generarTablero(seed);
    	} else {
    		System.out.println("Error con esta seed");
    	}
    }
    
    
    
    //VALIDACIÓN SEED, COMPRUEBA QUE TENGA MINIMO 2 AGUJEROS Y 2 TRINEOS
    public boolean validarSeed(String seed) {
    	int contadorAgujero = 0;
    	int contadorTrineo = 0;
    	for (int i = 0; i < seed.length(); i++) {
    		if (seed.charAt(i) == '2') {
    			contadorAgujero ++;
    		} else if (seed.charAt(i) == '3') {
    			contadorTrineo ++;
    		}
     	}
    	
    	if (contadorAgujero >= 2 && contadorTrineo >= 2) {
        	return true;
    	} else {
    		return false;
    	}
    }
    
    
    
    //GENERACIO TABLERO
	public void generarTablero(String seed) {
		this.seed = seed;
		casillas.add(new CasillaNormal(0, "Casilla NORMAL"));	//Generar primera casilla
		//Generación tablero en base a seed
		for (int i = 1; i < midaTablero - 1; i++) {
			switch (seed.charAt(i - 1)) {
				case '1':
					casillas.add(new CasillaOso(i, "Casilla OSO"));
					break;
				case '2':
					casillas.add(new CasillaAgujero(i, "Casilla AGUJERO", 0)); 
					/*HACER FORMULA PARA SABER LA CASILLA AGUJERO ANTERIOR
					SI NO HAY CASILLA ANTEIROR VUELVE AL PRINCIPIO*/
					break;
				case '3':
					casillas.add(new CasillaTrineo(i, "Casilla TRINEO", 0));
					/*HACER FORMULA PARA SABER LA CASILLA TRINEO SIGUIENTE*/
					break;
				case '4':
					casillas.add(new CasillaInterrogante(i, "Casilla INTERROGANTE"));
					break;
				case '5':				
					casillas.add(new CasillaRompedizas(i, "Casilla ROMPEDIZAS"));
					break;
				default:
					casillas.add(new CasillaNormal(i, "Casilla NORMAL"));
					break;		
			}
		}
		casillas.add(new CasillaNormal(midaTablero - 1, "Casilla NORMAL")); //Generar ultima casilla	

	
		
		
		
		
		//DETERMINAR SIGUIENTE CASILLA DE CADA TRINEO
		for (int i = 1; i < midaTablero - 1; i++) {
			if (casillas.get(i).tipo.equals("Casilla TRINEO")) {
				for (int y = i + 1; y < midaTablero - 1; y++) {
					if(casillas.get(y).tipo.equals("Casilla TRINEO")) {
						casillas.set(i, new CasillaTrineo(i, "Casilla TRINEO", y));
						y = midaTablero; //Como un brake, hace que salga del bucle y que vaya a buscar el siguiente trineo
					}
				}
			}
			if (casillas.get(i).tipo.equals("Casilla TRINEO") && ((CasillaTrineo) casillas.get(i)).getPosicionSiguienteTrineo() == 0) {
				casillas.set(i, new CasillaTrineo(i, "Casilla TRINEO", i));
			}
		}

		
		
		//DETERMINAR AGUJERO ANTERIOR DE CADA AGUJERO
		for (int i = midaTablero - 1; i > 0; i--) {
			if (casillas.get(i).tipo.equals("Casilla AGUJERO")) {
				for (int y = i - 1; y > 0; y--) {
					if(casillas.get(y).tipo.equals("Casilla AGUJERO")) {
						casillas.set(i, new CasillaAgujero(i, "Casilla AGUJERO", y));
						y = -1; //Como un brake, hace que salga del bucle y que vaya a buscar el siguiente trineo
					}
				}
			}
		}
		
		

		
		
		
		//IMPRESION CASILLAS
		for (int i = 0; i < midaTablero; i++) {
			if (casillas.get(i).tipo.equals("Casilla TRINEO")) {
				System.out.println(i + " " + casillas.get(i).tipo + " " + ((CasillaTrineo) casillas.get(i)).getPosicionSiguienteTrineo());
			} else if (casillas.get(i).tipo.equals("Casilla AGUJERO")) {
				System.out.println(i + " " + casillas.get(i).tipo + " " + ((CasillaAgujero) casillas.get(i)).getPosicionAgujeroAnterior());
			} else {
				System.out.println(i + " " + casillas.get(i).tipo);
			}
			}
		}
			
		
		
	
	
	/**
	 * Aplica el efecto de la casilla donde se encuentra actualmente el jugador.
	 * @param jugador El jugador que acaba de caer en la casilla.
	 */
	public void aplicarEfectoCasilla(Jugador jugador) {
	    int pos = jugador.getPosicion();
	    
	    // Verificamos que la posición sea válida dentro del tablero
	    if (pos >= 0 && pos < casillas.size()) {
	        Casilla casillaActual = casillas.get(pos);
	        
	        System.out.println("--- EFECTO: " + jugador.getNombre() + " ha caído en " + casillaActual.tipo + " ---");
	        
	        // Polimorfismo en acción: llama al activarEfecto específico de cada subclase
	        casillaActual.activarEfecto(jugador);
	    }
	}
	
	
	
	
	public Casilla getCasilla(int posicion) {
	    if (posicion >= 0 && posicion < casillas.size()) {
	        return casillas.get(posicion);
	    }
	    return null;
	}
	

	
}