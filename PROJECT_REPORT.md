# Project Document: OOP Squabbles (Icon Matcher Game)

## 1. Introduction and Game Overview
This document outlines the design and implementation plan for the **OOP Squabbles (Icon Matcher Game)**, a two-player, multi-threaded application developed in Java. The project's goal is to satisfy the requirements for the **M.C.A. Continuous Internal Evaluation (CIE) Mini Project** by demonstrating core Object-Oriented Programming (OOP) principles, multi-threading, and robust exception handling.

The game is a text-based adaptation of the "Spot It!" or "Dobble" card game, focusing on speed and pattern matching.

### 1.1 Game Rules (Core Logic)
*   **Deck**: A special deck of 54 cards is used. Each card features exactly 8 icons.
*   **The Key Rule**: Any two cards in the entire deck share **exactly one matching icon**.
*   **Setup**:
    *   The Dealer (Server) holds the remaining cards.
    *   Player 1 and Player 2 each receive one **Active Card**.
*   **Gameplay**:
    1.  The Dealer (Server) places a card face-up (the **Center Card**).
    2.  Both players simultaneously look at their Active Card and the Center Card to find the single matching icon.
    3.  The first player to submit the correct matching icon (via a text command) wins the Center Card.
    4.  The winning player places the won card face-up on their pile, and this card immediately becomes their new **Active Card** for the next round.
*   **End Game**: The game continues until the Dealer runs out of cards. The player who has collected the most cards wins.

---

## 2. OOP Concepts Coverage
This project is specifically designed to cover the required OOP and advanced Java concepts for the evaluation.

| Concept | Required Criteria | Implementation Details |
| :--- | :--- | :--- |
| **Inheritance** | Covered | A base class `Card` is extended by classes like `StandardCard`, `CenterCard`, and `PlayerActiveCard`. This establishes a clear hierarchy. |
| **Polymorphism** | Covered | The central method `findAndCheckMatch(Card other)` is defined in the base `Card` class but will be overridden or implemented differently in the game logic to determine the match outcome based on card type. |
| **Threads** | Covered (Critical) | A `GameServer` class spawns a dedicated `PlayerHandlerThread` for each of the two players. This is mandatory for simultaneously receiving input and accurately recording the fastest response time. |
| **Packages** | Covered | The entire codebase is organized into logical, domain-specific packages for maintainability and clear architecture (e.g., `com.squabbles.model`, `com.squabbles.network`). |
| **Exception Handling** | Covered | Custom exceptions will be used to ensure application robustness, especially under high-speed input conditions (e.g., `InvalidIconSubmissionException`, `ConnectionDroppedException`). |
| **Generics** | Covered (Recommended) | The central `Deck` class will be defined using generics, e.g., `Deck<T extends Card>`, to ensure type safety when managing the card inventory. |
| **RESTful API** | Bonus/Phase II | Can be demonstrated by making a simple API call (e.g., to a placeholder service) to log the final match results and fastest times after the game concludes. |

---

## 3. Design and Architecture
The application follows a standard **Client-Server Architecture** using Java Sockets for communication and a clear separation of concerns (Model-Logic-Network).

### 3.1 Class Structure (Model Layer)
The following classes represent the core game entities, leveraging Inheritance and Polymorphism:

| Class/Interface | Type | Responsibilities | Key OOP Tie-in |
| :--- | :--- | :--- | :--- |
| `Card` | Base Class | Defines fundamental properties: `cardId`, `Set<String> icons`. Contains the abstract `findMatch(Card other)`. | Inheritance, Polymorphism |
| `Deck` | Logic/Model | Manages the list of cards. Generates the initial 54 cards using the special algorithm. | Generics (`Deck<Card>`), Encapsulation |
| `Player` | Model | Stores player state: name, score (cards collected), `activeCard`. | Encapsulation |
| `GameServer` | Network/Controller | Initializes the deck and board, manages the main game loop, and accepts client connections. | Threading (Main) |

### 3.2 Network and Threading Architecture
1.  **Server Initialization**: The `GameServer` starts and listens on a specific port.
2.  **Client Connection**: When a player runs the `GameClient`, a connection is established.
3.  **Dedicated Threads**: Upon connection, the `GameServer` immediately creates and starts a separate `PlayerHandlerThread` for that player.

| Thread/Component | Responsibility | Why a Thread is Needed |
| :--- | :--- | :--- |
| **GameServer** (Main Thread) | Manages the game state, dealing the next card, and determining the winner of the round. | Coordinates the game, ensuring turns are sequential after a match is resolved. |
| **PlayerHandlerThread** (Two Instances) | Simultaneously listens for a player's `MATCH <ICON>` command and records the exact submission time. | Enables the "first-to-click" rule by logging concurrent, real-time input from both players. |

---

## 4. Implementation Details (How it will be built)

### 4.1 The Card Generation Algorithm
The key to the game is the mathematical guarantee that any two cards share exactly one icon. This requires a specific algorithm based on **Finite Projective Planes** (the geometry behind the Dobble game), where the number of cards, icons per card, and total icons relate to a prime number $p$.

1.  **$p$ Selection**: For $N=8$ icons per card, $p$ should be 7.
2.  **Total Icons**: The total number of unique icons $I$ required is $p^2 + p + 1 = 7^2 + 7 + 1 = \mathbf{57}$ (the 20 icons mentioned by the user is likely a simplification, the math requires 57 icons for 57 cards). We will use a subset of this math for 54 cards.
3.  **Implementation**: The server will use three families of cards (sets of icons) based on algebraic equations to generate the icons, ensuring the one-match rule is satisfied before the game starts.

### 4.2 Player Submission and Match Resolution
1.  **Server Deal**: The server prints the Center Card's icons to both clients and sets a timer.
2.  **Player Input**: Players submit: `MATCH <IconName>`.
3.  **Resolution Logic**:
    *   The `PlayerHandlerThread` for each player receives the input and records its timestamp.
    *   The `GameServer` checks which thread submitted the answer first (`min(timestamp_1, timestamp_2)`).
    *   The answer from the fastest player is checked against the correct match (using the polymorphic `findMatch()` logic).
    *   If correct, the card is awarded, the player's `activeCard` is updated, and the new score is announced.
    *   If incorrect, an Exception is thrown, the card is not awarded, and the slow player is given a chance.

This structure ensures that the logic is clean, the OOP principles are fully utilized, and the core complexity (threading and card math) is directly addressed.
