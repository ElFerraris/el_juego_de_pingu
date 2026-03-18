package modelo;

/**
 * Clase abstracta que representa un objeto del juego.
 * Cada hijo (Pez, BolaNieve, Dado) implementa su propia lógica de uso.
 */
public abstract class Objeto {

    private String nombre;
    private String tipo;

    public Objeto(String nombre, String tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }

    /**
     * Método abstracto: cada hijo implementa su propia lógica de uso.
     */
    public abstract void usar(Jugador jugador);

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
}
