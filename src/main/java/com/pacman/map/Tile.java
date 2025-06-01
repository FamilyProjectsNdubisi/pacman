package com.pacman.map;

import java.awt.Color;

public enum Tile {
    EMPTY(' ', Color.BLACK),
    WALL('#', Color.BLUE),
    DOT('.', Color.WHITE),
    POWER_PELLET('O', Color.WHITE),
    PACMAN_SPAWN('P', Color.BLACK),
    GHOST_SPAWN('G', Color.BLACK);

    private final char symbol;
    private final Color color;

    Tile(char symbol, Color color) {
        this.symbol = symbol;
        this.color = color;
    }

    public char getSymbol() {
        return symbol;
    }

    public Color getColor() {
        return color;
    }

    public boolean isDot() {
        return this == DOT || this == POWER_PELLET;
    }

    public static Tile fromSymbol(char symbol) {
        for (Tile tile : values()) {
            if (tile.symbol == symbol) {
                return tile;
            }
        }
        return EMPTY;
    }
}