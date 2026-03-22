import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

class Bullet {
    double x, y;
    double dx, dy;

    Bullet(double x, double y, double dx, double dy) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
    }

    void move() {
        x += dx;
        y += dy;
    }
}

public class SpaceShuttleGame extends JPanel implements Runnable, KeyListener, MouseListener {

    int x = 450, y = 400;
    int xSpeed = 0, ySpeed = 0;

    Image rocket, bg;

    ArrayList<Bullet> bullets = new ArrayList<>();
    ArrayList<Rectangle> enemies = new ArrayList<>();

    int score = 0;
    int lives = 3;

    String gameState = "START";
    int delay = 0;

    public SpaceShuttleGame() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        rocket = new ImageIcon("ufo.png").getImage();
        bg = new ImageIcon("bg.jpg").getImage();

        new Thread(this).start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

        g.setColor(Color.WHITE);

        // START SCREEN
        if (gameState.equals("START")) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("SPACE SHOOTER", getWidth()/2 - 180, getHeight()/2 - 50);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press ENTER to Start", getWidth()/2 - 120, getHeight()/2);
            g.drawString("Mouse Click = Shoot | Arrow Keys = Move", getWidth()/2 - 170, getHeight()/2 + 40);
            return;
        }

        // PLAYER
        g.drawImage(rocket, x, y, 100, 100, this);

        // BULLETS
        g.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            g.fillOval((int)b.x, (int)b.y, 10, 10);
        }

        // ENEMIES (SLOW + SMOOTH)
        g.setColor(Color.RED);
        for (Rectangle e : enemies) {
            g.fillRoundRect(e.x, e.y, e.width, e.height, 15, 15);
        }

        // UI
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Lives: " + lives, 20, 55);

        // GAME OVER
        if (gameState.equals("GAME_OVER")) {
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", getWidth()/2 - 160, getHeight()/2);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press R to Restart", getWidth()/2 - 100, getHeight()/2 + 40);
        }
    }

    public void run() {
        while (true) {

            if (gameState.equals("PLAYING")) {

                x += xSpeed;
                y += ySpeed;

                // Boundaries
                x = Math.max(0, Math.min(x, getWidth() - 100));
                y = Math.max(0, Math.min(y, getHeight() - 100));

                // Move bullets
                for (int i = 0; i < bullets.size(); i++) {
                    Bullet b = bullets.get(i);
                    b.move();

                    if (b.x < 0 || b.x > getWidth() || b.y < 0 || b.y > getHeight()) {
                        bullets.remove(i);
                        i--;
                    }
                }

                delay++;

                // ✅ SLOW + LIMITED ENEMY SPAWN
                if (delay > 120 && Math.random() < 0.01 && enemies.size() < 5) {
                    int ex = (int)(Math.random() * (getWidth() - 50));
                    enemies.add(new Rectangle(ex, 0, 50, 50));
                }

                // ✅ SLOW ENEMY MOVEMENT
                for (int i = 0; i < enemies.size(); i++) {
                    Rectangle e = enemies.get(i);

                    e.y += 1.5; // slower fall
                    e.x += Math.sin(e.y * 0.05) * 1; // gentle movement

                    if (e.y > getHeight()) {
                        enemies.remove(i);
                        lives--;
                        i--;

                        if (lives <= 0) {
                            gameState = "GAME_OVER";
                        }
                    }
                }

                // Collision
                for (int i = 0; i < bullets.size(); i++) {
                    Bullet b = bullets.get(i);
                    Rectangle bulletRect = new Rectangle((int)b.x, (int)b.y, 10, 10);

                    for (int j = 0; j < enemies.size(); j++) {
                        Rectangle e = enemies.get(j);

                        if (bulletRect.intersects(e)) {
                            bullets.remove(i);
                            enemies.remove(j);
                            score += 10;
                            i--;
                            break;
                        }
                    }
                }
            }

            repaint();

            try {
                Thread.sleep(15);
            } catch (Exception e) {}
        }
    }

    // KEYBOARD
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState.equals("PLAYING")) {
            if (key == KeyEvent.VK_LEFT) xSpeed = -6;
            if (key == KeyEvent.VK_RIGHT) xSpeed = 6;
            if (key == KeyEvent.VK_UP) ySpeed = -6;
            if (key == KeyEvent.VK_DOWN) ySpeed = 6;
        }

        if (key == KeyEvent.VK_ENTER && gameState.equals("START")) {
            gameState = "PLAYING";
            delay = 0;
        }

        if (key == KeyEvent.VK_R && gameState.equals("GAME_OVER")) {
            score = 0;
            lives = 3;
            bullets.clear();
            enemies.clear();
            gameState = "PLAYING";
            delay = 0;
        }
    }

    public void keyReleased(KeyEvent e) {
        xSpeed = 0;
        ySpeed = 0;
    }

    public void keyTyped(KeyEvent e) {}

    // 🖱️ MOUSE AIM SHOOTING
    public void mousePressed(MouseEvent e) {
        if (gameState.equals("PLAYING")) {

            double mouseX = e.getX();
            double mouseY = e.getY();

            double centerX = x + 50;
            double centerY = y + 50;

            double dx = mouseX - centerX;
            double dy = mouseY - centerY;

            double distance = Math.sqrt(dx * dx + dy * dy);

            dx = (dx / distance) * 8; // slower bullet speed
            dy = (dy / distance) * 8;

            bullets.add(new Bullet(centerX, centerY, dx, dy));
        }
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("🚀 Space Shooter Game");

        SpaceShuttleGame game = new SpaceShuttleGame();

        frame.add(game);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}