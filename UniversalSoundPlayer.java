import javax.sound.sampled.*;
import javazoom.jl.player.Player;
import java.io.*;

public class UniversalSoundPlayer {

    // تشغيل WAV
    public static void playWav(String path) {
        try {
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInput);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // تشغيل MP3
    public static void playMp3(String path) {
        new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(path);
                BufferedInputStream bis = new BufferedInputStream(fis);
                Player player = new Player(bis);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // دالة موحدة: تشوف نوع الملف وتشتغل
    public static void play(String path) {
        if (path.toLowerCase().endsWith(".wav")) {   // ← هنا
            playWav(path);
        } else if (path.toLowerCase().endsWith(".mp3")) {   // ← وهنا
            playMp3(path);
        } else {
            System.out.println("Unsupported audio format: " + path);
        }
    }
}
