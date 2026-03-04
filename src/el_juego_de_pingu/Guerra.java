package el_juego_de_pingu;

public class Guerra{
	
	private Jugador jugador1;
	private Jugador jugador2;

	public void iniciarGuerra(Jugador jugador1, Jugador jugador2) {

		int bolasJ1 = jugador1.getInventario().getCuantidad("BolaNieve");
	    int bolasJ2 = jugador2.getInventario().getCuantidad("BolaNieve");
	    int diferencia =0;
	    
	    if(bolasJ1 > bolasJ2) {
	    	diferencia = bolasJ1 - bolasJ2;
	    	System.out.println("El jugador " + jugador1.getNombre() + " ha ganado la Guerra contra " + jugador2.getNombre()+ ". \n El Jugador " + jugador1.getNombre() +" avanza " + diferencia+" casillas.");
	    	jugador1.moverFicha(diferencia);
	    }
	    
	    else if(bolasJ2 > bolasJ1) {
	    	diferencia = bolasJ2 - bolasJ1;

	    	System.out.println("El jugador " + jugador2.getNombre() + " ha ganado la Guerra contra " + jugador1.getNombre() + ". \n El Jugador " + jugador2.getNombre() +" avanza " + diferencia+" casillas.");
	    	jugador2.moverFicha(diferencia);
	    }
	    else {
	    	System.out.println("EMPATE Nadie avanza");
	    }
	    
	    limpiarBolas(jugador1);
	    limpiarBolas(jugador2);
	    
	}

	public void limpiarBolas(Jugador jugador) {
		
		while (jugador.getInventario().getCuantidad("BolaNieve") > 0) {
			jugador.getInventario().eliminarObjeto("BolaNieve");
	  }	
		
	}
	
	
}
