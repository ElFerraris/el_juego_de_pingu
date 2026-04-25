package util;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.net.URL;

/**
 * SoundManager
 * 
 * Gestiona la carga y reproducción de efectos de sonido (SFX) y música de
 * fondo.
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
        } catch (Exception e) {
        }

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
        if (hoverSound != null)
            hoverSound.setVolume(sfxVolume);
        if (confirmSound != null)
            confirmSound.setVolume(sfxVolume);
        if (backSound != null)
            backSound.setVolume(sfxVolume);
    }

    public static void playHover() {
        if (hoverSound != null)
            hoverSound.play();
    }

    public static void playConfirm() {
        if (confirmSound != null)
            confirmSound.play();
    }

    public static void playBack() {
        if (backSound != null)
            backSound.play();
    }

    // ==================== GESTIÓN DE MÚSICA ====================

    private static String currentMusicPath = "";
    private static MediaPlayer[] gameMusicLayers = new MediaPlayer[7];
    private static int currentLevel = -1;

    public static void setMusicVolume(double volume) {
        musicVolume = volume;
        if (musicPlayer != null) {
            musicPlayer.setVolume(musicVolume);
        }
        // También actualizamos el volumen de las capas de la partida
        updateGameMusicLevel(currentLevel);
    }

    /**
     * Reproduce la música del menú en bucle.
     */
    public static void playMenuMusic() {
        stopGameMusic(); // Por si venimos de una partida
        playMusic("/assets/music/Menu/menu.mp3");
    }

    /**
     * Carga y reproduce un archivo de música normal (para menús).
     */
    public static void playMusic(String path) {
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
     * Inicia el sistema de música dinámica para la partida.
     * Carga las 7 pistas y las pone a reproducir sincronizadas a volumen 0.
     */
    public static void startGameMusic() {
        stopMusic(); // Paramos la música de menú
        stopGameMusic(); // Limpiamos por si acaso

        for (int i = 0; i < 7; i++) {
            try {
                String path = "/assets/music/Partida/" + (i + 1) + ".wav";
                URL resource = SoundManager.class.getResource(path);
                if (resource != null) {
                    Media media = new Media(resource.toExternalForm());
                    gameMusicLayers[i] = new MediaPlayer(media);
                    gameMusicLayers[i].setVolume(0); // Empezamos en silencio
                    gameMusicLayers[i].setCycleCount(MediaPlayer.INDEFINITE);
                    gameMusicLayers[i].play();
                }
            } catch (Exception e) {
                System.err.println("Error cargando capa " + (i + 1) + ": " + e.getMessage());
            }
        }
        updateGameMusicLevel(1); // Empezamos con el nivel 1
    }

    /**
     * Actualiza qué capa de música suena según la posición más avanzada.
     * 
     * @param maxPos La posición del jugador más adelantado.
     */
    public static void updateGameMusicByPosition(int maxPos) {
        // Nivel = posición / 7 + 1 (máximo 7)
        int level = (maxPos / 7) + 1;
        if (level > 7)
            level = 7;

        if (level != currentLevel) {
            updateGameMusicLevel(level);
        }
    }

    private static Timeline fadeTimeline;

    private static void updateGameMusicLevel(int level) {
        currentLevel = level;

        if (fadeTimeline != null) {
            fadeTimeline.stop();
        }

        fadeTimeline = new Timeline();

        for (int i = 0; i < 7; i++) {
            if (gameMusicLayers[i] != null) {
                // El nivel que toca sube a musicVolume, los demás bajan a 0.0
                double targetVol = ((i + 1) == level) ? musicVolume : 0.0;

                // Animamos la propiedad de volumen de cada MediaPlayer
                KeyValue kv = new KeyValue(gameMusicLayers[i].volumeProperty(), targetVol);
                KeyFrame kf = new KeyFrame(Duration.millis(1500), kv); // 1.5 segundos de fundido
                fadeTimeline.getKeyFrames().add(kf);
            }
        }
        fadeTimeline.play();
    }

    /**
     * Detiene toda la música de la partida.
     */
    public static void stopGameMusic() {
        for (int i = 0; i < 7; i++) {
            if (gameMusicLayers[i] != null) {
                gameMusicLayers[i].stop();
                gameMusicLayers[i].dispose();
                gameMusicLayers[i] = null;
            }
        }
        currentLevel = -1;
    }

    /**
     * Detiene la música actual de menús.
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
