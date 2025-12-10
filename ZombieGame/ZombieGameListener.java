package ZombieGame;

import ZombieGame.Texture.TextureReader;
import com.sun.opengl.util.j2d.TextRenderer;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import javax.media.opengl.*;
import java.util.ArrayList;
import java.util.BitSet;
import javax.media.opengl.glu.GLU;

public class ZombieGameListener extends ZombieAnimListener {


    class Zombie {
        float x, y;
        int animationFrame = 0;
        boolean isDead = false;
        long deathTime = 0;

        public Zombie(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    class Bullet {
        float x, y;
        float dx, dy;
        boolean isActive = true;
        int textureIndex;

        public Bullet(float x, float y, float angle) {
            this.x = x;
            this.y = y;
            this.dx = -(float) (Math.sin(Math.toRadians(angle)) * 4);
            this.dy = (float) (Math.cos(Math.toRadians(angle)) * 4);

            int angInt = (int) angle;
            if (angInt == 0) textureIndex = BULLET_UP_INDEX;
            else if (angInt == 180) textureIndex = BULLET_DOWN_INDEX;
            else if (angInt == 90) textureIndex = BULLET_LEFT_INDEX;
            else if (angInt == 270) textureIndex = BULLET_RIGHT_INDEX;
            else textureIndex = BULLET_UP_INDEX;
        }
    }


    int maxWidth = 200;
    int maxHeight = 100;

    float soldierX = maxWidth / 2.0f;
    float soldierY = maxHeight / 2.0f;
    final int SOLDIER_SPEED = 3;

    int soldierHealth = 3;
    long lastHitTime = 0;
    final long HIT_INVINCIBILITY_MS = 2000;

    long gameStartTime = 0;
    final int SURVIVAL_TIME_SECONDS = 30;
    boolean gameWon = false;
    boolean winSoundPlayed = false;

    int finalTimeRemaining = 0;

    float soldierAngle = 0;
    int soldierAnimIndex = 0;
    boolean isSoldierMoving = false;

    ArrayList<Zombie> zombies = new ArrayList<>();
    ArrayList<Bullet> bullets = new ArrayList<>();

    int MAX_ZOMBIES = 5;
    float ZOMBIE_SPEED = 0.5f;
    String playerName = "Player";

    int score = 0;
    TextRenderer textRenderer;

    boolean gameOver = false;
    boolean gameOverSoundPlayed = false;
    boolean isPaused = false;

    String textureNames[] = {
            "Zombie_Walk1.png", "Zombie_Walk2.png", "Zombie_Walk3.png", "Zombie_Walk4.png", "Zombie_Walk5.png",
            "Zombie_Walk6.png", "Zombie_Walk7.png", "Zombie_Walk8.png", "Zombie_Walk9.png", "Zombie_Walk10.png",
            "Man1.png", "Man2.png", "Man3.png", "Man4.png",
            "BG.png",
            "ARM1.png", "ARM2.png", "ARM3.png", "ARM4.png",
            "Zombie Dead.png",
            "h1.png", "h2.png", "h3.png"
    };

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    final int ZOMBIE_START_INDEX = 0;
    final int SOLDIER_START_INDEX = 10;
    final int SOLDIER_FRAMES_COUNT = 4;
    final int BACKGROUND_INDEX = 14;

    final int BULLET_RIGHT_INDEX = 15;
    final int BULLET_LEFT_INDEX = 16;
    final int BULLET_DOWN_INDEX = 17;
    final int BULLET_UP_INDEX = 18;
    final int ZOMBIE_DEAD_INDEX = 19;

    final int HEART_FULL_INDEX = 20;
    final int HEART_TWO_INDEX = 21;
    final int HEART_ONE_INDEX = 22;

    long lastFireTime = 0;
    long lastStepTime = 0;
    final int STEP_INTERVAL = 200;

    final String BACKGROUND_MP3 = "soundeffects/8bit-music-for-game-68698.mp3";
    final String GAMEOVER_WAV = "soundeffects/mixkit-retro-arcade-game-over-470.wav";
    final String GUNSHOT_MP3 = "soundeffects/mixkit-game-gun-shot-1662.mp3";
    final String WIN_SOUND_MP3 = "soundeffects/level-up-47165.mp3";



    public void setGameSettings(boolean isHard, String name) {
        this.playerName = name;
        if (isHard) {
            this.MAX_ZOMBIES = 15;
            this.ZOMBIE_SPEED = 0.8f;
        } else {
            this.MAX_ZOMBIES = 5;
            this.ZOMBIE_SPEED = 0.4f;
        }
    }

    @Override
    public void init(GLAutoDrawable gld) {
        UniversalSoundPlayer.loopMp3(BACKGROUND_MP3);

        soldierHealth = 3;
        gameOver = false;
        gameWon = false;
        gameOverSoundPlayed = false;
        winSoundPlayed = false;
        score = 0;
        gameStartTime = System.currentTimeMillis();
        finalTimeRemaining = SURVIVAL_TIME_SECONDS;

        GL gl = gld.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);

        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 18));

