package com.pacman.map;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameMap {
    private static final int TILE_SIZE = 40; // Match Pac-Man's size
    private Tile[][] maze;
    private int width;
    private int height;
    private Point pacmanSpawn;
    private List<Point> ghostSpawns;

    public GameMap() {
        ghostSpawns = new ArrayList<>();
        loadDefaultMap();
    }

    private void loadDefaultMap() {
        String[] defaultMap = {
                "############################",
                "#............##............#",
                "#.####.#####.##.#####.####.#",
                "#O####.#####.##.#####.####O#",
                "#..........................#",
                "#.####.##.########.##.####.#",
                "#......##....##....##......#",
                "######.##### ## #####.######",
                "     #.##          ##.#     ",
                "     #.## ###--### ##.#     ",
                "######.## #  G  G# ##.######",
                "      .   #  G  G#   .      ",
                "######.## #      # ##.######",
                "     #.## ######## ##.#     ",
                "     #.##    P     ##.#     ",
                "######.## ######## ##.######",
                "#............##............#",
                "#.####.#####.##.#####.####.#",
                "#O..##................##..O#",
                "###.##.##.########.##.##.###",
                "#......##....##....##......#",
                "#.##########.##.##########.#",
                "#..........................#",
                "############################"
        };

        height = defaultMap.length;
        width = defaultMap[0].length();
        maze = new Tile[height][width];

        for (int y = 0; y < height; y++) {
            String row = defaultMap[y];
            for (int x = 0; x < width; x++) {
                char symbol = row.charAt(x);
                Tile tile = Tile.fromSymbol(symbol);
                maze[y][x] = tile;

                if (tile == Tile.PACMAN_SPAWN) {
                    pacmanSpawn = new Point(x, y);
                    maze[y][x] = Tile.EMPTY; // Replace spawn point with empty tile
                } else if (tile == Tile.GHOST_SPAWN) {
                    ghostSpawns.add(new Point(x, y));
                    maze[y][x] = Tile.EMPTY; // Replace spawn point with empty tile
                }
            }
        }
    }

    public void render(Graphics2D g2d, int offsetX, int offsetY) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Tile tile = maze[y][x];
                if (tile != Tile.EMPTY) {
                    g2d.setColor(tile.getColor());
                    if (tile == Tile.WALL) {
                        g2d.fillRect(offsetX + x * TILE_SIZE,
                                offsetY + y * TILE_SIZE,
                                TILE_SIZE, TILE_SIZE);
                    } else if (tile == Tile.DOT) {
                        int dotSize = 6;
                        g2d.fillOval(offsetX + x * TILE_SIZE + (TILE_SIZE - dotSize) / 2,
                                offsetY + y * TILE_SIZE + (TILE_SIZE - dotSize) / 2,
                                dotSize, dotSize);
                    } else if (tile == Tile.POWER_PELLET) {
                        int pelletSize = 12;
                        g2d.fillOval(offsetX + x * TILE_SIZE + (TILE_SIZE - pelletSize) / 2,
                                offsetY + y * TILE_SIZE + (TILE_SIZE - pelletSize) / 2,
                                pelletSize, pelletSize);
                    }
                }
            }
        }
    }

    public Tile getTileAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return maze[y][x];
        }
        return Tile.WALL;
    }

    public void setTileAt(int x, int y, Tile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            maze[y][x] = tile;
        }
    }

    public Point getPacmanSpawn() {
        return new Point(pacmanSpawn);
    }

    public List<Point> getGhostSpawns() {
        return new ArrayList<>(ghostSpawns);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }
}