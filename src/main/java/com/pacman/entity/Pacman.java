package com.pacman.entity;

import com.pacman.map.GameMap;
import com.pacman.map.Tile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Pacman {
    private static final int SIZE = 40;
    private static final double SPEED = 2.5;
    private static final int ANIMATION_FRAMES = 8; // Even faster animation cycle
    private static final int EATING_ANIMATION_DURATION = 15; // Duration of eating animation

    private double x;
    private double y;
    private Direction currentDirection;
    private Direction nextDirection;
    private int animationTick;
    private int score;
    private boolean isPoweredUp;
    private int powerUpTimer;
    private Map<Direction, BufferedImage> sprites;
    private BufferedImage eatingSprite;
    private boolean isEating;
    private int eatingAnimationTimer;

    public enum Direction {
        UP(0, -1, "pacmanUp.png"),
        DOWN(0, 1, "pacmanDown.png"),
        LEFT(-1, 0, "pacmanLeft.png"),
        RIGHT(1, 0, "pacmanRight.png");

        final int dx, dy;
        final String spriteName;

        Direction(int dx, int dy, String spriteName) {
            this.dx = dx;
            this.dy = dy;
            this.spriteName = spriteName;
        }
    }

    public Pacman(Point spawnPoint) {
        this.x = spawnPoint.x * SIZE;
        this.y = spawnPoint.y * SIZE;
        this.currentDirection = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
        this.score = 0;
        this.isPoweredUp = false;
        this.powerUpTimer = 0;
        this.isEating = false;
        this.eatingAnimationTimer = 0;
        loadSprites();
    }

    private void loadSprites() {
        sprites = new HashMap<>();
        try {
            // Load directional sprites
            for (Direction dir : Direction.values()) {
                BufferedImage sprite = ImageIO.read(new File("src/main/images/" + dir.spriteName));
                // Scale the image to match our tile size
                BufferedImage scaled = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = scaled.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(sprite, 0, 0, SIZE, SIZE, null);
                g2d.dispose();
                sprites.put(dir, scaled);
            }

            // Load eating animation sprite
            eatingSprite = ImageIO.read(new File("src/main/images/pacman.jpg"));
            BufferedImage scaledEating = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledEating.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(eatingSprite, 0, 0, SIZE, SIZE, null);
            g2d.dispose();
            eatingSprite = scaledEating;

        } catch (IOException e) {
            System.err.println("Error loading Pac-Man sprites: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(GameMap gameMap) {
        animationTick = (animationTick + 1) % ANIMATION_FRAMES;

        // Update eating animation timer
        if (isEating) {
            eatingAnimationTimer--;
            if (eatingAnimationTimer <= 0) {
                isEating = false;
            }
        }

        // Try to change direction if there's a pending direction change
        if (nextDirection != currentDirection) {
            // Center position for better turning
            double centerX = x + SIZE / 2;
            double centerY = y + SIZE / 2;

            // Check if we're close to tile center for turning
            boolean nearTileCenter = Math.abs(centerX % SIZE - SIZE / 2) < SPEED * 1.5 &&
                    Math.abs(centerY % SIZE - SIZE / 2) < SPEED * 1.5;

            if (nearTileCenter && canMove(gameMap, nextDirection)) {
                currentDirection = nextDirection;
                // Align to grid when turning
                x = Math.round(x / SIZE) * SIZE;
                y = Math.round(y / SIZE) * SIZE;
            }
        }

        // Move in current direction if possible
        if (canMove(gameMap, currentDirection)) {
            double nextX = x + currentDirection.dx * SPEED;
            double nextY = y + currentDirection.dy * SPEED;

            // Check game boundaries
            if (isInBounds(gameMap, nextX, nextY)) {
                // Update position if movement is valid
                if (isValidPosition(gameMap, nextX, nextY)) {
                    x = nextX;
                    y = nextY;
                }
            } else if (isTunnelPosition(gameMap, nextX, nextY)) {
                // Handle wrapping around the screen (tunnel)
                if (nextX < -SIZE) {
                    x = gameMap.getWidth() * SIZE;
                } else if (nextX > gameMap.getWidth() * SIZE) {
                    x = -SIZE;
                } else {
                    x = nextX;
                    y = nextY;
                }
            }
        }

        // Check for dot collection using center point
        int tileX = (int) ((x + SIZE / 2) / SIZE);
        int tileY = (int) ((y + SIZE / 2) / SIZE);

        // Ensure tile coordinates are within bounds
        if (tileX >= 0 && tileX < gameMap.getWidth() && tileY >= 0 && tileY < gameMap.getHeight()) {
            Tile currentTile = gameMap.getTileAt(tileX, tileY);
            if (currentTile == Tile.DOT || currentTile == Tile.POWER_PELLET) {
                // Trigger eating animation
                isEating = true;
                eatingAnimationTimer = EATING_ANIMATION_DURATION;

                if (currentTile == Tile.DOT) {
                    score += 10;
                } else {
                    score += 50;
                    isPoweredUp = true;
                    powerUpTimer = 600;
                }
                gameMap.setTileAt(tileX, tileY, Tile.EMPTY);
            }
        }

        // Update power-up timer
        if (isPoweredUp) {
            powerUpTimer--;
            if (powerUpTimer <= 0) {
                isPoweredUp = false;
            }
        }
    }

    private boolean isInBounds(GameMap gameMap, double newX, double newY) {
        return newX >= 0 &&
                newX + SIZE <= gameMap.getWidth() * SIZE &&
                newY >= 0 &&
                newY + SIZE <= gameMap.getHeight() * SIZE;
    }

    private boolean isTunnelPosition(GameMap gameMap, double newX, double newY) {
        // Check if position is in tunnel area (usually around y = 10-12 in standard
        // map)
        int tileY = (int) (newY / SIZE);
        return tileY >= 8 && tileY <= 12; // Tunnel y-position range
    }

    private boolean isValidPosition(GameMap gameMap, double newX, double newY) {
        // Check corners of Pac-Man's bounding box
        int[][] checkPoints = {
                { 0, 0 }, // Top-left
                { SIZE - 1, 0 }, // Top-right
                { 0, SIZE - 1 }, // Bottom-left
                { SIZE - 1, SIZE - 1 }, // Bottom-right
                { SIZE / 2, SIZE / 2 } // Center
        };

        for (int[] point : checkPoints) {
            int checkX = (int) ((newX + point[0]) / SIZE);
            int checkY = (int) ((newY + point[1]) / SIZE);

            // Check if the point is within map bounds
            if (checkX >= 0 && checkX < gameMap.getWidth() &&
                    checkY >= 0 && checkY < gameMap.getHeight()) {
                if (gameMap.getTileAt(checkX, checkY) == Tile.WALL) {
                    return false;
                }
            } else if (!isTunnelPosition(gameMap, newX, newY)) {
                // If not in tunnel area and out of bounds, prevent movement
                return false;
            }
        }
        return true;
    }

    private boolean canMove(GameMap gameMap, Direction dir) {
        double nextX = x + dir.dx * SPEED;
        double nextY = y + dir.dy * SPEED;
        return isValidPosition(gameMap, nextX, nextY);
    }

    public void render(Graphics2D g2d, int offsetX, int offsetY) {
        if (isEating && eatingSprite != null) {
            // Use eating animation sprite
            g2d.drawImage(eatingSprite, (int) offsetX + (int) x, (int) offsetY + (int) y, SIZE, SIZE, null);
        } else {
            // Use directional sprite with blinking animation
            BufferedImage sprite = sprites.get(currentDirection);
            if (sprite != null && animationTick < ANIMATION_FRAMES * 0.9) {
                g2d.drawImage(sprite, (int) offsetX + (int) x, (int) offsetY + (int) y, SIZE, SIZE, null);
            }
        }
    }

    public void setDirection(Direction dir) {
        nextDirection = dir;
    }

    public int getScore() {
        return score;
    }

    public boolean isPoweredUp() {
        return isPoweredUp;
    }

    public Direction getDirection() {
        return currentDirection;
    }

    public Point getPosition() {
        return new Point((int) x, (int) y);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, SIZE, SIZE);
    }

    public void resetPosition(Point spawnPoint) {
        this.x = spawnPoint.x * SIZE;
        this.y = spawnPoint.y * SIZE;
        this.currentDirection = Direction.RIGHT;
        this.nextDirection = Direction.RIGHT;
    }
}