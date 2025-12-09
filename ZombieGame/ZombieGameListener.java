package ZombieGame;

import com.cs304.lab9.AnimListener;
import com.cs304.lab9.Texture.TextureReader;
import java.awt.event.*;
import java.io.IOException;
import javax.media.opengl.*;
import java.util.ArrayList;
import java.util.BitSet;
import javax.media.opengl.glu.GLU;

public class ZombieGameListener extends ZombieAnimListener {

    // ====================================================
    // 1. الكلاسات المساعدة
    // ====================================================

    class Zombie {
        float x, y;
        int animationFrame = 0;

        public Zombie(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    // [التعديل 2]: تحديث كلاس Bullet
    class Bullet {
        float x, y;
        float dx, dy;
        boolean isActive = true;
        int textureIndex; // متغير جديد لتخزين رقم صورة الطلقة المناسب

        public Bullet(float x, float y, float angle) {
            this.x = x;
            this.y = y;

            // حسابات الحركة (كما هي من الكود السابق المظبوط)
            this.dx = -(float) (Math.sin(Math.toRadians(angle)) * 4);
            this.dy = (float) (Math.cos(Math.toRadians(angle)) * 4);

            // تحديد الصورة المناسبة بناءً على الزاوية (نظامك: 0=فوق، 90=يسار، 180=تحت، 270=يمين)
            int angInt = (int) angle;
            if (angInt == 0) {
                textureIndex = BULLET_UP_INDEX;    // ARM4
            } else if (angInt == 180) {
                textureIndex = BULLET_DOWN_INDEX;  // ARM3
            } else if (angInt == 90) {
                textureIndex = BULLET_LEFT_INDEX;  // ARM1
            } else if (angInt == 270) {
                textureIndex = BULLET_RIGHT_INDEX; // ARM2
            } else {
                textureIndex = BULLET_UP_INDEX; // Default
            }
        }
    }

    // ====================================================
    // 2. المتغيرات الرئيسية
    // ====================================================

    int maxWidth = 200;
    int maxHeight = 100;

    float soldierX = maxWidth / 2;
    float soldierY = maxHeight / 2;
    final int SOLDIER_SPEED = 3;

    float soldierAngle = 0; // (0=فوق، 270=يمين، 180=تحت، 90=يسار)

    int soldierAnimIndex = 0;
    boolean isSoldierMoving = false;

    ArrayList<Zombie> zombies = new ArrayList<>();
    ArrayList<Bullet> bullets = new ArrayList<>();

    int MAX_ZOMBIES = 15;
    float ZOMBIE_SPEED = 0.5f;
    boolean gameOver = false;

    // [التعديل 1]: إضافة صور الطلقات الجديدة
    String textureNames[] = {
            "Zombie_Walk1.png", "Zombie_Walk2.png", "Zombie_Walk3.png", "Zombie_Walk4.png", "Zombie_Walk5.png",
            "Zombie_Walk6.png", "Zombie_Walk7.png", "Zombie_Walk8.png", "Zombie_Walk9.png", "Zombie_Walk10.png", // 0-9
            "Man1.png", "Man2.png", "Man3.png", "Man4.png", // 10-13
            "BG.png", // 14
            "ARM2.png", // 15 (شمال)
            "ARM1.png", // 16 (يمين)
            "ARM3.png", // 17 (تحت)
            "ARM4.png"  // 18 (فوق)
    };

    TextureReader.Texture texture[] = new TextureReader.Texture[textureNames.length];
    int textures[] = new int[textureNames.length];

    final int ZOMBIE_START_INDEX = 0;
    final int SOLDIER_START_INDEX = 10;
    final int SOLDIER_FRAMES_COUNT = 4;
    final int BACKGROUND_INDEX = 14;

    // تعريف مؤشرات الطلقات الجديدة
    final int BULLET_LEFT_INDEX = 15;
    final int BULLET_RIGHT_INDEX = 16;
    final int BULLET_DOWN_INDEX = 17;
    final int BULLET_UP_INDEX = 18;

    long lastFireTime = 0;

    // ====================================================
    // 3. دالة التهيئة (init)
    // ====================================================
    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glGenTextures(textureNames.length, textures, 0);

        for(int i = 0; i < textureNames.length; i++){
            try {
                // تأكد من وجود كل الصور الجديدة في فولدر الـ Assets
                texture[i] = TextureReader.readTexture(assetsFolderName + "//" + textureNames[i] , true);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[i]);
                new GLU().gluBuild2DMipmaps(
                        GL.GL_TEXTURE_2D, GL.GL_RGBA, texture[i].getWidth(), texture[i].getHeight(), GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, texture[i].getPixels()
                );
            } catch( IOException e ) {
                System.out.println("Error loading texture: " + textureNames[i]);
                System.out.println(e);
                e.printStackTrace();
            }
        }

        for (int i = 0; i < MAX_ZOMBIES; i++) {
            zombies.add(new Zombie(-100, -100));
            resetZombie(zombies.get(i));
        }
    }

