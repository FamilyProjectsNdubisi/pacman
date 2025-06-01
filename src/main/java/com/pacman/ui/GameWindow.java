package com.pacman.ui;

import com.pacman.game.GameState;
import com.pacman.entity.Pacman;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameWindow extends JFrame {
    private static final int FPS = 60;
    private static final long FRAME_TIME = 1000L / FPS;

    private StartScreen startScreen;
    private GamePanel gamePanel;
    private GameState gameState;
    private Timer gameTimer;
    private boolean isRunning;

    public GameWindow() {
        setTitle("Pac-Man");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Start with the start screen
        startScreen = new StartScreen();
        getContentPane().add(startScreen);
        pack();
        setLocationRelativeTo(null);

        // Initialize game timer
        gameTimer = new Timer((int) FRAME_TIME, e -> gameLoop());
        isRunning = false;

        // Add key listener for the game
        KeyListener keyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        };
        addKeyListener(keyListener);
        setFocusable(true);
        requestFocus();
    }

    private void startGame() {
        if (startScreen == null)
            return;

        String playerName = startScreen.getPlayerName();
        System.out.println("Starting game for player: " + playerName); // Debug output

        // Remove start screen
        getContentPane().remove(startScreen);
        startScreen = null;

        // Initialize game state with player name
        gameState = new GameState(playerName);

        // Create and add game panel
        gamePanel = new GamePanel(gameState);
        getContentPane().add(gamePanel);

        // Add key listener to game panel
        gamePanel.addKeyListener(getKeyListeners()[0]);
        gamePanel.setFocusable(true);

        // Adjust window size and validate
        pack();
        setLocationRelativeTo(null);

        // Ensure the panel is visible and has focus
        gamePanel.setVisible(true);
        gamePanel.requestFocusInWindow();

        // Validate and repaint
        validate();
        repaint();

        // Start game loop
        isRunning = true;
        gameTimer.start();
    }

    private void gameLoop() {
        if (!isRunning || gameState == null) {
            return;
        }

        // Update game state
        gameState.update();

        // Render
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }

    private void handleKeyPress(KeyEvent e) {
        if (startScreen != null && startScreen.isGameStarted()) {
            startGame();
            return;
        }

        if (gameState == null)
            return;

        if (gameState.isGameOver()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    restartGame();
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
            return;
        }

        if (!isRunning)
            return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                gameState.getPacman().setDirection(Pacman.Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                gameState.getPacman().setDirection(Pacman.Direction.RIGHT);
                break;
            case KeyEvent.VK_UP:
                gameState.getPacman().setDirection(Pacman.Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                gameState.getPacman().setDirection(Pacman.Direction.DOWN);
                break;
            case KeyEvent.VK_P:
                gameState.togglePause();
                break;
        }
    }

    private void restartGame() {
        // Stop current game
        isRunning = false;
        gameTimer.stop();

        // Remove game panel
        getContentPane().remove(gamePanel);
        gamePanel = null;
        gameState = null;

        // Show start screen again
        startScreen = new StartScreen();
        getContentPane().add(startScreen);
        pack();
        validate();
        repaint();

        // Ensure focus is set correctly
        startScreen.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GameWindow window = new GameWindow();
                window.setVisible(true);
                window.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}