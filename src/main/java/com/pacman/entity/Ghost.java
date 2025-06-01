package com.pacman.entity;

import com.pacman.map.GameMap;
import com.pacman.map.Tile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Ghost {
    private static final int SIZE = 40;
    private static final double NORMAL_SPEED = 2.0;
    private static final double SCARED_SPEED = 1.5;
    private static final int SCATTER_DURATION = 600; // 10 seconds at 60 FPS
    private static final int CHASE_DURATION = 1200; // 20 seconds at 60 FPS

    public enum GhostType {
        BLINKY("redGhost.png", new Point(25, 0)), // Targets Pacman directly
        PINKY("pinkGhost.png", new Point(2, 0)), // Targets 4 tiles ahead of Pacman
        INKY("blueGhost.png", new Point(27, 30)), // Complex targeting using Blinky's position
        CLYDE("orangeGhost.png", new Point(0, 30)); // Random movement when far, chase when close

        final String spriteName;
        final Point scatterTarget;

        GhostType(String sprite, Point scatterTarget) {
            this.spriteName = sprite;
            this.scatterTarget = scatterTarget;
        }
    }

    private double x;
    private double y;
    private GhostType type;
    private BufferedImage normalSprite;
    private BufferedImage scaredSprite;
    private Pacman.Direction currentDirection;
    private boolean isScared;
    private int modeTimer;
    private boolean isChasing;
    private Random random;
    private boolean isInGhostHouse;

    public Ghost(Point spawnPoint, GhostType type) {
        this.x = spawnPoint.x * SIZE;
        this.y = spawnPoint.y * SIZE;
        this.type = type;
        this.currentDirection = Pacman.Direction.UP;
        this.isScared = false;
        this.isChasing = true;
        this.modeTimer = CHASE_DURATION;
        this.random = new Random();
        this.isInGhostHouse = true;
        loadSprites();
    }

    private void loadSprites() {
        try {
            normalSprite = ImageIO.read(new File("src/main/images/" + type.spriteName));
            scaredSprite = ImageIO.read(new File("src/main/images/scaredGhost.png"));

            // Scale sprites
            BufferedImage scaledNormal = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            BufferedImage scaledScared = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = scaledNormal.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(normalSprite, 0, 0, SIZE, SIZE, null);
            g2d.dispose();

            g2d = scaledScared.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(scaredSprite, 0, 0, SIZE, SIZE, null);
            g2d.dispose();

            normalSprite = scaledNormal;
            scaredSprite = scaledScared;
        } catch (IOException e) {
            System.err.println("Error loading ghost sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(GameMap gameMap, Pacman pacman, Ghost blinky) {
        // Update mode timer
        modeTimer--;
        if (modeTimer <= 0) {
            isChasing = !isChasing;
            modeTimer = isChasing ? CHASE_DURATION : SCATTER_DURATION;
        }

        // Calculate next move
        Point target;
        if (isInGhostHouse) {
            // Move upward to exit ghost house
            target = new Point((int) (x / SIZE), (int) (y / SIZE) - 1);
            if (y / SIZE <= 11) { // Ghost house exit at y=11
                isInGhostHouse = false;
            }
        } else {
            target = getTargetTile(pacman, blinky);
        }

        // Get all possible directions
        List<Pacman.Direction> possibleDirs = getPossibleDirections(gameMap);

        // Choose best direction
        Pacman.Direction nextDir;
        if (isScared) {
            // Random movement when scared
            nextDir = possibleDirs.isEmpty() ? currentDirection : possibleDirs.get(random.nextInt(possibleDirs.size()));
        } else {
            // Choose direction that gets closest to target
            nextDir = getBestDirection(possibleDirs, target);
        }

        // Move ghost
        double speed = isScared ? SCARED_SPEED : NORMAL_SPEED;
        if (nextDir != null) {
            currentDirection = nextDir;
            double nextX = x + currentDirection.dx * speed;
            double nextY = y + currentDirection.dy * speed;

            // Handle tunnel wrapping
            if (isTunnelPosition(gameMap, nextX, nextY)) {
                if (nextX < -SIZE) {
                    x = gameMap.getWidth() * SIZE;
                } else if (nextX > gameMap.getWidth() * SIZE) {
                    x = -SIZE;
                }
            } else {
                x = nextX;
                y = nextY;
            }
        }
    }

    private List<Pacman.Direction> getPossibleDirections(GameMap gameMap) {
        List<Pacman.Direction> possibleDirs = new ArrayList<>();

        for (Pacman.Direction dir : Pacman.Direction.values()) {
            // Don't allow reversing unless it's the only option
            if (dir == getOppositeDirection(currentDirection) && !isScared) {
                continue;
            }

            if (canMove(gameMap, dir)) {
                possibleDirs.add(dir);
            }
        }

        // If no directions are possible (including reverse), allow reverse
        if (possibleDirs.isEmpty()) {
            Pacman.Direction reverseDir = getOppositeDirection(currentDirection);
            if (canMove(gameMap, reverseDir)) {
                possibleDirs.add(reverseDir);
            }
        }

        return possibleDirs;
    }

    private Pacman.Direction getBestDirection(List<Pacman.Direction> possibleDirs, Point target) {
        double minDistance = Double.MAX_VALUE;
        Pacman.Direction bestDir = null;
        Point currentPos = new Point((int) (x / SIZE), (int) (y / SIZE));

        for (Pacman.Direction dir : possibleDirs) {
            int newX = currentPos.x + dir.dx;
            int newY = currentPos.y + dir.dy;
            double distance = Point.distance(newX, newY, target.x, target.y);

            if (distance < minDistance) {
                minDistance = distance;
                bestDir = dir;
            }
        }

        return bestDir != null ? bestDir : currentDirection;
    }

    private Point getTargetTile(Pacman pacman, Ghost blinky) {
        if (isScared) {
            return new Point(
                    random.nextInt(28),
                    random.nextInt(31));
        }

        Point pacmanPos = new Point(
                (int) (pacman.getPosition().x / SIZE),
                (int) (pacman.getPosition().y / SIZE));

        if (!isChasing) {
            return type.scatterTarget;
        }

        switch (type) {
            case BLINKY:
                return pacmanPos;
            case PINKY:
                // Target 4 tiles ahead of Pacman
                return new Point(
                        pacmanPos.x + pacman.getDirection().dx * 4,
                        pacmanPos.y + pacman.getDirection().dy * 4);
            case INKY:
                // Complex targeting using Blinky's position
                Point blinkyPos = new Point((int) (blinky.x / SIZE), (int) (blinky.y / SIZE));
                int targetX = 2 * pacmanPos.x - blinkyPos.x;
                int targetY = 2 * pacmanPos.y - blinkyPos.y;
                return new Point(targetX, targetY);
            case CLYDE:
                // Random movement when far, chase when close
                double distance = Point.distance(x / SIZE, y / SIZE, pacmanPos.x, pacmanPos.y);
                return distance > 8 ? type.scatterTarget : pacmanPos;
            default:
                return pacmanPos;
        }
    }

    private boolean canMove(GameMap gameMap, Pacman.Direction dir) {
        // Calculate next position including the full ghost size
        double nextX = x + dir.dx * NORMAL_SPEED;
        double nextY = y + dir.dy * NORMAL_SPEED;

        // Convert to tile coordinates for all corners
        int leftTile = (int) (nextX / SIZE);
        int rightTile = (int) ((nextX + SIZE - 1) / SIZE);
        int topTile = (int) (nextY / SIZE);
        int bottomTile = (int) ((nextY + SIZE - 1) / SIZE);

        // Check if any corner would be out of bounds
        if (leftTile < 0 || rightTile >= gameMap.getWidth() ||
                topTile < 0 || bottomTile >= gameMap.getHeight()) {
            // Allow movement only in tunnel area
            return isTunnelPosition(gameMap, nextX, nextY);
        }

        // Allow movement through ghost house door when exiting
        if (isInGhostHouse && topTile == 11) { // Ghost house exit at y=11
            return true;
        }

        // Check all tiles that the ghost would occupy
        for (int x = leftTile; x <= rightTile; x++) {
            for (int y = topTile; y <= bottomTile; y++) {
                if (gameMap.getTileAt(x, y) == Tile.WALL) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean isTunnelPosition(GameMap gameMap, double newX, double newY) {
        int tileY = (int) (newY / SIZE);
        // Allow wrapping only in tunnel area (y = 11-12 in standard map)
        if (tileY >= 11 && tileY <= 12) {
            // Allow moving past left/right bounds
            if (newX < 0 || newX > gameMap.getWidth() * SIZE) {
                return true;
            }
        }
        return false;
    }

    private Pacman.Direction getOppositeDirection(Pacman.Direction dir) {
        switch (dir) {
            case UP:
                return Pacman.Direction.DOWN;
            case DOWN:
                return Pacman.Direction.UP;
            case LEFT:
                return Pacman.Direction.RIGHT;
            case RIGHT:
                return Pacman.Direction.LEFT;
            default:
                return dir;
        }
    }

    public void setScared(boolean scared) {
        isScared = scared;
    }

    public boolean isScared() {
        return isScared;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, SIZE, SIZE);
    }

    public void render(Graphics2D g2d, int offsetX, int offsetY) {
        BufferedImage sprite = isScared ? scaredSprite : normalSprite;
        if (sprite != null) {
            g2d.drawImage(sprite, (int) offsetX + (int) x, (int) offsetY + (int) y, SIZE, SIZE, null);
        }
    }

    public void resetPosition(Point spawnPoint) {
        this.x = spawnPoint.x * SIZE;
        this.y = spawnPoint.y * SIZE;
        this.currentDirection = Pacman.Direction.UP;
        this.isChasing = true;
        this.modeTimer = CHASE_DURATION;
        this.isInGhostHouse = true;
    }
}