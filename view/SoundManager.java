package view;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    
    // Lấy đường dẫn absolute của project sounds folder
    private static String getSoundPath(String filename) {
        // Get current working directory và append sounds folder
        String projectRoot = System.getProperty("user.dir");
        return projectRoot + File.separator + "sounds" + File.separator + filename;
    }
    
    // Hover sound
    public static void playHoverSound() {
        playSound(getSoundPath("hover-gta-menu.wav"));
    }
    
    // Click sound
    public static void playClickSound() {
        playSound(getSoundPath("click-gta-menu-m4a.wav"));
    }
    
    // Piece placement sound
    public static void playPlacementSound() {
        playSound(getSoundPath("chessPlacing-aheart.wav"));
    }
    
    // Game end sound
    public static void playGameEndSound() {
        playSound(getSoundPath("gameEnd-shocked-sound-effect.wav"));
    }
    
    // Phát âm thanh từ file
    private static void playSound(String soundFile) {
        new Thread(() -> {
            try {
                File file = new File(soundFile);
                if (!file.exists()) {
                    return;
                }
                
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                // Bỏ qua lỗi
            }
        }).start();
    }
}

