# Squabbles: The Icon Matcher - Project Report

## 1. Project Overview
**Squabbles** is a fast-paced, real-time multiplayer icon matching game built with Java. The core mechanic involves finding the single matching icon between two cards—your card and the center card. The game supports multiplayer (1v1), single-player vs. Bot, and a practice mode. It features a modern JavaFX user interface, network-based gameplay, and persistent player statistics using SQLite.

## 2. Key Features
*   **Multiplayer (1v1)**: Play against another human player over a local network.
*   **Single Player (vs Bot)**: Challenge an AI opponent with adjustable difficulty.
*   **Practice Mode**: Play solo to hone your skills without an opponent.
*   **Game Mechanics**:
    *   **Turn-Based Action**: Players take turns to find the match.
    *   **10-Second Timer**: Each turn has a strict 10-second time limit.
    *   **Lives System**: Players start with 5 lives. Wrong guesses deduct a life.
    *   **Scoring**: +1 point for a correct match. First to 10 points wins.
*   **Persistence**: Player names and win/loss statistics are stored in a local SQLite database (`squabbles.db`).
*   **Modern UI**: A dark-themed, responsive UI built with JavaFX and CSS, featuring animations and visual feedback.

## 3. Technical Architecture
The project follows a **Client-Server** architecture using Java Sockets for communication and JavaFX for the presentation layer.

### 3.1. Server-Side (`com.squabbles.network`)
The server manages game state, matchmaking, and client connections.
*   **`GameServer`**: The entry point. It listens on a specific port (default: 5555) and accepts incoming socket connections.
*   **`LobbyServer`**: Manages the matchmaking queue. It pairs players into a `GameRoom` or creates a bot game.
*   **`GameRoom`**: The core game engine. It handles:
    *   **State Management**: Holds the `Deck`, `centerCard`, and player scores/lives.
    *   **Turn Logic**: Enforces the turn order and manages the 10-second `turnTimer`.
    *   **Validation**: Verifies if a player's move (icon match) is correct.
    *   **Broadcasting**: Sends state updates (`MSG_UPDATE_CARDS`, `MSG_TURN_UPDATE`) to all players in the room.
*   **`ServerClient`**: Represents a connected player. It handles reading/writing to the socket and stores player-specific data (name, score, lives).
*   **`BotClient`**: A subclass of `ServerClient` that simulates an AI player. It runs on a separate thread and automatically "finds" matches after a delay.

### 3.2. Client-Side (`com.squabbles.view`, `com.squabbles.network`)
The client handles the UI and communicates user actions to the server.
*   **`GameClient`**: Manages the socket connection. It sends requests (e.g., `MSG_MATCH_ATTEMPT`) and listens for server messages on a background thread. It uses callbacks to update the UI.
*   **`WelcomeView`**: The landing screen. It captures the player's name, initializes the database, and allows mode selection.
*   **`GameView`**: The main gameplay screen. It renders the cards, handles mouse clicks, displays the timer/lives, and shows visual feedback (animations).
*   **`GameOverView`**: Displays the final result and allows returning to the main menu.

### 3.3. Data Persistence (`com.squabbles.util`)
*   **`DatabaseManager`**: Uses JDBC to connect to a SQLite database. It handles creating the `players` table and updating win/loss records.
    *   **Schema**: Table `players` (`name` TEXT PRIMARY KEY, `wins` INTEGER, `losses` INTEGER).

### 3.4. Game Logic (`com.squabbles.model`, `com.squabbles.logic`)
*   **`DeckGenerator`**: Uses a mathematical algorithm (Projective Plane geometry) to generate a valid Dobble/Spot It! style deck where any two cards share exactly one symbol.
*   **`Card` & `Icon`**: Model classes representing the game entities.

## 4. Code Structure
```
src/main/java/com/squabbles/
├── logic/
│   └── DeckGenerator.java       # Math logic for card generation
├── model/
│   ├── Card.java                # Card entity
│   └── Icon.java                # Icon entity
├── network/
│   ├── BotClient.java           # AI Logic
│   ├── GameClient.java          # Client networking
│   ├── GameRoom.java            # Game session logic (Timer, Turns)
│   ├── GameServer.java          # Server entry point
│   ├── LobbyServer.java         # Matchmaking
│   ├── NetworkProtocol.java     # Message constants (e.g., "MATCH_ATTEMPT")
│   └── ServerClient.java        # Player connection handler
├── util/
│   ├── DatabaseManager.java     # SQLite JDBC handler
│   └── IconLoader.java          # Image/Emoji resource loader
├── view/
│   ├── GameOverView.java        # End screen
│   ├── GameView.java            # Main game UI
│   ├── SessionSetupView.java    # (Legacy/Alternative setup)
│   └── WelcomeView.java         # Main menu
└── Main.java                    # Application entry point
```

## 5. How It Works (Flow)
1.  **Start**: User runs `Main`. `WelcomeView` appears.
2.  **Connect**: User enters name and clicks **"Submit Name"**. Game buttons enable. User clicks "Single Player" or "Practice Mode". `GameServer` accepts connection.
3.  **Setup**: `LobbyServer` creates a `GameRoom` with the player and a `BotClient`.
4.  **Game Loop**:
    *   `GameRoom` broadcasts `MSG_START_GAME`.
    *   **Turn Start**: `GameRoom` picks a player, starts the 10s timer, and sends `MSG_TURN_UPDATE`.
    *   **Action**: Player clicks an icon. `GameClient` sends `MSG_MATCH_ATTEMPT <iconId>`.
    *   **Validation**: `GameRoom` checks if it's the player's turn and if the match is valid.
    *   **Result**:
        *   **Valid**: Score +1, Deck updates, Timer resets.
        *   **Invalid**: Lives -1.
        *   **Timeout**: Turn skips to opponent.
    *   **Update**: `GameRoom` broadcasts new state (`MSG_UPDATE_CARDS`, `MSG_MATCH_RESULT`).
5.  **End**: If a player reaches 10 points (Win) or 0 lives (Loss), `MSG_GAME_OVER` is sent. Database stats are updated.

## 6. How to Run
1.  **Prerequisites**: Ensure you have Java 21 and Maven installed.
2.  **Build**: Open a terminal in the project root and run:
    ```bash
    mvn clean package
    ```
3.  **Run**: Execute the batch file:
    ```bash
    run_console.bat
    ```

## 7. Technologies Used
*   **Java 21**: Core programming language.
*   **JavaFX**: For the rich, responsive User Interface.
*   **SQLite (JDBC)**: For lightweight, serverless database storage.
*   **Maven**: For dependency management and building.
