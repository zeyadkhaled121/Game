package ZombieGame;


import com.cs304.lab9.AnimListener;
import com.sun.opengl.util.*;
import java.awt.*;
import javax.media.opengl.*;
import javax.swing.*;

public class ZombieAnim extends JFrame {

    public static void main(String[] args) {
        new ZombieAnim();
    }


    public ZombieAnim() {
        GLCanvas glcanvas;
        Animator animator;

        ZombieAnimListener listener = new ZombieGameListener();
        glcanvas = new GLCanvas();
        glcanvas.addGLEventListener(listener);
        glcanvas.addKeyListener(listener);
        getContentPane().add(glcanvas, BorderLayout.CENTER);
        animator = new FPSAnimator(15);
        animator.add(glcanvas);
        animator.start();

        setTitle("Zombie Apocalypse");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
        glcanvas.requestFocus();
    }
}

