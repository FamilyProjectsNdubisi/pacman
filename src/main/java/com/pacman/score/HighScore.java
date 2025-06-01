package com.pacman.score;

import java.io.*;
import java.util.*;

public class HighScore implements Serializable {
    private static final String SCORES_FILE = "highscores.dat";
    private static final int MAX_HIGH_SCORES = 10;

    private String playerName;
    private int score;
    private Date date;

    public HighScore(String playerName, int score) {
        this.playerName = playerName;
        this.score = score;
        this.date = new Date();
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public Date getDate() {
        return date;
    }

    @SuppressWarnings("unchecked")
    public static List<HighScore> loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORES_FILE))) {
            return (List<HighScore>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public static void saveHighScore(HighScore newScore) {
        List<HighScore> scores = loadHighScores();
        scores.add(newScore);
        scores.sort((a, b) -> b.getScore() - a.getScore());

        if (scores.size() > MAX_HIGH_SCORES) {
            scores = scores.subList(0, MAX_HIGH_SCORES);
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORES_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isHighScore(int score) {
        List<HighScore> scores = loadHighScores();
        return scores.size() < MAX_HIGH_SCORES || score > scores.get(scores.size() - 1).getScore();
    }

    @Override
    public String toString() {
        return String.format("%s - %d points (%tF)", playerName, score, date);
    }
}