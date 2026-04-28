package modelo;

import java.util.ArrayList;

/**
 * Sistema de gestión de recursos y objetos para un jugador.
 * 
 * <p>
 * Centraliza el almacenamiento y manipulación de los diferentes consumibles
 * del juego: peces (para sobornos), bolas de nieve (para combate) y
 * dados especiales (para movilidad). Controla los límites de capacidad y
 * proporciona métodos para el uso estratégico de los mismos.
 * </p>
 * 
 * @author BadLabs©️
 * @version 1.0
 */
public class Inventario {

    /** Capacidad máxima permitida para peces. */
    public static final int MAX_PECES = 2;
    /** Capacidad máxima permitida para bolas de nieve. */
    public static final int MAX_BOLAS_NIEVE = 6;
    /** Capacidad máxima permitida para dados especiales en total. */
    public static final int MAX_DADOS = 3;

    /** Colección de dados especiales (Rápidos/Lentos). */
    private ArrayList<Objeto> dados = new ArrayList<>();
    /** Contador actual de peces recolectados. */
    private int peces;
    /** Contador actual de bolas de nieve fabricadas. */
    private int bolasNieve;

    /**
     * Constructor por defecto del inventario inicializado a cero.
     */
    public Inventario() {
    }

    /**
     * Intenta añadir un objeto de un tipo específico al inventario.
     * 
     * <p>
     * Valida que no se exceda el límite máximo definido para cada categoría.
     * </p>
     * 
     * @param tipo Nombre del objeto ("Pez", "BolaNieve", "DadoRapido",
     *             "DadoLento").
     * @return {@code true} si el objeto fue añadido con éxito; {@code false} si el
     *         inventario está lleno para ese tipo.
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
     * Elimina una unidad del tipo de objeto especificado.
     * 
     * @param tipo Categoría del objeto a eliminar.
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
     * Comprueba si el inventario contiene al menos una unidad del tipo indicado.
     * 
     * @param tipo Categoría o subtipo de objeto.
     * @return {@code true} si existe disponibilidad.
     */
    public boolean tieneObjeto(String tipo) {
        switch (tipo) {
            case "Pez":
                return peces >= 1;
            case "BolaNieve":
                return bolasNieve >= 1;
            case "Dados":
                return dados.size() >= 1;
            case "DadoRapido":
                return tieneObjetoEspecifico("Rapido");
            case "DadoLento":
                return tieneObjetoEspecifico("Lento");
            default:
                return false;
        }
    }

    /**
     * Obtiene la cantidad actual de objetos de un tipo.
     * 
     * @param tipo Categoría ("Pez", "BolaNieve", "Dados", "Total", etc.).
     * @return Cantidad entera almacenada.
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

    /**
     * Método auxiliar para contar cuántos dados de un subtipo existen.
     * 
     * @param clase Clase del dado a buscar.
     * @return Cantidad encontrada.
     */
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
     * Ejecuta el uso de un objeto genérico.
     * 
     * @param tipo    Categoría del objeto.
     * @param jugador El {@link Jugador} que consume el objeto.
     * @return {@code true} si se pudo usar; {@code false} si no había existencias.
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
     * Selecciona y consume un dado especial específico (Rápido o Lento).
     * 
     * @param subTipo Identificador del subtipo ("Rapido" o "Lento").
     * @param jugador El {@link Jugador} sobre el que se aplica el efecto del dado.
     * @return {@code true} si se encontró y usó el dado.
     */
    public boolean usarDadoEspecifico(String subTipo, Jugador jugador) {
        for (int i = 0; i < dados.size(); i++) {
            Objeto d = dados.get(i);
            if ((subTipo.equals("Rapido") && d instanceof DadoRapido) ||
                    (subTipo.equals("Lento") && d instanceof DadoLento)) {
                ((Dado) dados.remove(i)).usar(jugador);
                return true;
            }
        }
        return false;
    }

    /**
     * Proporciona la lista interna de dados especiales.
     * 
     * @return Lista de {@link Objeto} que son dados.
     */
    public ArrayList<Objeto> getListaDados() {
        return this.dados;
    }

    /**
     * Verifica la existencia de un subtipo de dado sin consumirlo.
     * 
     * @param subTipo "Rapido" o "Lento".
     * @return {@code true} si existe al menos uno.
     */
    public boolean tieneObjetoEspecifico(String subTipo) {
        for (Objeto d : dados) {
            if (subTipo.equals("Rapido") && d instanceof DadoRapido)
                return true;
            if (subTipo.equals("Lento") && d instanceof DadoLento)
                return true;
        }
        return false;
    }

    /**
     * Simula la pérdida de recursos tras un ataque de la Foca.
     * Reduce a la mitad (redondeo hacia abajo) todas las existencias.
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

    /**
     * Intenta agregar múltiples unidades de un mismo tipo.
     * 
     * @param tipo     Tipo de objeto.
     * @param cantidad Número de unidades deseadas.
     * @return Número real de unidades que se pudieron añadir antes de llenar el
     *         inventario.
     */
    public int agregarObjetos(String tipo, int cantidad) {
        int agregados = 0;

        boolean inventarioLleno = false;
        while (agregados < cantidad && !inventarioLleno) {
            if (agregarObjeto(tipo)) {
                agregados++;
            } else {
                inventarioLleno = true;
            }
        }

        return agregados;
    }

}
