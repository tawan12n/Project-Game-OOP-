import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Meteor extends GameObject {

    private Handler handler;
    private Random r = new Random();
    private int choose = 0;
    private int hp = 100;

    private int originalX, originalY; 
    private long deathTime = -1; // Time when the meteor was destroyed
    private BufferedImage Meteor1;
    private MovementThread movementThread;

    public Meteor(int x, int y, ID id, Handler handler,SpriteSheet ss) {
        super(x, y, id, ss);
        movementThread = new MovementThread(this, 2); // อัพเดต 60 ครั้งต่อวินาที
        movementThread.start();
        this.handler = handler;

        // Store the original position
        this.originalX = x;
        this.originalY = y;

        velX = (r.nextInt(3) - 1) * 1.0f;
        velY = (r.nextInt(3) - 1) * 1.0f;
        Meteor1 = ss.grabImage(4, 8, 32, 32);
    }

    @Override
    public void tick() {
        
        if (hp <= 0) {
            if (deathTime == -1) {
                deathTime = System.currentTimeMillis(); // Record the time of destruction
            } else {
                long currentTime = System.currentTimeMillis();
                if (currentTime - deathTime >= 5000) { // 10 seconds
                    respawn();
                }
            }
            return; // Skip further actions when destroyed
        }

        // Update position
        x += velX;
        y += velY;

        // Random movement and collision handling
        choose = r.nextInt(10);

        for (int i = 0; i < handler.object.size(); i++) {
            GameObject tempObject = handler.object.get(i);

            if (tempObject.getId() == ID.Block) {
                if (getBoundsBig().intersects(tempObject.getBounds())) {
                    // Reverse velocity on collision and set to zero to stop
                    x += (velX * 2) * -1;
                    y += (velY * 2) * -1;
                    velX = 0;
                    velY = 0;
                } else if (choose == 0) {
                    // Assign new random movement with small values for subtle motion
                    velX = (r.nextInt(3) - 1) * 1.0f; // -1, 0, or 1 for horizontal
                    velY = (r.nextInt(3) - 1) * 1.0f; // -1, 0, or 1 for vertical
                }
            }

            if (tempObject.getId() == ID.Bullet) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    hp -= 50;
                    handler.removeObject(tempObject);
                }
            }
        }
    }

    @Override
    public void render(Graphics g) {
        if (hp > 0) {
        	g.drawImage(Meteor1, x, y,75,75, null);
//            g.setColor(Color.YELLOW);
//            g.fillRect((int) x, (int) y, 32, 32);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, 32, 32);
    }

    public Rectangle getBoundsBig() {
        return new Rectangle((int) x - 16, (int) y - 16, 64, 64);
    }

    // Respawn the meteor at the original position
    private void respawn() {
        this.x = originalX;
        this.y = originalY;
        this.hp = 100; // Restore health
        this.velX = (r.nextInt(3) - 1) * 1.0f; // Assign new random velocity
        this.velY = (r.nextInt(3) - 1) * 1.0f;
        deathTime = -1; // Reset death time
    }
}
