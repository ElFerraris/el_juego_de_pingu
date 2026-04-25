package util;

import java.io.*;
import java.util.Properties;

/**
 * SettingsManager
 * 
 * Gestiona la configuración del juego (volumen, resolución, pantalla completa).
 * Guarda los datos en la carpeta de documentos del usuario para persistencia.
 */
public class SettingsManager {

    private static final String FOLDER_BASE = "juego_pingu";
    private static final String FOLDER_CONFIG = "config";
    private static final String FILE_NAME = "opciones.properties";

    private Properties props;
    private File settingsFile;

    private static SettingsManager instance;

    private SettingsManager() {
        props = new Properties();
        setupFile();
        load();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private void setupFile() {
        // Buscamos la carpeta de documentos del usuario
        String userHome = System.getProperty("user.home");
        File documents = new File(userHome, "Documents");

        // Creamos la carpeta del juego y dentro la de config
        File appDir = new File(documents, FOLDER_BASE);
        File configDir = new File(appDir, FOLDER_CONFIG);

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        settingsFile = new File(configDir, FILE_NAME);
    }

    public void load() {
        if (settingsFile.exists()) {
            try (InputStream is = new FileInputStream(settingsFile)) {
                props.load(is);
            } catch (IOException e) {
                System.err.println("Error cargando configuración: " + e.getMessage());
            }
        } else {
            // Valores por defecto
            props.setProperty("resolution", "1280x720");
            props.setProperty("fullscreen", "false");
            props.setProperty("volume.sfx", "0.5");
            props.setProperty("volume.music", "0.5");
            save();
        }
    }

    public void save() {
        try (OutputStream os = new FileOutputStream(settingsFile)) {
            props.store(os, "Configuración del Juego de Pingu");
        } catch (IOException e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }

    // --- GETTERS ---
    public String getResolution() {
        return props.getProperty("resolution", "1280x720");
    }

    public boolean isFullscreen() {
        return Boolean.parseBoolean(props.getProperty("fullscreen", "false"));
    }

    public double getSfxVolume() {
        return Double.parseDouble(props.getProperty("volume.sfx", "0.8"));
    }

    public double getMusicVolume() {
        return Double.parseDouble(props.getProperty("volume.music", "0.8"));
    }

    // --- SETTERS ---
    public void setResolution(String res) {
        props.setProperty("resolution", res);
    }

    public void setFullscreen(boolean fs) {
        props.setProperty("fullscreen", String.valueOf(fs));
    }

    public void setSfxVolume(double vol) {
        props.setProperty("volume.sfx", String.valueOf(vol));
    }

    public void setMusicVolume(double vol) {
        props.setProperty("volume.music", String.valueOf(vol));
    }
}
