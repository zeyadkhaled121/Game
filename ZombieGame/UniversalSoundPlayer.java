package ZombieGame;

import javazoom.jl.player.Player;
import java.io.*;
import javax.sound.sampled.*;

public class UniversalSoundPlayer {

    private static Player bgmPlayer;
    private static Thread bgmThread;
    private static boolean loopRunning = false;

 
    public static void stop() {
        stopLoop();


    }

    
    public static void play(String path) {
        try {
            if (path.toLowerCase().endsWith(".wav")) {
                playWav(path);
            } else if (path.toLowerCase().endsWith(".mp3")) {
                playMp3OneShot(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void playWav(String path) {
        new Thread(() -> {
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(path));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.err.println("Error playing WAV: " + path);
            }
        }).start();
    }

    private static void playMp3OneShot(String path) {
        new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(path);
                Player player = new Player(fis);
                player.play();
                player.close();
            } catch (Exception e) {
                System.err.println("Error playing MP3 SFX: " + path);
            }
        }).start();
    }

 
    public static void loopMp3(String path) {
        stopLoop(); 

        loopRunning = true;
        bgmThread = new Thread(() -> {
            while (loopRunning) {
                try {
                    FileInputStream fis = new FileInputStream(path);
                    bgmPlayer = new Player(fis);
                    bgmPlayer.play();
                } catch (Exception e) {
                    break;
                }
            }
        });
        bgmThread.start();
    }

    public static void stopLoop() {
        loopRunning = false;
        try {
            if (bgmPlayer != null) bgmPlayer.close();
        } catch (Exception ignored) {}
        try {
            if (bgmThread != null) bgmThread.interrupt();
        } catch (Exception ignored) {}
    }
}
