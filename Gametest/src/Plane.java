import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Plane extends GameObject {

    private Handler handler;
    private Game game;
    private int health = 100;  // Starting health
    private double shields = 0.0; // Starting shields
    private BufferedImage plane;
    private int angle = 0; // Rotation angle
    private int baseSpeed = 7; // Default movement speed
    private int turnSpeedBoost = 1; // Additional speed while turning
    private int speed = baseSpeed;
    private float turnSpeed = 2; // Speed at which the plane turns
    private Integer lastHitX = null, lastHitY = null; // Last hit position for rendering red marker
    private MovementThread movementThread;
    

    public Plane(int x, int y, ID id, Handler handler, Game game, SpriteSheet ss) {
        super(x, y, id, ss);
        movementThread = new MovementThread(this, 2); // อัพเดต 60 ครั้งต่อวินาที
        movementThread.start();
        this.handler = handler;
        this.game = game;
        plane = ss.grabImage(1, 1, 32, 32); // Assumes plane sprite is at (1,1) in the sprite sheet
        
    }

    @Override
    public void tick() {
        // Update speed if turning
        if (handler.isLeft() || handler.isRight()) {
            speed = baseSpeed + turnSpeedBoost;
        } else {
            speed = baseSpeed;
        }

        // Update rotation angle
        if (handler.isLeft()) {
            angle -= turnSpeed;
        }
        if (handler.isRight()) {
            angle += turnSpeed;
        }

        // Calculate velocity based on angle and speed
        velX = (int) (Math.cos(Math.toRadians(angle - 90)) * speed);
        velY = (int) (Math.sin(Math.toRadians(angle - 90)) * speed);

        // Update plane position
        x += velX;
        y += velY;

        // Check for collisions
        collision();
    }

    private void collision() {
        for (int i = 0; i < handler.object.size(); i++) {
            GameObject tempObject = handler.object.get(i);

            if (tempObject.getId() == ID.Block) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    // Simple collision response
                    x -= velX;
                    y -= velY;
                }
            }

            if (tempObject.getId() == ID.Crate) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    game.ammo += 15;
                    handler.removeObject(tempObject);
                }
            }

            if (tempObject.getId() == ID.Meteor) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    if (shields > 0) {
                        shields -= 0.5;
                        if (shields < 0) shields = 0;
                    } else {
                        health -= 10;
                        if (health < 0) health = 0;
                    }
                    handler.removeObject(tempObject);
                }
            }

            if (tempObject.getId() == ID.Shield) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    shields++;
                    handler.removeObject(tempObject);
                }
            }

            if (tempObject.getId() == ID.Missile) {
                if (getBounds().intersects(tempObject.getBounds())) {
                    lastHitX = tempObject.getX();
                    lastHitY = tempObject.getY();
                    takeDamage(((Missile) tempObject).getDamage());
                    handler.removeObject(tempObject);
                    break;
                }
            }
        }
    }

    // Method to handle damage and shield depletion
    public void takeDamage(int damage) {
        if (health <= 0) return; // หยุดการลด health ถ้า health ถึง 0 แล้ว

        if (shields > 0) {
            shields -= 1;
            if (shields < 0) shields = 0;
            System.out.println("Shield hit! Remaining shields: " + shields);
        } else {
            health -= damage;
            if (health < 0) health = 0;
            System.out.println("Health reduced by " + damage + "! Remaining health: " + health);

            if (health == 0) {
                System.out.println("Player has been defeated.");
                // เพิ่มการหยุดหรือจัดการสถานะเมื่อ health เหลือ 0 เช่น เปลี่ยนสถานะเกมเป็น lose
                // เช่น game.setGameState(GameState.LOSE);
            }
        }
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        int width = 48;
        int height = 48;
        int yOffset = 10;
        int xOffset = 6;

        // Translate to center and rotate
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(angle));

        // Draw the plane image with offset for alignment in the hitbox
        g2d.drawImage(plane, -width / 2 - xOffset, -height / 2 - yOffset, width, height, null);

        // Draw hitbox outline in green for debugging
       

        // Draw red marker at the last hit position if damage was taken
        if (lastHitX != null && lastHitY != null) {
            int relativeHitX = lastHitX - x;
            int relativeHitY = lastHitY - y;
            int dotX = Math.max(-width / 2, Math.min(relativeHitX, width / 2 - 6));
            int dotY = Math.max(-height / 2, Math.min(relativeHitY, height / 2 - 6));

            g2d.setColor(Color.RED);
            g2d.fillOval(dotX - 3, dotY - 3, 6, 6);

            lastHitX = null; // Reset hit marker position
            lastHitY = null;
        }

        g2d.setTransform(oldTransform);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x - 24, y - 24, 48, 48); // Adjust hitbox as needed
    }

    public int getHealth() {
        return health;
    }

    public double getShields() {
        return shields;
    }
}
