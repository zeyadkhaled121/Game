import javax.sound.sampled.*;
import javazoom.jl.player.Player;
import java.io.*;

public class UniversalSoundPlayer {

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

    public static void play(String path) {
        if (path.toLowerCase().endsWith(".wav")) {  
            playWav(path);
        } else if (path.toLowerCase().endsWith(".mp3")) {   
            playMp3(path);
        } else {
            System.out.println("Unsupported audio format: " + path);
        }
    }
}
