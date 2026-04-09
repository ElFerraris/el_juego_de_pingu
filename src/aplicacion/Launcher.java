package aplicacion;

import java.io.File;

public class Launcher {
    public static void main(String[] args) {
        // Obtenemos la ruta absoluta de la carpeta 'lib'
        String libPath = new File("lib").getAbsolutePath();
        
        // --- CONFIGURACIÓN AUTOMÁTICA DE RUTAS NATIVAS ---
        // Estas propiedades le dicen a JavaFX dónde buscar los archivos .dll
        System.setProperty("java.library.path", libPath);
        System.setProperty("javafx.library.path", libPath);
        System.setProperty("prism.library.path", libPath);
        System.setProperty("glass.library.path", libPath);
        
        // Habilitar logs detallados para ver si ahora sí las encuentra
        System.setProperty("prism.verbose", "true");
        
        System.out.println("--- DIAGNÓSTICO JAVAFX (AUTOMATIZADO) ---");
        System.out.println("Forzando ruta de librerías a: " + libPath);
        
        File libDir = new File(libPath);
        if (!libDir.exists()) {
            System.err.println("¡ERROR! No se encuentra la carpeta 'lib' en la raíz del proyecto.");
        }

        // Ejecutar Main
        try {
            Main.main(args);
        } catch (Exception e) {
            System.err.println("Error fatal al arrancar la aplicación:");
            e.printStackTrace();
        }
    }
}
