package el_juego_de_pingu;

public class BolaNieve extends Objeto{

	private int potencia = 3;

	public BolaNieve(String nombre, String tipo) {
		super(nombre, tipo);
	}

	/**
     * Este método recibe al jugador que quieres atacar.
     */
    public void atacar(Jugador oponente) {
        if (oponente != null) {
            System.out.println("¡Impacto! Lanzando bola a " + oponente.getNombre());
            
            // Aquí es donde "llamas" al sitio del jugador:
            // Jugador tiene un método para retroceder
            oponente.retroceder(this.potencia); 
        }
    }

    @Override
    public void usar(Jugador jugadorQueLaUsa) {
        // En un juego real, aquí necesitarías una lógica para elegir qué oponente
        // Pero para la estructura básica, podrías dejarlo preparado:
        System.out.println(jugadorQueLaUsa.getNombre() + " se prepara para lanzar una bola.");
    }
	
}
