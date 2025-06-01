package com.pacman.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.pacman.score.HighScore;

public class StartScreen extends JPanel {
    private static final int WIDTH = 28 * 40; // Same as game width
    private static final int HEIGHT = 31 * 40; // Same as game height
    private BufferedImage titleImage;
    private JTextField nameField;
    private boolean gameStarted;
    private String playerName;
    private java.util.List<HighScore> highScores;

    public StartScreen() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setLayout(null); // Using absolute positioning

        loadTitleImage();
        createComponents();
        loadHighScores();

        gameStarted = false;
        playerName = "";
    }

    private void loadTitleImage() {
        try {
            titleImage = ImageIO.read(new File("src/main/images/titleScreen.jpg"));
        } catch (IOException e) {
            System.err.println("Error loading title image: " + e.getMessage());
        }
    }

    private void createComponents() {
        // Create name input field
        nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 20));
        nameField.setHorizontalAlignment(JTextField.CENTER);

        // Center the text field
        int fieldWidth = 200;
        int fieldHeight = 30;
        nameField.setBounds((WIDTH - fieldWidth) / 2, HEIGHT / 2, fieldWidth, fieldHeight);

        // Add action listener for Enter key
        nameField.addActionListener(e -> startGame());

        add(nameField);

        // Request focus on the text field
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    private void loadHighScores() {
        highScores = HighScore.loadHighScores();
    }

    private void startGame() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) {
            playerName = name;
            gameStarted = true;
            nameField.setEnabled(false);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw title
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "PAC-MAN";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (WIDTH - titleWidth) / 2, HEIGHT / 3);

        // Draw instructions
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        String instructions = "Enter your name and press Enter to start";
        fm = g2d.getFontMetrics();
        int instrWidth = fm.stringWidth(instructions);
        g2d.drawString(instructions, (WIDTH - instrWidth) / 2, HEIGHT / 2 - 40);

        if (gameStarted) {
            String startMsg = "Starting game...";
            fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(startMsg);
            g2d.drawString(startMsg, (WIDTH - msgWidth) / 2, HEIGHT * 2 / 3);
        }

        // Draw high scores
        if (!highScores.isEmpty()) {
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("High Scores:", 50, HEIGHT - 150);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            int y = HEIGHT - 120;
            for (int i = 0; i < Math.min(5, highScores.size()); i++) {
                HighScore score = highScores.get(i);
                g2d.drawString(String.format("%d. %s - %d",
                        i + 1,
                        score.getPlayerName(),
                        score.getScore()),
                        50, y);
                y += 20;
            }
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public String getPlayerName() {
        return playerName != null ? playerName : "Player";
    }
}