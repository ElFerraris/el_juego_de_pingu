package modelo;

import java.util.ArrayList;

/**
 * Inventario de un jugador. Contiene peces, bolas de nieve y dados especiales.
 */
public class Inventario {

    public static final int MAX_PECES = 2;
    public static final int MAX_BOLAS_NIEVE = 6;
    public static final int MAX_DADOS = 3;

    private ArrayList<Objeto> dados = new ArrayList<>();
    private int peces;
    private int bolasNieve;

    public Inventario() {
    }

    /**
     * Agrega un objeto al inventario si no se ha alcanzado el máximo.
     * @return true si se pudo agregar, false si está lleno.
     */
    public boolean agregarObjeto(String tipo) {
        switch (tipo.trim()) {
            case "Pez":
                if (peces < MAX_PECES) {
                    peces++;
                    return true;
                }
                break;
            case "BolaNieve":
                if (bolasNieve < MAX_BOLAS_NIEVE) {
                    bolasNieve++;
                    return true;
                }
                break;
            case "DadoRapido":
                if (dados.size() < MAX_DADOS) {
                    dados.add(new DadoRapido("Dado Rápido", "Dados"));
                    return true;
                }
                break;
            case "DadoLento":
                if (dados.size() < MAX_DADOS) {
                    dados.add(new DadoLento("Dado Lento", "Dados"));
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * Elimina un objeto del inventario.
     */
    public void eliminarObjeto(String tipo) {
        if (tipo.equals("Pez") && peces > 0) {
            peces--;
        } else if (tipo.equals("BolaNieve") && bolasNieve > 0) {
            bolasNieve--;
        } else if (tipo.equals("Dados") && !dados.isEmpty()) {
            dados.remove(dados.size() - 1);
        }
    }

    /**
     * Comprueba si el jugador tiene al menos un objeto del tipo indicado.
     */
    public boolean tieneObjeto(String tipo) {
        switch (tipo) {
            case "Pez": return peces >= 1;
            case "BolaNieve": return bolasNieve >= 1;
            case "Dados": return dados.size() >= 1;
            default: return false;
        }
    }

    /**
     * Devuelve la cantidad de objetos del tipo indicado.
     * "Total" devuelve la suma de todos los objetos.
     */
    public int getCantidad(String tipo) {
        switch (tipo) {
            case "Pez": 
                return peces;
            case "BolaNieve": 
                return bolasNieve;
            case "Dados": 
                return dados.size();
            case "DadoRapido":
                return contarDadosPorClase(DadoRapido.class);
            case "DadoLento":
                return contarDadosPorClase(DadoLento.class);
            case "Total": 
                return peces + bolasNieve + dados.size();
            default: 
                return 0;
        }
    }
    
    private int contarDadosPorClase(Class<?> clase) {
        int contador = 0;
        for (Objeto d : dados) {
            if (clase.isInstance(d)) {
                contador++;
            }
        }
        return contador;
    }

    /**
     * Usa un objeto del inventario y aplica su efecto.
     */
    public boolean usarObjeto(String tipo, Jugador jugador) {
        if (tipo.equals("Pez") && peces > 0) {
            peces--;
            return true;
        }
        if (tipo.equals("BolaNieve") && bolasNieve > 0) {
            bolasNieve--;
            return true;
        }
        if (tipo.equals("Dados") && !dados.isEmpty()) {
            Dado dadoParaUsar = (Dado) dados.remove(0);
            dadoParaUsar.usar(jugador);
            return true;
        }
        return false;
    }

    /**
     * Usa un dado específico (Rápido o Lento) del inventario.
     */
    public boolean usarDadoEspecifico(String subTipo, Jugador jugador) {
        for (int i = 0; i < dados.size(); i++) {
            Objeto d = dados.get(i);
            if ((subTipo.equals("Rapido") && d instanceof DadoRapido) ||
                (subTipo.equals("Lento") && d instanceof DadoLento)) {
                dados.remove(i).usar(jugador);
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve la lista de dados especiales.
     */
    public ArrayList<Objeto> getListaDados() {
        return this.dados;
    }

    /**
     * Comprueba si existe un dado específico (Rápido o Lento).
     */
    public boolean tieneObjetoEspecifico(String subTipo) {
        for (Objeto d : dados) {
            if (subTipo.equals("Rapido") && d instanceof DadoRapido) return true;
            if (subTipo.equals("Lento") && d instanceof DadoLento) return true;
        }
        return false;
    }

    /**
     * Reduce el inventario a la mitad (cuando la foca ataca).
     */
    public void serAtacado() {
        this.peces /= 2;
        this.bolasNieve /= 2;

        int dadosARemover = dados.size() / 2;
        for (int i = 0; i < dadosARemover; i++) {
            if (!dados.isEmpty()) {
                dados.remove(0);
            }
        }
    }
    
    public int agregarObjetos(String tipo, int cantidad) {
        int agregados = 0;
        
        while (agregados < cantidad && agregarObjeto(tipo)) {
            agregados++;
        }
        
        return agregados;
    }
    
    
}
