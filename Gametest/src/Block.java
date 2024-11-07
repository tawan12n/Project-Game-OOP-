import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Block extends GameObject{

	private BufferedImage asteroids;

	public Block(int x, int y, ID id,SpriteSheet ss) {
		super(x, y, id, ss);
		
		asteroids = ss.grabImage(1, 8, 32, 32);

	}


	public void tick() {

		
	}


	public void render(Graphics g) {
		
		g.drawImage(asteroids, x, y,35,35, null);
//		g.setColor(Color.black);
//		g.fillRect(x, y, 32, 32);
		
	}


	public Rectangle getBounds() {

		return new Rectangle(x,y,32,32);
	}
	
}
