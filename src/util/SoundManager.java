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

    static {
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
            return new AudioClip(resource.toExternalForm());
        }
        System.err.println("No se pudo encontrar el sonido: " + path);
        return null;
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
