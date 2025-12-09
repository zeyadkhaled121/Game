package ZombieGame;

import java.io.*;
import java.util.Scanner;

public class ScoreManager {
    private static final String FILE_PATH = "highscore.txt";


    public static void checkAndSaveScore(int newScore) {
        int currentHighScore = getHighScore();
        if (newScore > currentHighScore) {
            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                writer.write(String.valueOf(newScore));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static int getHighScore() {
        try (Scanner scanner = new Scanner(new File(FILE_PATH))) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            }
        } catch (FileNotFoundException e) {
            return 0; 
        }
        return 0;
    }
}