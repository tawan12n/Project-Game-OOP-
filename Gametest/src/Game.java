import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
    private boolean isRunning = false;
    private Thread thread;
    private Handler handler;
    private Camera camera;
    private SpriteSheet ssrock, ssaemo, ssshield, ssfloor, ssmissile, ssplane;
    
    private BufferedImage menuBackground;
    private String levelName = "";

    private BufferedImage level, CelestialObjects, aemo, shield, floor, missile, plane;
    public int ammo = 100;

    private long lastSpawnTime = 0;
    private int missileDamage;
    private float missileSpeed;
    private static int spawnInterval;
    private long countdownTime;
    private long startTime;
    private long timeRemaining;
    private int missileLifetime;

    private Random random = new Random();

    private enum GameState {
        MENU,
        DIFFICULTY_SELECTION,
        RUNNING,
        WIN,
        LOSE
    }

    private GameState gameState = GameState.MENU;

    public Game() {
        new Window(1920, 1080, "Plane Game", this);
        start();

        handler = new Handler();
        camera = new Camera(0, 0);
        this.addKeyListener(new KeyInput(handler));
        this.addMouseListener(new MouseInput(handler, camera, this, ssrock));
        this.addMouseListener(new MenuMouseListener());

        BufferedImageLoader loader = new BufferedImageLoader();
        level = loader.loadImage("/plane_level2.png");
        CelestialObjects = loader.loadImage("CelestialObjects.png");
        aemo = loader.loadImage("/Pickup Icon - Weapons - Rocket.png");
        shield = loader.loadImage("/Pickup Icon - Shield Generator - All around shield.png");
        floor = loader.loadImage("/Space Sprite Sheet.png");
        missile = loader.loadImage("/Main ship weapon - Projectile - Rocket.png");
        plane = loader.loadImage("/Main Ship - Base - Full health.png");
        menuBackground = loader.loadImage("/menu_background.png");

        ssrock = new SpriteSheet(CelestialObjects);
        ssaemo = new SpriteSheet(aemo);
        ssshield = new SpriteSheet(shield);
        ssfloor = new SpriteSheet(floor);
        ssmissile = new SpriteSheet(missile);
        ssplane = new SpriteSheet(plane);

        floor = ssfloor.grabImage(3,1,32,32);

        loadLevel(level);
    }

    private void start() {
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    private void stop() {
        isRunning = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        this.requestFocus();
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        long timer = System.currentTimeMillis();
        int frames = 0;

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                tick();
                delta--;
            }

            render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames);
                frames = 0;
            }
        }
        stop();
    }

    public void tick() {
        if (gameState != GameState.RUNNING) return;

        long currentTime = System.currentTimeMillis();
        timeRemaining = countdownTime - (currentTime - startTime);

        GameObject player = handler.getPlayer();
        if (player instanceof Plane) {
            Plane plane = (Plane) player;
            if (plane.getHealth() <= 0) {
                gameState = GameState.LOSE;
                System.out.println("Player health reached zero. We Lose.");
                return;
            }
        }

        if (timeRemaining <= 0) {
            gameState = GameState.WIN;
            System.out.println("Time is up. We Win!");
            return;
        }

        if (currentTime - lastSpawnTime >= spawnInterval) {
            spawnMissile();
            lastSpawnTime = currentTime;
        }

        synchronized (handler) {
            if (player != null) {
                camera.tick(player);
            }
        }
        handler.tick();
    }

    private void spawnMissile() {
        if (gameState != GameState.RUNNING) return;

        GameObject player = handler.getPlayer();
        if (player == null) return;

        int playerX = player.getX();
        int playerY = player.getY();

        int x, y;
        int minDistance = 250;

        do {
            x = playerX + random.nextInt(400) - 200;
            y = playerY + random.nextInt(400) - 200;
        } while (distance(playerX, playerY, x, y) < minDistance);

        x = Math.max(0, Math.min(x, 1920));
        y = Math.max(0, Math.min(y, 1080));

        // Pass missileLifetime to the Missile constructor
        handler.addObject(new Missile(x, y, ID.Missile, handler, player, camera, ssmissile, missileDamage, missileSpeed, missileLifetime));
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        
        if (gameState == GameState.MENU) {
            renderMenu(g);
        } else if (gameState == GameState.DIFFICULTY_SELECTION) {
            renderDifficultySelection(g);
        } else if (gameState == GameState.RUNNING) {
            renderGame(g);
        } else if (gameState == GameState.WIN) {
            renderWinScreen(g);
        } else if (gameState == GameState.LOSE) {
            renderLoseScreen(g);
        }

        g.dispose();
        bs.show();
    }

    private void renderMenu(Graphics g) {
        if (menuBackground != null) {
            g.drawImage(menuBackground, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.setColor(Color.WHITE);
        g.drawString("Plane Game", getWidth() / 2 - 150, 200);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawRect(getWidth() / 2 - 100, 300, 200, 50);
        g.drawString("Start", getWidth() / 2 - 35, 335);

        g.drawRect(getWidth() / 2 - 100, 400, 200, 50);
        g.drawString("Exit", getWidth() / 2 - 30, 435);
    }

    private void renderDifficultySelection(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawString("Select Difficulty", getWidth() / 2 - 150, 200);

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawRect(getWidth() / 2 - 100, 300, 200, 50);
        g.drawString("Easy", getWidth() / 2 - 35, 335);

        g.drawRect(getWidth() / 2 - 100, 400, 200, 50);
        g.drawString("Medium", getWidth() / 2 - 50, 435);

        g.drawRect(getWidth() / 2 - 100, 500, 200, 50);
        g.drawString("Hard", getWidth() / 2 - 35, 535);
    }

    private void renderGame(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(-camera.getX(), -camera.getY());

        int tileWidth = 32;
        int tileHeight = 32;
        int startX = (int) (camera.getX() / tileWidth) * tileWidth;
        int startY = (int) (camera.getY() / tileHeight) * tileHeight;
        int endX = (int) (camera.getX() + getWidth());
        int endY = (int) (camera.getY() + getHeight());

        if (floor != null) {
            for (int xx = startX; xx < endX; xx += tileWidth) {
                for (int yy = startY; yy < endY; yy += tileHeight) {
                    g.drawImage(floor, xx, yy, tileWidth, tileHeight, null);
                }
            }
        }

        handler.render(g);
        g2d.translate(camera.getX(), camera.getY());

        GameObject player = handler.getPlayer();
        if (player instanceof Plane) {
            Plane plane = (Plane) player;
            g.setColor(Color.WHITE);
            g.drawString("Ammo: " + ammo, 10, 20);

            g.setColor(Color.RED);
            g.fillRect(10, 40, 200, 20);
            int healthBarWidth = (int) ((plane.getHealth() / 100.0) * 200);
            g.setColor(Color.GREEN);
            g.fillRect(10, 40, healthBarWidth, 20);
            g.setColor(Color.BLACK);
            g.drawString("Health", 80, 55);

            g.setColor(Color.CYAN);
            int maxShieldBarWidth = 200;
            int shieldBarWidth = (int) (Math.min(plane.getShields(), 1) * maxShieldBarWidth);
            g.fillRect(10, 70, shieldBarWidth, 20);
            g.setColor(Color.WHITE);
            g.drawRect(10, 70, maxShieldBarWidth, 20);

            g.setColor(Color.BLACK);
            int displayedShields = plane.getShields() > 1 ? (int) (plane.getShields() - 1) : 0;
            g.drawString("Shields: " + displayedShields, 80, 85);

            g.setColor(Color.WHITE);
            g.drawRect(10, 40, 200, 20);
            g.drawRect(10, 70, maxShieldBarWidth, 20);
        }

        // Display the level name
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        g.drawString("Level: " + levelName, 10, 130);

        int centerX = getWidth() / 2;
        String timeString = String.format("Help will come within: %02d:%02d", timeRemaining / 60000, (timeRemaining / 1000) % 60);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.WHITE);
        int timeWidth = g.getFontMetrics().stringWidth(timeString);
        g.drawString(timeString, centerX - timeWidth / 2, 30);
    }

    private void renderWinScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150)); 
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String winMessage = "We Win!";
        int messageWidth = g.getFontMetrics().stringWidth(winMessage);
        g.drawString(winMessage, (getWidth() - messageWidth) / 2, getHeight() / 2);
    }

    private void renderLoseScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150)); 
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String loseMessage = "We Lose!";
        int messageWidth = g.getFontMetrics().stringWidth(loseMessage);
        g.drawString(loseMessage, (getWidth() - messageWidth) / 2, getHeight() / 2);
    }

    private void loadLevel(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        Random random = new Random();

        for (int xx = 0; xx < w; xx++) {
            for (int yy = 0; yy < h; yy++) {
                int pixel = image.getRGB(xx, yy);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                if (red == 255) {
                    handler.addObject(new Block(xx * 32, yy * 32, ID.Block, ssrock));
                }
                if (blue == 255 && green == 0) {
                    handler.addObject(new Plane(xx * 32, yy * 32, ID.Player, handler, this, ssplane));
                }
                if (green == 255 && blue == 0) {
                    handler.addObject(new Meteor(xx * 32, yy * 32, ID.Meteor, handler, ssrock));
                }
                if (green == 255 && blue == 255) {
                    int chance = random.nextInt(100);
                    if (chance < 50) {
                        handler.addObject(new Crate(xx * 32, yy * 32, ID.Crate, ssaemo));
                    } else {
                        handler.addObject(new Shield(xx * 32, yy * 32, ID.Shield, handler, ssshield));
                    }
                }
                if (red == 255 && blue == 225 && green == 0) {
                    GameObject player = handler.getPlayer();
                    if (player != null) {
                        handler.addObject(new Missile(xx * 32, yy * 32, ID.Missile, handler, player, camera, ssmissile, missileDamage, missileSpeed, missileLifetime));
                    }
                }
            }
        }
    }

    private class MenuMouseListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            int mx = e.getX();
            int my = e.getY();

            if (gameState == GameState.MENU) {
                if (mx >= getWidth() / 2 - 100 && mx <= getWidth() / 2 + 100) {
                    if (my >= 300 && my <= 350) {
                        gameState = GameState.DIFFICULTY_SELECTION;
                    } else if (my >= 400 && my <= 450) {
                        System.exit(0);
                    }
                }
            } else if (gameState == GameState.DIFFICULTY_SELECTION) {
                if (mx >= getWidth() / 2 - 100 && mx <= getWidth() / 2 + 100) {
                    if (my >= 300 && my <= 350) {
                        startGame("Easy");
                    } else if (my >= 400 && my <= 450) {
                        startGame("Medium");
                    } else if (my >= 500 && my <= 550) {
                        startGame("Hard");
                    }
                }
            }
        }
      }

        private void startGame(String difficulty) {
            gameState = GameState.RUNNING;
            startTime = System.currentTimeMillis();

            switch (difficulty) {
                case "Easy":
                    ammo = 150;
                    levelName = "Easy Level";
                    missileDamage = 20;
                    missileSpeed = 6.9f;
                    spawnInterval = 6000;
                    countdownTime = 90000;
                    missileLifetime = 4000;
                    break;
                case "Medium":
                    ammo = 100;
                    levelName = "Medium Level";
                    missileDamage = 30;
                    missileSpeed = 7.0f;
                    spawnInterval = 5000;
                    countdownTime = 120000;
                    missileLifetime = 5000;
                    break;
                case "Hard":
                    ammo = 50;
                    levelName = "Hard Level";
                    missileDamage = 50;
                    missileSpeed = 7.3f;
                    spawnInterval = 4000;
                    countdownTime = 150000;
                    missileLifetime = 6000;
                    break;
            }
            System.out.println("Starting game with " + difficulty + " difficulty.");
        }

    public static void main(String[] args) {
        new Game();
    }

}
