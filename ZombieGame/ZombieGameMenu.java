package ZombieGame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;
import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;

public class ZombieGameMenu extends JFrame implements GLEventListener, MouseListener {

    enum GameState {
        MAIN_MENU, INSTRUCTIONS, LEVEL_SELECT, ABOUT_US, PLAYING_EASY, PLAYING_HARD
    }

    private GLCanvas canvas;
    private GameState currentState = GameState.MAIN_MENU;
    private TextRenderer textRenderer;
    private String playerName = "Player 1";
    private FPSAnimator animator;

    final int WINDOW_WIDTH = 800;
    final int WINDOW_HEIGHT = 600;

    private int currentWidth = WINDOW_WIDTH;
    private int currentHeight = WINDOW_HEIGHT;

    Texture menuBackgroundTexture;
    Texture instructionsBackground;
    Texture levelSelectImage;
    Texture gameBackgroundTexture;

   

    public ZombieGameMenu() {
        super("Zombie Apocalypse - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        canvas = new GLCanvas();
        canvas.addGLEventListener(this);
        canvas.addMouseListener(this);

        add(canvas);
        setVisible(true);

        try {
            String iconPath = "Game-main/Game Assets/logo.png";
            Image icon = Toolkit.getDefaultToolkit().getImage(iconPath);
            this.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Error setting icon image: " + e.getMessage());
        }

        animator = new FPSAnimator(canvas, 60);
        animator.start();
    }

    
    public void resetMenuMusic() {
        
    }

   
    @Override
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 24));
        try {
            menuBackgroundTexture = TextureIO.newTexture(new File("Game-main/Game Assets/background.png"), true);
            levelSelectImage = TextureIO.newTexture(new File("Game-main/Game Assets/levels.png"), true);
            gameBackgroundTexture = TextureIO.newTexture(new File("Game-main/Game Assets/wlcome.png"), true);
            instructionsBackground = TextureIO.newTexture(new File("Game-main/Game Assets/instr.png"), true);
        } catch (Exception e) {
            System.err.println("Error loading textures: " + e.getMessage());
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        gl.glViewport(0, 0, width, height);
        currentWidth = width;
        currentHeight = height;
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU glu = new GLU();
        glu.gluOrtho2D(0.0, width, height, 0.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        switch (currentState) {
            case MAIN_MENU:
                drawMainMenu(gl);
                break;
            case LEVEL_SELECT:
                drawLevelSelect(gl);
                break;
            case INSTRUCTIONS:
                drawInstructions(gl);
                break;
            case ABOUT_US:
                drawAboutUs(gl);
                break;
           
            case PLAYING_EASY:
            case PLAYING_HARD:
                drawGameScreen(gl, "Starting Game...");
                break;
        }
    }

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

   
    private void drawMainMenu(GL gl) {
        drawTexture(gl, menuBackgroundTexture);
        double scaleX = (double)currentWidth / WINDOW_WIDTH;
        double scaleY = (double)currentHeight / WINDOW_HEIGHT;
        drawText((int)(540 * scaleX), (int)(330 * scaleY), "PLAY", Color.WHITE, 28);
        drawText((int)(495 * scaleX), (int)(390 * scaleY), "INSTRUCTIONS", Color.WHITE, 28);
        drawText((int)(510 * scaleX), (int)(450 * scaleY), "ABOUT US", Color.WHITE, 28);
        drawText((int)(545 * scaleX), (int)(510 * scaleY), "EXIT", Color.WHITE, 28);
    }
    private void drawLevelSelect(GL gl) {
        if (levelSelectImage != null) { drawTexture(gl, levelSelectImage); }
        else { gl.glClearColor(0.1f, 0.1f, 0.2f, 1.0f); gl.glClear(GL.GL_COLOR_BUFFER_BIT); }
        gl.glDisable(GL.GL_TEXTURE_2D);
        double scaleX = (double)currentWidth / WINDOW_WIDTH;
        double scaleY = (double)currentHeight / WINDOW_HEIGHT;
        drawText(currentWidth / 2 - (int)(150 * scaleX), (int)(100 * scaleY), "Select Difficulty", Color.WHITE, 40);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }
    private void drawInstructions(GL gl) {
        if(instructionsBackground != null) { drawTexture(gl, instructionsBackground); }
        else { gl.glClearColor(0.2f, 0.1f, 0.1f, 1.0f); gl.glClear(GL.GL_COLOR_BUFFER_BIT); }
        gl.glDisable(GL.GL_TEXTURE_2D);
        double scaleX = (double)currentWidth / WINDOW_WIDTH;
        double scaleY = (double)currentHeight / WINDOW_HEIGHT;
        drawButton(gl, (int)(80 * scaleX), (int)(150 * scaleY), currentWidth - (int)(160 * scaleX), (int)(300 * scaleY), new Color(0f, 0f, 0f, 0.6f));
        drawText(currentWidth / 2 - (int)(150 * scaleX), (int)(100 * scaleY), "Instructions", Color.YELLOW, 40);
        drawText((int)(100 * scaleX), (int)(250 * scaleY), " Use Arrow Keys to Move", Color.WHITE, 24);
        drawText((int)(100 * scaleX), (int)(300 * scaleY), " Press Space to Shoot", Color.WHITE, 24);
        drawText((int)(100 * scaleX), (int)(350 * scaleY), "  Press P for Pause and Resume", Color.WHITE, 24);
        drawText((int)(100 * scaleX), (int)(400 * scaleY), "  Survive the Zombie Waves!", Color.WHITE, 24);
        int backWidth = (int)(200 * scaleX); int backHeight = (int)(40 * scaleY); int backX = currentWidth / 2 - (backWidth / 2);
        drawButton(gl, backX, (int)(500 * scaleY), backWidth, backHeight, Color.DARK_GRAY);
        drawText(currentWidth / 2 - (int)(30 * scaleX), (int)(530 * scaleY), "BACK", Color.WHITE, 24);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }
    private void drawAboutUs(GL gl) {
        if(instructionsBackground != null) { drawTexture(gl, instructionsBackground); }
        else { gl.glClearColor(0.1f, 0.2f, 0.1f, 1.0f); gl.glClear(GL.GL_COLOR_BUFFER_BIT); }
        gl.glDisable(GL.GL_TEXTURE_2D);
        double scaleX = (double)currentWidth / WINDOW_WIDTH;
        double scaleY = (double)currentHeight / WINDOW_HEIGHT;
        int boxX = (int)(80 * scaleX); int boxWidth = currentWidth - (int)(160 * scaleX); int boxHeight = (int)(300 * scaleY); int startY = (int)(150 * scaleY);
        drawButton(gl, boxX, startY, boxWidth, boxHeight, new Color(0f, 0f, 0f, 0.6f));
        int textStartX = (int)(100 * scaleX);
        drawText(currentWidth / 2 - (int)(200 * scaleX), (int)(100 * scaleY), "About Zombie Apocalypse", Color.CYAN, 36);
        drawText(textStartX, (int)(200 * scaleY), "- Developed by:", Color.WHITE, 22);
        drawText(textStartX + (int)(20 * scaleX), (int)(228 * scaleY), "Mostafa Mahmoud", Color.WHITE, 22);
        drawText(textStartX + (int)(20 * scaleX), (int)(256 * scaleY), "Zeyad Khaled", Color.WHITE, 22);
        drawText(textStartX + (int)(20 * scaleX), (int)(284 * scaleY), "Abdelfattah Mostafa", Color.WHITE, 22);
        drawText(textStartX + (int)(20 * scaleX), (int)(312 * scaleY), "Abdelaziz Rabiee", Color.WHITE, 22);
        drawText(textStartX, (int)(340 * scaleY), "- This Game developed for CS304 Project", Color.WHITE, 22);
        int backWidth = (int)(200 * scaleX); int backHeight = (int)(40 * scaleY); int backX = currentWidth / 2 - (backWidth / 2);
        drawButton(gl, backX, (int)(500 * scaleY), backWidth, backHeight, Color.DARK_GRAY);
        drawText(currentWidth / 2 - (int)(30 * scaleX), (int)(530 * scaleY), "BACK", Color.WHITE, 24);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }
    private void drawGameScreen(GL gl, String message) {
        if (gameBackgroundTexture != null) { drawTexture(gl, gameBackgroundTexture); }
        else { gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); gl.glClear(GL.GL_COLOR_BUFFER_BIT); }
        gl.glDisable(GL.GL_TEXTURE_2D);
        double scaleX = (double)currentWidth / WINDOW_WIDTH; double scaleY = (double)currentHeight / WINDOW_HEIGHT;
        int boxWidth = currentWidth - (int)(160 * scaleX); int boxHeight = (int)(150 * scaleY); int boxX = (int)(80 * scaleX); int boxY = currentHeight / 2 - (boxHeight / 2);
        drawButton(gl, boxX, boxY, boxWidth, boxHeight, new Color(0f, 0f, 0f, 0.7f));
        int previousTextX = currentWidth / 2 - (int)(150 * scaleX); int textY1 = boxY + (int)(45 * scaleY); int textY2 = boxY + (int)(105 * scaleY);
        drawText(previousTextX, textY1, "Welcome, " + playerName + "!", Color.WHITE, 36);
        drawText(previousTextX, textY2, message, Color.YELLOW, 30);
        gl.glEnable(GL.GL_TEXTURE_2D);
    }
    private void drawButton(GL gl, int x, int y, int width, int height, Color color) {
        gl.glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        gl.glBegin(GL.GL_QUADS); gl.glVertex2f(x, y); gl.glVertex2f(x + width, y); gl.glVertex2f(x + width, y + height); gl.glVertex2f(x, y + height); gl.glEnd();
    }
    private void drawText(int x, int y, String text, Color color, int size) {
        TextRenderer tempRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, size));
        tempRenderer.setColor(color); tempRenderer.beginRendering(currentWidth, currentHeight);
        tempRenderer.draw(text, x, currentHeight - y); tempRenderer.endRendering();
    }
    private void drawTexture(GL gl, Texture tex) {
        if(tex != null) {
            gl.glEnable(GL.GL_TEXTURE_2D); tex.enable(); tex.bind(); gl.glColor4f(1, 1, 1, 1);
            gl.glBegin(GL.GL_QUADS); gl.glTexCoord2f(0, 0); gl.glVertex2f(0, 0);
            gl.glTexCoord2f(1, 0); gl.glVertex2f(currentWidth, 0);
            gl.glTexCoord2f(1, 1); gl.glVertex2f(currentWidth, currentHeight);
            gl.glTexCoord2f(0, 1); gl.glVertex2f(0, currentHeight);
            gl.glEnd(); tex.disable(); gl.glDisable(GL.GL_TEXTURE_2D);
        }
    }

    
    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        double ratioX = (double)currentWidth / WINDOW_WIDTH;
        double ratioY = (double)currentHeight / WINDOW_HEIGHT;
        int scaledX = (int)(x / ratioX);
        int scaledY = (int)(y / ratioY);

        if (currentState == GameState.MAIN_MENU) {
            if (scaledX >= 450 && scaledX <= 750) {
                if (scaledY >= 300 && scaledY <= 345) { currentState = GameState.LEVEL_SELECT; }
                else if (scaledY >= 360 && scaledY <= 405) { currentState = GameState.INSTRUCTIONS; }
                else if (scaledY >= 420 && scaledY <= 465) { currentState = GameState.ABOUT_US; }
                else if (scaledY >= 480 && scaledY <= 525) { System.exit(0); }
            }
        }
        else if (currentState == GameState.INSTRUCTIONS || currentState == GameState.ABOUT_US) {
            if (scaledX >= 290 && scaledX <= 510 && scaledY >= 490 && scaledY <= 550) { currentState = GameState.MAIN_MENU; }
        }
        else if (currentState == GameState.LEVEL_SELECT) {
            if (scaledX >= 300 && scaledX <= 500) {
                // Easy Mode
                if (scaledY >= 310 && scaledY <= 370) {
                    String inputName = JOptionPane.showInputDialog(this, "Enter your Player Name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
                    if (inputName != null && !inputName.trim().isEmpty()) {
                        playerName = inputName.trim();
                        startGame(false); 
                    }
                }
                // Hard Mode
                else if (scaledY >= 400 && scaledY <= 460) {
                    String inputName = JOptionPane.showInputDialog(this, "Enter your Player Name:", "Player Name", JOptionPane.PLAIN_MESSAGE);
                    if (inputName != null && !inputName.trim().isEmpty()) {
                        playerName = inputName.trim();
                        startGame(true); 
                    }
                }
                
                else if (scaledY >= 490 && scaledY <= 550) {
                    currentState = GameState.MAIN_MENU;
                }
            }
        }
    }

    
    private void startGame(boolean isHard) {
       
        this.setVisible(false);
        
        new ZombieAnim(this, isHard, playerName);
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
}

