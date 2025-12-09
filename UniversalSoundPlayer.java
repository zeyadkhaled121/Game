package ZombieGame;

import javazoom.jl.player.Player;
import java.io.*;
import javax.sound.sampled.*;

public class UniversalSoundPlayer {

    // متغيرات خاصة بموسيقى الخلفية فقط
    private static Player bgmPlayer;
    private static Thread bgmThread;
    private static boolean loopRunning = false;

    // ---------------------------------------
    // ميثود STOP المطلوبة
    // ---------------------------------------
    public static void stop() {
        // هذه الدالة توقف الموسيقى المستمرة (الخلفية)
        stopLoop();

        // ملاحظة: الأصوات القصيرة (WAV) مثل الرصاص لا يتم إيقافها قسراً
        // لأنها تنتهي تلقائياً خلال أجزاء من الثانية، وهذا أفضل لأداء اللعبة.
    }

    // ---------------------------------------
    // تشغيل مؤثر صوتي (SFX) مرة واحدة
    // ---------------------------------------
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

    // تشغيل ملف WAV (يتم إنشاء Clip جديد لكل صوت لضمان تداخل الطلقات)
    private static void playWav(String path) {
        new Thread(() -> {
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(path));
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                // إضافة Listener لإغلاق الموارد وتنظيف الذاكرة بعد انتهاء الصوت
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

    // تشغيل ملف MP3 كمؤثر صوتي (ليس خلفية)
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

    // ---------------------------------------
    // التحكم بموسيقى الخلفية (Loop)
    // ---------------------------------------
    public static void loopMp3(String path) {
        stopLoop(); // نوقف أي موسيقى خلفية سابقة أولاً

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

    // إيقاف اللوب (موسيقى الخلفية)
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
