package el_juego_de_pingu;

public class Guerra{
	
	private Jugador jugador1;
	private Jugador jugador2;

	public void iniciarGuerra(Jugador j1, Jugador j2) {
	    int bolasJ1 = j1.getInventario().getCuantidad("BolaNieve");
	    int bolasJ2 = j2.getInventario().getCuantidad("BolaNieve");

	    if (bolasJ1 > bolasJ2) {
	        int diferencia = bolasJ1 - bolasJ2;
	        System.out.println(j1.getNombre() + " gana. " + j2.getNombre() + " retrocede " + diferencia);
	        j2.retroceder(diferencia); // El perdedor retrocede
	    } else if (bolasJ2 > bolasJ1) {
	        int diferencia = bolasJ2 - bolasJ1;
	        System.out.println(j2.getNombre() + " gana. " + j1.getNombre() + " retrocede " + diferencia);
	        j1.retroceder(diferencia); // El perdedor retrocede
	    } else {
	        System.out.println("Empate técnico: Nadie retrocede.");
	    }

	    // Ambos gastan todas sus bolas (regla obligatoria)
	    limpiarBolas(j1);
	    limpiarBolas(j2);
	}	

	public void limpiarBolas(Jugador jugador) {
		
		while (jugador.getInventario().getCuantidad("BolaNieve") > 0) {
			jugador.getInventario().eliminarObjeto("BolaNieve");
	  }	
		
	}
	
	
	
	
}