        for (int i = 0; i < textureNames.length; i++) {
            try {
                texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i], true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D, GL.GL_RGBA, texture[i].getWidth(),
                        texture[i].getHeight(), GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels()
                );
            } catch (IOException e) {
                System.out.println("Error loading texture: " + textureNames[i]);
                e.printStackTrace();
            }
        }

        zombies.clear();
        for (int i = 0; i < MAX_ZOMBIES; i++) {
            zombies.add(new Zombie(-100, -100));
            resetZombie(zombies.get(i));
        }
    }

    @Override
    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // 1. Draw Background
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[BACKGROUND_INDEX]);
        gl.glPushMatrix();
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(maxWidth, 0.0f, 0.0f);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(maxWidth, maxHeight, 0.0f);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(0.0f, maxHeight, 0.0f);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);

        if (!gameOver && !isPaused && !gameWon) {
            handleLogic();
        }

        // 2. Draw Sprites (Soldier, Bullets, Zombies)
        int currentSoldierFrame = SOLDIER_START_INDEX;
        if (isSoldierMoving && !isPaused && !gameOver && !gameWon) {
            currentSoldierFrame = SOLDIER_START_INDEX + ((soldierAnimIndex / 3) % SOLDIER_FRAMES_COUNT);
            soldierAnimIndex++;
        }

        if (System.currentTimeMillis() - lastHitTime < HIT_INVINCIBILITY_MS && !gameOver && !gameWon) {
            gl.glColor3f(1.0f, 0.5f, 0.5f);
        } else {
            gl.glColor3f(1.0f, 1.0f, 1.0f);
        }
        DrawSprite(gl, soldierX, soldierY, currentSoldierFrame, 1, soldierAngle);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        for (Bullet b : bullets) {
            if (b.isActive) DrawSprite(gl, b.x, b.y, b.textureIndex, 0.3f, 0);
        }

        for (Zombie z : zombies) {
            double angleToSoldier = Math.toDegrees(Math.atan2(soldierY - z.y, soldierX - z.x));
            if (z.isDead) {
                DrawSprite(gl, z.x, z.y, ZOMBIE_DEAD_INDEX, 1, (float) angleToSoldier);
            } else {
                int currentFrame = ZOMBIE_START_INDEX + ((z.animationFrame / 5) % 10);
                DrawSprite(gl, z.x, z.y, currentFrame, 1, (float) angleToSoldier);
                if (!gameOver && !isPaused && !gameWon) z.animationFrame++;
            }
        }


        if (gameOver) {
            drawGameOverOverlay(gl);
        }

        // 4. Draw UI
        drawScore(gld);
        drawHealth(gld);
    }

    private void drawGameOverOverlay(GL gl) {
        gl.glEnable(GL.GL_BLEND);
        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glColor4f(1.0f, 0.0f, 0.0f, 0.5f);

        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(0, 0, 0);
        gl.glVertex3f(maxWidth, 0, 0);
        gl.glVertex3f(maxWidth, maxHeight, 0);
        gl.glVertex3f(0, maxHeight, 0);
        gl.glEnd();

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }

    private void handleLogic() {
        int timeElapsed = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
        int currentTimeRemaining = SURVIVAL_TIME_SECONDS - timeElapsed;

        if (currentTimeRemaining <= 0 && !gameWon) {
            gameWon = true;
            UniversalSoundPlayer.stopLoop();
            if (!winSoundPlayed) {
                UniversalSoundPlayer.play(WIN_SOUND_MP3);
                winSoundPlayed = true;
                ScoreManager.checkAndSaveScore(score);
            }
            return;
        }

        // --- Game Controls ---
        isSoldierMoving = false;
        if (isKeyPressed(KeyEvent.VK_RIGHT) && soldierX < maxWidth - 5) {
            soldierX += SOLDIER_SPEED; soldierAngle = 270; isSoldierMoving = true;
        }
        if (isKeyPressed(KeyEvent.VK_LEFT) && soldierX > 5) {
            soldierX -= SOLDIER_SPEED; soldierAngle = 90; isSoldierMoving = true;
        }
        if (isKeyPressed(KeyEvent.VK_UP) && soldierY < maxHeight - 5) {
            soldierY += SOLDIER_SPEED; soldierAngle = 0; isSoldierMoving = true;
        }
        if (isKeyPressed(KeyEvent.VK_DOWN) && soldierY > 5) {
            soldierY -= SOLDIER_SPEED; soldierAngle = 180; isSoldierMoving = true;
        }

        if (isKeyPressed(KeyEvent.VK_SPACE)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFireTime > 200) {
                float gunOffsetDistance = 8.0f;
                float rad = (float) Math.toRadians(soldierAngle);
                bullets.add(new Bullet(soldierX - (float) Math.sin(rad) * gunOffsetDistance,
                        soldierY + (float) Math.cos(rad) * gunOffsetDistance, soldierAngle));
                UniversalSoundPlayer.play(GUNSHOT_MP3);
                lastFireTime = currentTime;
            }
        }

        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.x += b.dx; b.y += b.dy;
            if (b.x < 0 || b.x > maxWidth || b.y < 0 || b.y > maxHeight) {
                bullets.remove(i); i--;
            }
        }

        long currentTime = System.currentTimeMillis();
        for (Zombie z : zombies) {
            if (z.isDead) {
                if (currentTime - z.deathTime > 500) {
                    z.isDead = false; resetZombie(z);
                }
                continue;
            }

            float dx = soldierX - z.x;
            float dy = soldierY - z.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                z.x += (dx / distance) * ZOMBIE_SPEED;
                z.y += (dy / distance) * ZOMBIE_SPEED;
            }

            if (distance < 5.0) {
                if (currentTime - lastHitTime > HIT_INVINCIBILITY_MS) {
                    soldierHealth--;
                    lastHitTime = currentTime;
                    soldierX = maxWidth / 2.0f; soldierY = maxHeight / 2.0f;

                    if (soldierHealth <= 0) {
                        if (!gameOver) {
                            gameOver = true;

                            finalTimeRemaining = Math.max(0, currentTimeRemaining);

                            UniversalSoundPlayer.stopLoop();
                            if (!gameOverSoundPlayed) {
                                UniversalSoundPlayer.play(GAMEOVER_WAV);
                                gameOverSoundPlayed = true;
                                ScoreManager.checkAndSaveScore(score);
                            }
                        }
                    }
                }
            }
        }
        checkCollisions();
    }

    private void checkCollisions() {
        float hitDistance = 8.0f;
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            if (!b.isActive) continue;
            for (Zombie z : zombies) {
                if (z.isDead) continue;
                if (Math.sqrt(Math.pow(b.x - z.x, 2) + Math.pow(b.y - z.y, 2)) < hitDistance) {
                    b.isActive = false; bullets.remove(i); i--;
                    score += 50; z.isDead = true; z.deathTime = System.currentTimeMillis();
                    break;
                }
            }
        }
    }

    private void resetZombie(Zombie z) {
        int side = (int) (Math.random() * 4);
        switch (side) {
            case 0 -> { z.x = (float) (Math.random() * maxWidth); z.y = maxHeight + 10; }
            case 1 -> { z.x = (float) (Math.random() * maxWidth); z.y = -10; }
            case 2 -> { z.x = maxWidth + 10; z.y = (float) (Math.random() * maxHeight); }
            case 3 -> { z.x = -10; z.y = (float) (Math.random() * maxHeight); }
        }
    }

    public void DrawSprite(GL gl, float x, float y, int index, float scale, float angle) {
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);
        gl.glPushMatrix();
        gl.glTranslated(x, y, 0);
        float adjustedScale = scale * 4.0f;
        gl.glScaled(adjustedScale, adjustedScale, 1);
        gl.glRotated(angle, 0, 0, 1);
        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(0.0f, 0.0f); gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 0.0f); gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glTexCoord2f(1.0f, 1.0f); gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glTexCoord2f(0.0f, 1.0f); gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glDisable(GL.GL_BLEND);
    }

    private void drawHealth(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        int heartTextureIndex;
        switch (soldierHealth) {
            case 3 -> heartTextureIndex = HEART_FULL_INDEX;
            case 2 -> heartTextureIndex = HEART_TWO_INDEX;
            case 1 -> heartTextureIndex = HEART_ONE_INDEX;
            default -> { return; }
        }
        DrawSprite(gl, maxWidth - 15f, maxHeight - 10f, heartTextureIndex, 0.5f, 0);
    }

    private void drawScore(GLAutoDrawable gld) {
        textRenderer.beginRendering(gld.getWidth(), gld.getHeight());
        int centerX = gld.getWidth() / 2;
        int centerY = gld.getHeight() / 2;

        int displayTime;
        if (gameOver) {
            displayTime = finalTimeRemaining;
        } else if (gameWon) {
            displayTime = 0;
        } else {
            int timeElapsed = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
            displayTime = Math.max(0, SURVIVAL_TIME_SECONDS - timeElapsed);
        }

        textRenderer.setColor(Color.YELLOW);
        textRenderer.draw("Time: " + displayTime + "s", centerX - 40, gld.getHeight() - 30);

        if (gameOver) {
            textRenderer.setColor(Color.YELLOW);
            textRenderer.draw("GAME OVER", centerX - 60, centerY + 50);

            textRenderer.setColor(Color.WHITE);
            textRenderer.draw("Player: " + playerName, centerX - 70, centerY + 10);
            textRenderer.draw("Final Score: " + score, centerX - 70, centerY - 20);
            textRenderer.draw("High Score: " + ScoreManager.getHighScore(), centerX - 70, centerY - 50);

        } else if (gameWon) {
            textRenderer.setColor(Color.GREEN);
            textRenderer.draw("SURVIVED!", centerX - 60, centerY + 50);
            textRenderer.setColor(Color.WHITE);
            textRenderer.draw("Player: " + playerName, centerX - 70, centerY + 10);
            textRenderer.draw("Final Score: " + score, centerX - 70, centerY - 20);
            textRenderer.draw("High Score: " + ScoreManager.getHighScore(), centerX - 70, centerY - 50);
        } else if (isPaused) {
            textRenderer.setColor(Color.ORANGE);
            textRenderer.draw("PAUSED", centerX - 40, centerY + 20);
            textRenderer.setColor(Color.WHITE);
            textRenderer.draw("Score: " + score, 10, gld.getHeight() - 30);
        } else {
            textRenderer.setColor(Color.WHITE);
            textRenderer.draw("Score: " + score, 10, gld.getHeight() - 30);
        }
        textRenderer.endRendering();
    }


    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        new GLU().gluOrtho2D(0.0, maxWidth, 0.0, maxHeight);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    public BitSet keyBits = new BitSet(256);

    @Override
    public void keyPressed(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.VK_P) {
            if (!gameOver && !gameWon) {
                isPaused = !isPaused;
                if (isPaused) UniversalSoundPlayer.stopLoop();
                else UniversalSoundPlayer.loopMp3(BACKGROUND_MP3);
            }
        }
        if (keyCode < 256) keyBits.set(keyCode);
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode < 256) keyBits.clear(keyCode);
    }

    @Override
    public void keyTyped(final KeyEvent event) {}

    public boolean isKeyPressed(final int keyCode) {
        if (keyCode >= 256) return false;
        return keyBits.get(keyCode);
    }
}

