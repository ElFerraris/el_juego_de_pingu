package util;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Utilidad para cargar recursos desde la carpeta de assets externa.
 * Facilita la transición de recursos de classpath a archivos de sistema.
 */
public class AssetLoader {

    /**
     * Carga una imagen desde la carpeta de assets.
     * @param path Ruta relativa al asset (ej: "/assets/iconos/trash.png")
     * @return Objeto Image o null si no se encuentra.
     */
    public static Image loadImage(String path) {
        try {
            String sanitizedPath = sanitize(path);
            File file = new File(sanitizedPath);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }
        } catch (Exception e) {
            System.err.println("AssetLoader: Error cargando imagen " + path + " -> " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene un InputStream de un asset.
     * @param path Ruta relativa al asset.
     * @return InputStream o null si hay error.
     */
    public static InputStream loadStream(String path) {
        try {
            return new FileInputStream(new File(sanitize(path)));
        } catch (FileNotFoundException e) {
            System.err.println("AssetLoader: Archivo no encontrado " + path);
            return null;
        }
    }

    /**
     * Obtiene la URL formateada para JavaFX (file:...) de un asset.
     * @param path Ruta relativa al asset.
     * @return String con la URL o cadena vacía si no existe.
     */
    public static String loadUrl(String path) {
        File file = new File(sanitize(path));
        if (file.exists()) {
            return file.toURI().toString();
        }
        return "";
    }

    private static String sanitize(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }
}
