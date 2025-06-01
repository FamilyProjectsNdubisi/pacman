package com.pacman.ui;

import com.pacman.game.GameState;
import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 40;
    private static final int PANEL_WIDTH = 28 * TILE_SIZE; // Standard Pac-Man maze width
    private static final int PANEL_HEIGHT = 31 * TILE_SIZE; // Standard Pac-Man maze height

    private GameState gameState;
    private int offsetX;
    private int offsetY;

    public GamePanel(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        calculateOffset();
    }

    private void calculateOffset() {
        if (gameState != null && gameState.getMap() != null) {
            offsetX = (PANEL_WIDTH - gameState.getMap().getWidth() * TILE_SIZE) / 2;
            offsetY = (PANEL_HEIGHT - gameState.getMap().getHeight() * TILE_SIZE) / 2;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (gameState != null) {
            // Draw debug info
            g2d.setColor(Color.WHITE);
            g2d.drawString("Panel Size: " + getWidth() + "x" + getHeight(), 10, 20);
            g2d.drawString("Offset: " + offsetX + "," + offsetY, 10, 40);

            // Render game state
            gameState.render(g2d, offsetX, offsetY);
        } else {
            g2d.setColor(Color.RED);
            g2d.drawString("Game State is null!", 10, 20);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }
}