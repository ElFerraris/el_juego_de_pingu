package util;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

/**
 * SoundManager
 * 
 * Gestiona la carga y reproducción de efectos de sonido (SFX) y música de fondo.
 * Utiliza AudioClip para efectos rápidos y MediaPlayer para la música.
 */
public class SoundManager {

    private static AudioClip hoverSound;
    private static AudioClip confirmSound;
    private static AudioClip backSound;
    
    private static MediaPlayer musicPlayer;
    
    private static double sfxVolume = 0.5;
    private static double musicVolume = 0.5;

    static {
        // Cargamos los volúmenes guardados
        try {
            SettingsManager sm = SettingsManager.getInstance();
            sfxVolume = sm.getSfxVolume();
            musicVolume = sm.getMusicVolume();
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
            clip.setVolume(sfxVolume);
            return clip;
        }
        return null;
    }

    // ==================== GESTIÓN DE SFX ====================

    public static void setSfxVolume(double volume) {
        sfxVolume = volume;
        if (hoverSound != null) hoverSound.setVolume(sfxVolume);
        if (confirmSound != null) confirmSound.setVolume(sfxVolume);
        if (backSound != null) backSound.setVolume(sfxVolume);
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

    // ==================== GESTIÓN DE MÚSICA ====================

    private static String currentMusicPath = "";

    public static void setMusicVolume(double volume) {
        musicVolume = volume;
        if (musicPlayer != null) {
            musicPlayer.setVolume(musicVolume);
        }
    }

    /**
     * Reproduce la música del menú en bucle.
     */
    public static void playMenuMusic() {
        playMusic("/assets/music/Menu/menu.mp3");
    }

    /**
     * Carga y reproduce un archivo de música.
     */
    public static void playMusic(String path) {
        // Si ya está sonando esta misma música, no hacemos nada para evitar cortes
        if (currentMusicPath.equals(path) && musicPlayer != null) {
            return;
        }

        stopMusic();

        try {
            URL resource = SoundManager.class.getResource(path);
            if (resource != null) {
                currentMusicPath = path;
                Media media = new Media(resource.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setVolume(musicVolume);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.play();
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir música: " + e.getMessage());
        }
    }

    /**
     * Detiene la música actual.
     */
    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
            currentMusicPath = "";
        }
    }
}
