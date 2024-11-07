import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Crate extends GameObject{
	
	private BufferedImage aemo;
	
	public Crate(int x, int y, ID id,SpriteSheet ss) {
		super(x, y, id, ss);
		
		aemo = ss.grabImage(1, 1, 32, 32);
	}

	
	public void tick() {
		
		
	}

	
	public void render(Graphics g) {
		g.drawImage(aemo, x, y, 32,32, null);
//		g.setColor(Color.cyan);
//		g.fillRect(x, y, 32,32);
		
	}

	
	public Rectangle getBounds() {
	
		return new Rectangle(x, y, 32,32);
	}

}
