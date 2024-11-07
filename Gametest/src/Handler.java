import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

public class Handler {

    LinkedList<GameObject> object = new LinkedList<>();
    private List<GameObject> objectsToAdd = new LinkedList<>();
    private List<GameObject> objectsToRemove = new LinkedList<>();

    private boolean up = false, down = false, right = false, left = false;

    public synchronized void tick() {
        // เพิ่มหรือลบวัตถุที่เก็บไว้ในลิสต์ชั่วคราว
        object.addAll(objectsToAdd);
        object.removeAll(objectsToRemove);

        // เคลียร์ลิสต์ชั่วคราวหลังจากอัปเดต
        objectsToAdd.clear();
        objectsToRemove.clear();

        // วนลูปตามวัตถุใน LinkedList ปัจจุบัน
        for (GameObject tempObject : object) {
            tempObject.tick();
        }
    }

    public synchronized void render(Graphics g) {
        for (GameObject tempObject : object) {
            tempObject.render(g);
        }
    }

    public synchronized void addObject(GameObject tempObject) {
        objectsToAdd.add(tempObject);  // เก็บวัตถุใหม่ในลิสต์ชั่วคราว
    }

    public synchronized void removeObject(GameObject tempObject) {
        objectsToRemove.add(tempObject); // เก็บวัตถุที่จะลบในลิสต์ชั่วคราว
    }

    public synchronized GameObject getPlayer() {
        for (GameObject obj : object) {
            if (obj.getId() == ID.Player) {
                return obj;
            }
        }
        return null; // ไม่พบ Player
    }

    public boolean isUp() { return up; }
    public void setUp(boolean up) { this.up = up; }
    public boolean isDown() { return down; }
    public void setDown(boolean down) { this.down = down; }
    public boolean isRight() { return right; }
    public void setRight(boolean right) { this.right = right; }
    public boolean isLeft() { return left; }
    public void setLeft(boolean left) { this.left = left; }
}
