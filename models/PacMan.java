package models;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashSet;
import java.util.Random;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U';
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updatePosition(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image redGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image vulnerableGhostImage;
    private Image cherryImage; 

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> ghosts;
    HashSet<Block> foods;
    HashSet<Block> cherries; 
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    boolean powerMode = false; 
    int powerModeTimer = 0;
    boolean ghostFlash = false;
    int flashCounter = 0;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("/assets/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/assets/blueGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/assets/redGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/assets/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/assets/pinkGhost.png")).getImage();
        vulnerableGhostImage = new ImageIcon(getClass().getResource("/assets/scaredGhost.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("/assets/cherry.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/assets/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/assets/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/assets/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/assets/pacmanRight.png")).getImage();

        // Load the map and spawn cherries
        loadMap();
        spawnCherries(); 
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updatePosition(newDirection);
        }
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    private void spawnCherries() {
        cherries = new HashSet<Block>();
        // Place 4 cherries randomly on the map
        int cherriesPlaced = 0;
        while (cherriesPlaced < 4) {
            int r = random.nextInt(rowCount);
            int c = random.nextInt(columnCount);
            
            // Check if the position is empty and not already occupied by a wall or a ghost
            if (tileMap[r].charAt(c) == ' ') {
                int x = c * tileSize + (tileSize - cherryImage.getWidth(null)) / 2;
                int y = r * tileSize + (tileSize - cherryImage.getHeight(null)) / 2;
                Block cherry = new Block(cherryImage, x, y, cherryImage.getWidth(null), cherryImage.getHeight(null));
                cherries.add(cherry);
                cherriesPlaced++;
            }
        }
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        foods = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;
                // Black wall
                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                // Blue ghost
                else if (tileMapChar == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                // Red ghost
                else if (tileMapChar == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                // Orange ghost
                else if (tileMapChar == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                // Pink ghost
                else if (tileMapChar == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                // Pacman
                else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                // Food
                else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw Pac-Man
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        
        // Draw ghosts
        for (Block ghost : ghosts) {
            if (powerMode) {
                if (powerModeTimer > 100 || !ghostFlash) {
                    g.drawImage(vulnerableGhostImage, ghost.x, ghost.y, ghost.width, ghost.height, null);
                } else {
                    g.drawImage(ghostFlash ? ghost.image : vulnerableGhostImage, 
                               ghost.x, ghost.y, ghost.width, ghost.height, null);
                }
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }
        
        // Draw walls
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        
        // Draw food
        g.setColor(Color.YELLOW);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        
        // Draw cherries
        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }
        
        // Draw score and lives
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over ! " + String.valueOf(score), tileSize / 2, tileSize / 2);
        } else {
            g.drawString("x" + String.valueOf(lives) + " Score : " + String.valueOf(score), tileSize / 2, tileSize / 2);
        }
        
        // Display power mode timer
        if (powerMode) {
            g.drawString("Power: " + (powerModeTimer / 20), boardWidth - 100, tileSize / 2);
        }
    }

    public void move() {
        // Update power mode timer
        if (powerMode) {
            powerModeTimer--;
            
            // Flash the ghosts every 5 frames (0.5s)
            if (powerModeTimer <= 50) {
                flashCounter++;
                // Alterner toutes les 10 frames (0.5s)
                if (flashCounter % 10 == 0) {
                    ghostFlash = !ghostFlash;
                }
            }
            
            if (powerModeTimer <= 0) {
                powerMode = false;
                ghostFlash = false;
                flashCounter = 0;
            }
        }
        
        // Pac Man movements
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Collide with the walls
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }
        
        // Collide with the ghosts
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                if (powerMode) {
                    // Eat the ghost
                    ghost.reset();
                    score += 200;
                } else {
                    // Lost a life
                    lives -= 1;
                    if (lives == 0) {
                        gameOver = true;
                        return;
                    }
                    resetposition();
                }
            }
            
            // Random ghosts movement
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updatePosition('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updatePosition(newDirection);
                    break;
                }
            }
        }

        // Eat food
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Eat cherries
        Block cherryEaten = null;
        for (Block cherry : cherries) {
            if (collision(pacman, cherry)) {
                cherryEaten = cherry;
                score += 100;
                powerMode = true;
                powerModeTimer = 600;
                ghostFlash = false;
                flashCounter = 0;
            }
        }
        cherries.remove(cherryEaten);

        if (foods.isEmpty()) {
            loadMap();
            spawnCherries();
            resetposition();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    public void resetposition() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updatePosition(newDirection);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            spawnCherries();
            resetposition();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.image = pacmanUpImage;
            pacman.updatePosition('U');
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.image = pacmanDownImage;
            pacman.updatePosition('D');
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.image = pacmanLeftImage;
            pacman.updatePosition('L');
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.image = pacmanRightImage;
            pacman.updatePosition('R');
        }
    }
}