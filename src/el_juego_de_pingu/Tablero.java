package el_juego_de_pingu;

import java.util.ArrayList;

public class Tablero extends Juego{
	
	public static void main(String[] args) {
		
		Tablero t1 = new Tablero();
		
		t1.generarSeedAleatorioa();
	}
	
	
	
	private int midaTablero = 50;
    ArrayList<Casilla> casillas = new ArrayList<Casilla>();
    
	
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
    
    
    
    public void introducirSeed(String seed) {
    	
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
    
	public void generarTablero(String seed) {
		
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

		
		/*Bucle generación tablero
		for (int i = 1; i < midaTablero - 1; i++) {
			int aleatorio = (int)(Math.random() * 100) + 1;
			
			if (aleatorio >= 1 && aleatorio <= 40) {
				casillas.add(new CasillaNormal(i, "Casilla NORMAL"));
			} else if (aleatorio >= 41 && aleatorio <= 50) {
				casillas.add(new CasillaOso(i, "Casilla OSO"));
			} else if (aleatorio >= 51 && aleatorio <= 60) {
				casillas.add(new CasillaAgujero(i, "Casilla AGUJERO", 0)); 
				HACER FORMULA PARA SABER LA CASILLA AGUJERO ANTERIOR
				SI NO HAY CASILLA ANTEIROR VUELVE AL PRINCIPIO
			} else if (aleatorio >= 61 && aleatorio <= 75) {
				casillas.add(new CasillaTrineo(i, "Casilla TRINEO", 0));
				HACER FORMULA PARA SABER LA CASILLA TRINEO SIGUIENTE
			} else if (aleatorio >= 76 && aleatorio <= 90) {
				casillas.add(new CasillaInterrogante(i, "Casilla INTERROGANTE"));
			} else if (aleatorio >= 90 && aleatorio <= 100) {
				casillas.add(new CasillaRompedizas(i, "Casilla ROMPEDIZAS"));
			} else {
				System.out.println("ERROR AL GENERAR TABLERO");
			}
			
			System.out.println((i + 1 )+ " TIPO: " + casillas.get(i).tipo); //PRINT PRUEBA
		}*/
		
		
	
		
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
		}
		
		
		
		
		
	
		
		
		
		
		//IMPRESION CASILLAS
		for (int i = 0; i < midaTablero; i++) {
			System.out.println(i + " " + casillas.get(i).tipo);
			}
		}
			
		
		
	
	
	
	
	
	
	
	/*public Casilla getCasilla(int posicion) {
		
	}
	*/
	
	public void moverJugador(Jugador jugador, int posicion) {
		
	}
	
}
