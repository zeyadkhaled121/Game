package ZombieGame;

import com.sun.opengl.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.media.opengl.*;
import javax.swing.*;

public class ZombieAnim extends JFrame {

    
    public static void main(String[] args) {
        
        new ZombieGameMenu();
    }

    
    public ZombieAnim(ZombieGameMenu menuReference, boolean isHardMode, String playerName) {
        GLCanvas glcanvas;
        Animator animator;

        ZombieGameListener listener = new ZombieGameListener();

        
        listener.setGameSettings(isHardMode, playerName);

        glcanvas = new GLCanvas();
        glcanvas.addGLEventListener(listener);
        glcanvas.addKeyListener(listener);
        getContentPane().add(glcanvas, BorderLayout.CENTER);

        animator = new FPSAnimator(15);
        animator.add(glcanvas);
        animator.start();

        setTitle("Zombie Apocalypse - Playing as: " + playerName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(700, 700);
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
        glcanvas.requestFocus();

        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                animator.stop(); 
                UniversalSoundPlayer.stopLoop(); 
                if (menuReference != null) {
                    menuReference.setVisible(true); 
                    menuReference.resetMenuMusic(); 
                }
            }
        });
    }
}
