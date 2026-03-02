package el_juego_de_pingu;

public abstract class Dado extends Objeto {

	protected int valorMin;
	protected int valorMax;
	
	
	public Dado(String nombre, String tipo, int valorMin, int valorMax) {
        super(nombre, tipo);
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }
	
	
	public int tirar() {
        return (int) (Math.random() * (valorMax - valorMin + 1)) + valorMin;
    }


	@Override
    public void usar(Jugador jugador) {
        int pasos = tirar();
        System.out.println("Has sacado un " + pasos + " con el " + this.nombre);
        jugador.moverFicha(pasos);
    }
	
}
	