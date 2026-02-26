package el_juego_de_pingu;

import java.util.ArrayList;

public class Tablero extends Juego{
	
	private int midaTablero = 50;
    ArrayList<Casilla> casillas = new ArrayList<Casilla>();
    
	
    
	public void generarTableroAleatorio() {
		
		casillas.add(new CasillaNormal(0, "Casilla normal"));	//Generar primera casilla


		//Bucle generación tablero
		for (int i = 1; i < midaTablero - 1; i++) {
			int aleatorio = (int)(Math.random() * 6) + 1;
			
			switch (aleatorio) {                       //LOS PRINTS SON DE PRUEBA
			case 1:
				casillas.add(new CasillaNormal(i, "Casilla NORMAL"));
				System.out.println(i + ": normal");
				break;
			case 2:
				casillas.add(new CasillaOso(i, "Casilla OSO"));
				System.out.println(i + ": oso");
				break;
			case 3:
				casillas.add(new CasillaAgujero(i, "Casilla AGUJERO", 0)); 
				System.out.println(i + ": agujero");
				/*HACER FORMULA PARA SABER LA CASILLA AGUJERO ANTERIOR
				SI NO HAY CASILLA ANTEIROR VUELVE AL PRINCIPIO*/
				break;
			case 4:
				casillas.add(new CasillaTrineo(i, "Casilla TRINEO", 0));
				System.out.println(i + ": trineo");

				/*HACER FORMULA PARA SABER LA CASILLA TRINEO SIGUIENTE*/
				break;
			case 5:
				casillas.add(new CasillaInterrogante(i, "Casilla INTERROGANTE"));
				System.out.println(i + ": interrogante");
				break;
			case 6:
				casillas.add(new CasillaRompedizas(i, "Casilla ROMPEDIZAS"));
				System.out.println(i + ": rompedizas");
				break;
			default:
			}

		}
		
		
		casillas.add(new CasillaNormal(midaTablero - 1, "Casilla normal")); //Generar ultima casilla		
	}
	
	
	
	
	
	/*public Casilla getCasilla(int posicion) {
		
	}
	*/
	
	public void moverJugador(Jugador jugador, int posicion) {
		
	}
	
}