    // ====================================================
    // 4. دالة الرسم (display)
    // ====================================================
    public void display(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glLoadIdentity();

        // رسم الخلفية
        DrawSprite(gl, maxWidth/2, maxHeight/2, BACKGROUND_INDEX, 10, 0);

        if (!gameOver) {
            handleLogic();
        }

        // رسم الجندي
        int currentSoldierFrame = SOLDIER_START_INDEX;
        if (isSoldierMoving) {
            currentSoldierFrame = SOLDIER_START_INDEX + ((soldierAnimIndex / 3) % SOLDIER_FRAMES_COUNT);
            if(!gameOver) soldierAnimIndex++;
        }
        DrawSprite(gl, soldierX, soldierY, currentSoldierFrame, 1, soldierAngle);

        // [التعديل 3]: تحديث رسم الطلقات
        for (Bullet b : bullets) {
            if (b.isActive) {
                // نستخدم b.textureIndex الذي حددناه عند الإنشاء
                // ونضع الزاوية 0 لأن الصورة نفسها مجهزة بالاتجاه الصحيح
                DrawSprite(gl, b.x, b.y, b.textureIndex, 0.3f, 0);
            }
        }

        // رسم الزومبي
        for (Zombie z : zombies) {
            int currentFrame = ZOMBIE_START_INDEX + ((z.animationFrame / 5) % 10);
            double angleToSoldier = Math.toDegrees(Math.atan2(soldierY - z.y, soldierX - z.x));
            DrawSprite(gl, z.x, z.y, currentFrame, 1, (float)angleToSoldier);
            if (!gameOver) z.animationFrame++;
        }
    }

    // ====================================================
    // 5. منطق اللعبة (لم يتغير)
    // ====================================================
    private void handleLogic() {
        isSoldierMoving = false;

        // (0=فوق، 270=يمين، 180=تحت، 90=يسار)
        if (isKeyPressed(KeyEvent.VK_RIGHT) && soldierX < maxWidth - 5) {
            soldierX += SOLDIER_SPEED;
            soldierAngle = 270;
            isSoldierMoving = true;
        }
        if (isKeyPressed(KeyEvent.VK_LEFT) && soldierX > 5) {
            soldierX -= SOLDIER_SPEED;
            soldierAngle = 90;
            isSoldierMoving = true;
        }
        if (isKeyPressed(KeyEvent.VK_UP) && soldierY < maxHeight - 5) {
            soldierY += SOLDIER_SPEED;
            soldierAngle = 0;
            isSoldierMoving = true;
        }
        if (isKeyPressed(KeyEvent.VK_DOWN) && soldierY > 5) {
            soldierY -= SOLDIER_SPEED;
            soldierAngle = 180;
            isSoldierMoving = true;
        }

        // إطلاق النار
        if (isKeyPressed(KeyEvent.VK_SPACE)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFireTime > 200) {

                float gunOffsetDistance = 8.0f;
                float rad = (float) Math.toRadians(soldierAngle);

                // الحسابات السليمة من الكود السابق
                float bulletStartX = soldierX - (float)Math.sin(rad) * gunOffsetDistance;
                float bulletStartY = soldierY + (float)Math.cos(rad) * gunOffsetDistance;

                bullets.add(new Bullet(bulletStartX, bulletStartY, soldierAngle));
                lastFireTime = currentTime;
            }
        }

        // تحديث الطلقات
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.x += b.dx;
            b.y += b.dy;

            if (b.x < 0 || b.x > maxWidth || b.y < 0 || b.y > maxHeight) {
                bullets.remove(i);
                i--;
            }
        }

        // تحديث الزومبي
        for (Zombie z : zombies) {
            float dx = soldierX - z.x;
            float dy = soldierY - z.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                z.x += (dx / distance) * ZOMBIE_SPEED;
                z.y += (dy / distance) * ZOMBIE_SPEED;
            }

            if (distance < 5.0) {
                gameOver = true;
                System.out.println("GAME OVER!");
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
                double dist = Math.sqrt(Math.pow(b.x - z.x, 2) + Math.pow(b.y - z.y, 2));
                if (dist < hitDistance) {
                    b.isActive = false;
                    bullets.remove(i);
                    i--;
                    resetZombie(z);
                    break;
                }
            }
        }
    }

    private void resetZombie(Zombie z) {
        int side = (int) (Math.random() * 4);
        switch (side) {
            case 0: z.x = (float)(Math.random()*maxWidth); z.y = maxHeight+10; break;
            case 1: z.x = (float)(Math.random()*maxWidth); z.y = -10; break;
            case 2: z.x = maxWidth+10; z.y = (float)(Math.random()*maxHeight); break;
            case 3: z.x = -10; z.y = (float)(Math.random()*maxHeight); break;
        }
    }

    // ====================================================
    // 6. دالة الرسم (لم تتغير)
    // ====================================================
    public void DrawSprite(GL gl, float x, float y, int index, float scale, float angle){
        gl.glEnable(GL.GL_BLEND);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[index]);

        gl.glPushMatrix();
        gl.glTranslated( x/(maxWidth/2.0) - 0.9, y/(maxHeight/2.0) - 0.9, 0);
        gl.glScaled(0.1 * scale, 0.1 * scale, 1);
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

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    public BitSet keyBits = new BitSet(256);
    @Override
    public void keyPressed(final KeyEvent event) { keyBits.set(event.getKeyCode()); }
    @Override
    public void keyReleased(final KeyEvent event) { keyBits.clear(event.getKeyCode()); }
    @Override
    public void keyTyped(final KeyEvent event) {}
    public boolean isKeyPressed(final int keyCode) { return keyBits.get(keyCode); }
}