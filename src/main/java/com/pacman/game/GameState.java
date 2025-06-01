package com.pacman.game;

import com.pacman.entity.*;
import com.pacman.map.GameMap;
import com.pacman.score.HighScore;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GameState {
    private static final int INITIAL_LIVES = 3;
    private static final int GHOST_RELEASE_DELAY = 180; // 3 seconds at 60 FPS
    private static final int POWER_PELLET_DURATION = 600; // 10 seconds at 60 FPS

    private GameMap gameMap;
    private Pacman pacman;
    private List<Ghost> ghosts;
    private String playerName;
    private int score;
    private int lives;
    private boolean gameOver;
    private boolean paused;
    private int ghostReleaseTimer;
    private int ghostsReleased;

    public GameState(String playerName) {
        this.playerName = playerName;
        this.gameMap = new GameMap();
        this.lives = INITIAL_LIVES;
        this.ghosts = new ArrayList<>();
        initializeGame();
    }

    private void initializeGame() {
        // Initialize Pac-Man
        pacman = new Pacman(gameMap.getPacmanSpawn());

        // Initialize Ghosts with different spawn points
        List<Point> ghostSpawns = gameMap.getGhostSpawns();
        Point centerSpawn = ghostSpawns.get(0); // Center of ghost house

        // Create ghosts with slightly different positions to prevent overlap
        ghosts.add(new Ghost(new Point(centerSpawn.x - 1, centerSpawn.y), Ghost.GhostType.BLINKY));
        ghosts.add(new Ghost(new Point(centerSpawn.x + 1, centerSpawn.y), Ghost.GhostType.PINKY));
        ghosts.add(new Ghost(new Point(centerSpawn.x, centerSpawn.y - 1), Ghost.GhostType.INKY));
        ghosts.add(new Ghost(new Point(centerSpawn.x, centerSpawn.y + 1), Ghost.GhostType.CLYDE));

        ghostReleaseTimer = GHOST_RELEASE_DELAY;
        ghostsReleased = 0;
        gameOver = false;
        paused = false;
    }

    public void update() {
        if (gameOver || paused)
            return;

        // Update Pac-Man
        pacman.update(gameMap);

        // Release ghosts gradually
        if (ghostsReleased < ghosts.size()) {
            ghostReleaseTimer--;
            if (ghostReleaseTimer <= 0) {
                ghostsReleased++;
                ghostReleaseTimer = GHOST_RELEASE_DELAY;
            }
        }

        // Update active ghosts
        Ghost blinky = ghosts.get(0); // Blinky is always first
        for (int i = 0; i < ghostsReleased; i++) {
            Ghost ghost = ghosts.get(i);

            // Update ghost scared state based on Pac-Man's power state
            ghost.setScared(pacman.isPoweredUp());

            // Update ghost
            ghost.update(gameMap, pacman, blinky);

            // Check for collision with Pac-Man
            if (ghost.getBounds().intersects(pacman.getBounds())) {
                handleGhostCollision(ghost);
            }
        }

        // Check if all dots are collected
        if (isLevelComplete()) {
            handleLevelComplete();
        }
    }

    private void handleGhostCollision(Ghost ghost) {
        if (ghost.isScared()) {
            // Eat ghost
            score += 200;
            ghost.resetPosition(gameMap.getGhostSpawns().get(0));
        } else {
            // Lose life
            lives--;
            if (lives <= 0) {
                handleGameOver();
            } else {
                resetLevel(false);
            }
        }
    }

    private boolean isLevelComplete() {
        // Check if any dots remain
        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {
                if (gameMap.getTileAt(x, y).isDot()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleLevelComplete() {
        // Save score if it's a high score
        if (HighScore.isHighScore(score)) {
            HighScore.saveHighScore(new HighScore(playerName, score));
        }
        resetLevel(true);
    }

    private void handleGameOver() {
        gameOver = true;
        // Save final score if it's a high score
        if (HighScore.isHighScore(score)) {
            HighScore.saveHighScore(new HighScore(playerName, score));
        }
    }

    private void resetLevel(boolean newLevel) {
        // Reset Pac-Man
        pacman.resetPosition(gameMap.getPacmanSpawn());

        // Reset ghosts with different positions
        List<Point> ghostSpawns = gameMap.getGhostSpawns();
        Point centerSpawn = ghostSpawns.get(0);

        // Position ghosts in different spots in the ghost house
        ghosts.get(0).resetPosition(new Point(centerSpawn.x - 1, centerSpawn.y)); // Blinky
        ghosts.get(1).resetPosition(new Point(centerSpawn.x + 1, centerSpawn.y)); // Pinky
        ghosts.get(2).resetPosition(new Point(centerSpawn.x, centerSpawn.y - 1)); // Inky
        ghosts.get(3).resetPosition(new Point(centerSpawn.x, centerSpawn.y + 1)); // Clyde

        for (Ghost ghost : ghosts) {
            ghost.setScared(false);
        }

        ghostReleaseTimer = GHOST_RELEASE_DELAY;
        ghostsReleased = 0;

        if (newLevel) {
            gameMap = new GameMap(); // Load a new map
        }
    }

    public void togglePause() {
        paused = !paused;
    }

    public void render(Graphics2D g2d, int offsetX, int offsetY) {
        // Render map
        gameMap.render(g2d, offsetX, offsetY);

        // Render Pac-Man
        pacman.render(g2d, offsetX, offsetY);

        // Render active ghosts
        for (int i = 0; i < ghostsReleased; i++) {
            ghosts.get(i).render(g2d, offsetX, offsetY);
        }

        // Render UI
        renderUI(g2d);

        // Render game over screen
        if (gameOver) {
            renderGameOver(g2d);
        }
    }

    private void renderGameOver(Graphics2D g2d) {
        // Semi-transparent black overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, gameMap.getWidth() * gameMap.getTileSize(),
                gameMap.getHeight() * gameMap.getTileSize());

        // Game Over text
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int messageWidth = fm.stringWidth(gameOverText);
        g2d.drawString(gameOverText,
                (gameMap.getWidth() * gameMap.getTileSize() - messageWidth) / 2,
                gameMap.getHeight() * gameMap.getTileSize() / 2);

        // Score text
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        String scoreText = "Final Score: " + score;
        messageWidth = fm.stringWidth(scoreText);
        g2d.drawString(scoreText,
                (gameMap.getWidth() * gameMap.getTileSize() - messageWidth) / 2,
                gameMap.getHeight() * gameMap.getTileSize() / 2 + 50);

        // Instructions
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String instructions = "Press ENTER to play again or ESC to quit";
        messageWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions,
                (gameMap.getWidth() * gameMap.getTileSize() - messageWidth) / 2,
                gameMap.getHeight() * gameMap.getTileSize() / 2 + 100);
    }

    private void renderUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));

        // Score
        g2d.drawString("Score: " + score, 20, 25);

        // Lives
        g2d.drawString("Lives: " + lives, 20, 50);

        // Power pellet timer
        if (pacman.isPoweredUp()) {
            g2d.setColor(Color.BLUE);
            g2d.drawString("POWER!", 20, 75);
        }

        // Paused
        if (paused) {
            String message = "PAUSED";
            FontMetrics fm = g2d.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            g2d.drawString(message,
                    (gameMap.getWidth() * gameMap.getTileSize() - messageWidth) / 2,
                    gameMap.getHeight() * gameMap.getTileSize() / 2);
        }
    }

    // Getters
    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public String getPlayerName() {
        return playerName;
    }

    public GameMap getMap() {
        return gameMap;
    }

    public Pacman getPacman() {
        return pacman;
    }
}