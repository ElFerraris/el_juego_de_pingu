package el_juego_de_pingu;
import java.util.Random;

public class Evento{
	private String tipoEvento;
	
	
	

	public Evento(String tipoEvento) {
		this.tipoEvento = tipoEvento;
	}
	
	
	
	
	public void aplicarEfecto(Jugador jugador) {

		System.out.println("Casilla aleatoria ocurrida: " + tipoEvento);
		
		switch (tipoEvento) {
        case "Pez":
            jugador.getInventario().agregarObjeto("Pez");
            break;
        case "BolaNieve":
            Random r = new Random();
            int cantidad = r.nextInt(3) + 1;
            for (int i = 0; i <= cantidad; i++) {
                jugador.getInventario().agregarObjeto("BolaNieve");
            }
            break;
        case "DadoRapido":
            jugador.getInventario().agregarObjeto("DadoRapido");
            break;
        case "DadoLento":
            jugador.getInventario().agregarObjeto("DadoLento");
            break;
		}
	}

	
}