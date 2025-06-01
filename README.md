# Pac-Man Game

A Java implementation of the classic Pac-Man arcade game.

## Requirements

- Java 17 or later
- Maven

## Building the Project

To build the project, run:

```bash
mvn clean install
```

## Running the Game

To run the game, execute:

```bash
mvn exec:java -Dexec.mainClass="com.pacman.game.PacManGame"
```

## Project Structure

- `src/main/java/com/pacman/` - Main source code
  - `game/` - Core game logic
  - `entity/` - Game entities (Pac-Man, ghosts)
  - `map/` - Map and tile management
  - `ui/` - User interface components
  - `util/` - Utility classes
- `src/main/resources/` - Game resources
  - `sprites/` - Game sprites
  - `audio/` - Sound files
  - `maps/` - Map layouts
- `src/test/` - Test files

## Controls

- Arrow keys: Move Pac-Man
- P: Pause game
- ESC: Exit game

## License

This project is open source and available under the MIT License. 