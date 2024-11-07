public class MovementThread extends Thread {
    private GameObject gameObject;
    private int updateRate;
    private volatile boolean isRunning = true;

    public MovementThread(GameObject gameObject, int updateRate) {
        this.gameObject = gameObject;
        this.updateRate = updateRate;
    }

    public void stopThread() {
        isRunning = false; // หยุดการทำงานของ thread
        this.interrupt(); // ทำให้ thread หยุด sleep ทันที
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                if (gameObject != null) {
                    gameObject.tick(); // อัปเดตการเคลื่อนไหวของวัตถุ
                }
                Thread.sleep(1000 / updateRate); // หน่วงเวลาตามอัตราการอัปเดตที่ตั้งไว้
            } catch (InterruptedException e) {
                System.out.println("Movement thread interrupted.");
                break;
            } catch (NullPointerException e) {
                System.err.println("Movement thread encountered a null object.");
                break; // ออกจาก loop ถ้าเกิด NullPointerException
            }
        }
    }
}
