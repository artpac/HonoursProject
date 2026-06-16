# Hive Board Game AI

A complete implementation of the abstract strategy board game **Hive**, written in Java, featuring an AI opponent built with **Monte Carlo Tree Search (MCTS)**, a **neural-network evaluation function**, and **evolutionary self-play training**.

Built as my BSc (Hons) Computer Science honours project at Edinburgh Napier University.

> ℹ️ *Hive is a tile-based game where each player manoeuvres insect pieces — each with its own movement rules — with the goal of completely surrounding the opponent's queen bee.*


<img width="1512" height="949" alt="WinScreen" src="https://github.com/user-attachments/assets/9ff4a73a-71e7-4201-b80b-0df51f776797" />




## Features

- **Full game engine** — implements every piece type, its movement rules, and all win/lose conditions.
- **Playable UI** — play against the AI or against another person locally.
- **MCTS-based AI opponent** — searches the game tree using Monte Carlo Tree Search to choose strong moves under a time/iteration budget.
- **Neural-network evaluation** — a trained network scores board positions to guide the search, rather than relying on simple heuristics alone.
- **Evolutionary self-play training** — network weights are optimised over generations of self-play, so the AI improves the more it trains.

## How it works

The project combines three ideas that are usually studied separately:

1. **Search** — MCTS builds a tree of possible future positions, balancing exploration of new moves against exploitation of moves that have performed well.
2. **Evaluation** — instead of hand-written heuristics, a neural network estimates how good a given board position is. This guides the search toward promising branches.
3. **Learning** — the network's weights are trained through an evolutionary algorithm: many candidate networks play against each other, the strongest are selected, and their parameters are recombined and mutated to form the next generation.

Together, this lets the AI learn to play purely from self-play, with no human game data.

## Project structure

```bash
HonoursProject/
├── HexGame/        # Core source — game engine, AI, training, and UI
│   ├── Game/           # Board, pieces, movement rules, game state
│   ├── AI/             # Monte Carlo Tree Search + neural-network evaluation
│   ├── AI/Training/    # Evolutionary self-play training loop
│   ├── DataCollection/ # Logging and data gathering from games
│   └── UI/             # User interface
└── models/         # Saved / trained neural-network models
```

## Getting started

### Prerequisites
- Java JDK 17 or later
- IntelliJ IDEA (the repo includes IntelliJ project files), or any IDE / build tool you prefer

### Run it
1. Clone the repository:
   ```bash
   git clone https://github.com/artpac/HonoursProject.git
   ```
2. Open the project in IntelliJ IDEA (`File → Open` and select the project folder).
3. Let the IDE index the project, then run the main class.
   HexGame/UI/Main.jav

### Train the AI
1. Open Training Coordinator
2. Select Trainer
3. Adjust Values
4. Run

## Built with

- **Java** — game engine, AI, and UI
- **Monte Carlo Tree Search** — move selection
- **Neural networks** — position evaluation
- **Evolutionary algorithms** — self-play training

## Author

**Artur Pacut**
- Portfolio: [arturpacut.dev](https://www.arturpacut.dev/)
- LinkedIn: [in/artpac2050](https://www.linkedin.com/in/artpac2050/)
