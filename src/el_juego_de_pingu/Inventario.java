package el_juego_de_pingu;

import java.util.ArrayList;

public class Inventario{

    ArrayList<Objeto> dados = new ArrayList<Objeto>();
	private int peces;        // max = 2
	private int bolasNieve;   // max = 6
	
	public Inventario() {

	}

	public boolean agregarObjeto(String tipo) {
	    // Es recomendable usar .trim() por si acaso
	    switch (tipo.trim()) { 
	        case "Pez":
	            if (peces < 2) {
	                peces++;
	                return true;
	            }
	            break;
	        case "BolaNieve":
	            if (bolasNieve < 6) {
	                bolasNieve++;
	                return true;
	            }
	            break;
	        case "DadoRapido":
	            if (dados.size() < 3) {
	                // Asegúrate de que la clase DadoRapido esté bien escrita
	                dados.add(new DadoRapido("Dado Rápido", "Dados")); 
	                return true;
	            }
	            break;
	        case "DadoLento":
	            if (dados.size() < 3) {
	                dados.add(new DadoLento("Dado Lento", "Dados")); 
	                return true;
	            }
	            break;
	    }
	    return false; 
	}

	public void eliminarObjeto(String tipo) {
	    if (tipo.equals("Pez") && peces > 0) {
	        peces--;
	    } else if (tipo.equals("BolaNieve") && bolasNieve > 0) {
	        bolasNieve--;
	    } else if (tipo.equals("Dados") && !dados.isEmpty()) {
	        // Elimina el último dado obtenido (el que esté al final de la lista)
	        dados.remove(dados.size() - 1);
	    }
	}

	
	public boolean tieneObjeto(String tipo) {
        if (tipo.equals("Pez")) {
            return peces >= 1;
        }
        if (tipo.equals("BolaNieve")) {
            return bolasNieve >= 1;
        }
        if (tipo.equals("Dados")) {
            // Usamos .size() para obtener la cantidad de elementos
            return dados.size() >= 1;
        }
        return false;
    }

	public int getCuantidad(String tipo) {
    switch (tipo) {
        case "Pez": return peces;
        case "BolaNieve": return bolasNieve;
        case "Dados": return dados.size();
        case "Total": return peces + bolasNieve + dados.size();
        default: return 0;
    }
}

	public boolean usarObjeto(String tipo, Jugador jugador) {
	    if (tipo.equals("Pez") && peces > 0) {
	        // Lógica para subornar al oso o alimentar a la foca
	        peces--;
	        return true;
	    }
	    if (tipo.equals("BolaNieve") && bolasNieve > 0) {
	        // Lógica para hacer retrocedir a otros
	        bolasNieve--;
	        return true;
	    }
	    if (tipo.equals("Dados") && !dados.isEmpty()) {
	        // Usamos el polimorfismo: sacamos el dado y llamamos a su método usar 
	        Dado dadoParaUsar = (Dado) dados.remove(0);
	        dadoParaUsar.usar(jugador);
	        return true;
	    }
	    return false;
	}
	
	
	
	//Dado especial
	public boolean usarDadoEspecifico(String subTipo, Jugador jugador) {
	    for (int i = 0; i < dados.size(); i++) {
	        Objeto d = dados.get(i);
	        // Comprobamos si es el tipo que busca el usuario
	        if ((subTipo.equals("Rapido") && d instanceof DadoRapido) || 
	            (subTipo.equals("Lento") && d instanceof DadoLento)) {
	            
	            dados.remove(i).usar(jugador); // Lo usa y lo borra de la lista
	            return true;
	        }
	    }
	    return false;
	}
	public ArrayList<Objeto> getDadosLista() {
	    return this.dados;
	}
	
	public boolean tieneObjetoEspecifico(String subTipo) {
	    for (Objeto d : dados) {
	        if (subTipo.equals("Rapido") && d instanceof DadoRapido) return true;
	        if (subTipo.equals("Lento") && d instanceof DadoLento) return true;
	    }
	    return false;
	}
	
}