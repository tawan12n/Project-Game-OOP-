

public class Camera {
	
	private float x,y;
	
	public Camera(float x,float y) {
		this.x = x;
		this.y = y;
	}
	
	public void tick(GameObject object) {
	    // Adjust to center the player within the 1980x1080 window
	    x += ((object.getX() - x) - 1920 / 2) * 0.2f;
	    y += ((object.getY() - y) - 1080 / 2) * 0.2f;

	    // Update camera bounds to match new game dimensions
	    if (x <= 0) x = 0;
	    if (x >= 2200 ) x = 2200 ; // Adjust based on level dimensions
	    if (y <= 0) y = 0;
	    if (y >= 3050 ) y = 3050; // Adjust based on level dimensions
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
}
