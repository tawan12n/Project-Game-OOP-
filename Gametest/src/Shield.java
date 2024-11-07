import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Shield extends GameObject {

    private Handler handler;
    private BufferedImage shield;

    public Shield(int x, int y, ID id, Handler handler,SpriteSheet ss) {
        super(x, y, id, ss);
        this.handler = handler;
        shield = ss.grabImage(1, 1, 32, 32);
    }

    @Override
    public void tick() {
        // Shield does not move
    }

    @Override
    public void render(Graphics g) {
    	g.drawImage(shield, x, y, 64,64, null);
//        g.setColor(Color.CYAN);
//        g.fillRect(x, y, 32, 32);
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 32, 32);
    }
}
