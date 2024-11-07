import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Missile extends GameObject {

    private Handler handler;
    private GameObject player;
    private Camera camera;
    private float speed;
    private int damage;
    private int lifetime; // Lifetime in milliseconds
    private long spawnTime;
    private BufferedImage  missile;
    private double angle;
    private MovementThread movementThread;

    public Missile(int x, int y, ID id, Handler handler, GameObject player, Camera camera, SpriteSheet ss, int damage, float speed, int lifetime) {
        super(x, y, id, ss);
        movementThread = new MovementThread(this, 2);
        movementThread.start();
        this.handler = handler;
        this.player = player;
        this.camera = camera;
        this.damage = damage;
        this.speed = speed;
        this.lifetime = lifetime;
        this.spawnTime = System.currentTimeMillis();
        missile = ss.grabImage(1, 1, 32, 32);
    }

    @Override
    public void tick() {
        // Remove missile if it exceeds its lifetime
        if (System.currentTimeMillis() - spawnTime >= lifetime) {
            handler.removeObject(this);
            return;
        }

        if (player != null) {
            float diffX = player.getX() - x;
            float diffY = player.getY() - y;
            angle = Math.toDegrees(Math.atan2(diffY, diffX));

            velX = (float) (Math.cos(Math.toRadians(angle)) * speed);
            velY = (float) (Math.sin(Math.toRadians(angle)) * speed);

            x += velX;
            y += velY;

            if (getBounds().intersects(player.getBounds())) {
                if (player instanceof Plane) {
                    Plane plane = (Plane) player;
                    plane.takeDamage(damage);
                }
                handler.removeObject(this);
            }
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, 30, 30);
    }

    @Override
    public void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        int missileWidth = missile.getWidth() + 20;
        int missileHeight = missile.getHeight() + 20;

        g2d.translate(x + missileWidth / 2, y + missileHeight / 2);
        g2d.rotate(Math.toRadians(angle + 90));
        g2d.drawImage(missile, -missileWidth / 2, -missileHeight / 2, missileWidth, missileHeight, null);

        g2d.setTransform(oldTransform);
    }

    public int getDamage() {
        return damage;
    }
}
