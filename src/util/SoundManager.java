package util;

import javafx.scene.media.AudioClip;
import java.net.URL;

/**
 * SoundManager
 * 
 * Gestiona la carga y reproducción de efectos de sonido (SFX).
 * Utiliza AudioClip para una reproducción instantánea ideal para botones.
 */
public class SoundManager {

    private static AudioClip hoverSound;
    private static AudioClip confirmSound;
    private static AudioClip backSound;
    
    private static double volume = 0.8; // Volumen por defecto (0.0 a 1.0)

    static {
        // Cargamos el volumen guardado si existe al iniciar
        try {
            volume = SettingsManager.getInstance().getSfxVolume();
        } catch (Exception e) {}
        
        try {
            hoverSound = loadSound("/assets/sfx/button-hover.wav");
            confirmSound = loadSound("/assets/sfx/button-confirm.wav");
            backSound = loadSound("/assets/sfx/button-back.wav");
        } catch (Exception e) {
            System.err.println("Error cargando SFX: " + e.getMessage());
        }
    }

    private static AudioClip loadSound(String path) {
        URL resource = SoundManager.class.getResource(path);
        if (resource != null) {
            AudioClip clip = new AudioClip(resource.toExternalForm());
            clip.setVolume(volume); // Aplicamos el volumen actual
            return clip;
        }
        System.err.println("No se pudo encontrar el sonido: " + path);
        return null;
    }

    public static void setVolume(double newVolume) {
        volume = newVolume;
        if (hoverSound != null) hoverSound.setVolume(volume);
        if (confirmSound != null) confirmSound.setVolume(volume);
        if (backSound != null) backSound.setVolume(volume);
    }

    public static void playHover() {
        if (hoverSound != null) hoverSound.play();
    }

    public static void playConfirm() {
        if (confirmSound != null) confirmSound.play();
    }

    public static void playBack() {
        if (backSound != null) backSound.play();
    }
}
