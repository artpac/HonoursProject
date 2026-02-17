package AI.Training;

import AI.*;
import Game.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main coordinator for all AI training methods
 */
public class TrainingCoordinator {

    public static void main(String[] args) {
        System.out.println("=== Hive AI Training System ===\n");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDateTime = LocalDateTime.now().format(dateFormat);
        System.out.println(formattedDateTime);

        String mode = args.length > 0 ? args[0] : "evolution";

        switch (mode.toLowerCase()) {
            case "selfplay":
                runSelfPlayTraining();
                break;
            case "evolution":
                runEvolutionaryTraining();
                break;
            case "combined":
                runCombinedTraining();
                break;
            case "test":
                testAI();
                break;
            default:
                System.out.println("Usage: java TrainingCoordinator [selfplay|evolution|combined|test]");
        }
    }

    /**
     * Self-play reinforcement learning
     */
    private static void runSelfPlayTraining() {
        System.out.println("Starting self-play training...\n");

        HiveAI agent = new HiveAI(true);
        SelfPlayTrainer trainer = new SelfPlayTrainer(agent);

        // Set number of games
        trainer.train(1, true);

        // Export training data
        trainer.exportTrainingData("training_data.csv");

        System.out.println("\nSelf-play training complete!");
    }

    /**
     * Evolutionary algorithm training
     */
    private static void runEvolutionaryTraining() {
        System.out.println("Starting evolutionary training...\n");

        EvolutionaryTrainer evolver = new EvolutionaryTrainer(5); // Population of 50

        // Evolve for 100 generations
        evolver.evolve(2, 5); // 5 games per evaluation

        // Export population statistics
        evolver.exportStats("evolution_stats.csv");

        System.out.println("\nEvolutionary training complete!");
    }

    /**
     * Combined training approach
     */
    private static void runCombinedTraining() {
        System.out.println("Starting combined training...\n");

        // Phase 1: Evolutionary training for diversity
        System.out.println("Phase 1: Evolutionary initialization (50 generations)");
        EvolutionaryTrainer evolver = new EvolutionaryTrainer(30);
        evolver.evolve(50, 3);

        // Get best evolved agent
        HiveAI bestAgent = evolver.getBestAgent();

        // Phase 2: Refine with self-play
        System.out.println("\nPhase 2: Self-play refinement (500 games)");
        SelfPlayTrainer refiner = new SelfPlayTrainer(bestAgent);
        refiner.train(500, true);

        System.out.println("\nCombined training complete!");
        System.out.println("Best agent saved to models/hive_network.dat");
    }

    /**
     * Test trained AI
     */
    private static void testAI() {
        System.out.println("Testing trained AI...\n");

        HiveAI agent = new HiveAI(true); // Load trained weights
        GameState testState = new GameState();

        // Play a few test moves
        for (int i = 0; i < 10; i++) {
            AIMove move = agent.getBestMove(testState, testState.getCurrentPlayer());
            if (move == null) {
                System.out.println("No legal moves available");
                break;
            }

            System.out.printf("Turn %d: %s %s at (%d,%d)\n",
                    i + 1,
                    move.getPiece().getColor().equals(java.awt.Color.WHITE) ? "White" : "Black",
                    move.getPiece().getType().name(),
                    move.getTo().getQ(),
                    move.getTo().getR()
            );

            move.execute(testState);
            testState.nextPlayer();
        }

        System.out.println("\nTest complete!");
    }
}

/**
 * Example usage in your game
 */
class AIOpponentExample {

    /**
     * Integrate AI into your game
     */
    public static void integrateIntoGame() {
        // In your GameBoard class, add:

        // 1. Create AI opponent
        HiveAI aiOpponent = new HiveAI(true); // Load trained model

        // 2. After player makes move, get AI response
        /*
        public void afterPlayerMove() {
            if (gameState.getCurrentPlayer().equals(Color.BLACK)) {
                // AI's turn
                AIMove aiMove = aiOpponent.getBestMove(gameState, Color.BLACK);

                if (aiMove != null) {
                    // Execute AI move
                    if (aiMove.getType() == MoveType.PLACE) {
                        gameState.getBoard().placePiece(aiMove.getPiece(), aiMove.getTo());
                        gameState.removePieceFromReserve(aiMove.getPiece());

                        if (aiMove.getPiece().getType() == PieceType.QUEEN) {
                            gameState.setQueenPlaced(Color.BLACK);
                        }
                    } else {
                        gameState.getBoard().movePiece(aiMove.getFrom(), aiMove.getTo());
                    }

                    saveGame(aiMove.getTo());
                    nextTurn();
                    repaint();
                }
            }
        }
        */
    }
}

/* ============================================
   USAGE GUIDE
   ============================================

1. INITIAL SETUP:
   - Create 'models/' directory in your project root
   - Optionally set ANTHROPIC_API_KEY environment variable for LLM features

2. TRAINING OPTIONS:

   A) Quick Start (Self-play):
      java AI.Training.TrainingCoordinator selfplay
      - Trains one agent through self-play
      - Good for getting started quickly
      - ~30 minutes for 1000 games

   B) Evolutionary Training:
      java AI.Training.TrainingCoordinator evolution
      - Creates diverse population of agents
      - Slower but more robust
      - ~2-3 hours for 100 generations

   C) Combined (Recommended):
      java AI.Training.TrainingCoordinator combined
      - Best of both approaches
      - Creates strong, diverse agent
      - ~3-4 hours total

3. INTEGRATE INTO YOUR GAME:

   In MainScreen.java, modify the "Player Vs Computer" button:

   pvc.addActionListener(f -> {
       SwingUtilities.invokeLater(() -> {
           File saveGame = createSaveFile();
           HiveGame game = new HiveGame(saveGame);

           // Add AI opponent
           HiveAI aiOpponent = new HiveAI(true);
           game.getGameBoard().setAIOpponent(aiOpponent);

           game.setVisible(true);
       });
   });

4. ADD TO GameBoard.java:

   private HiveAI aiOpponent;

   public void setAIOpponent(HiveAI ai) {
       this.aiOpponent = ai;
   }

   private void nextTurn() {
       // ... existing code ...

       // After switching turns, check if AI should move
       if (aiOpponent != null &&
           gameState.getCurrentPlayer().equals(Color.BLACK)) {
           SwingUtilities.invokeLater(() -> makeAIMove());
       }
   }

   private void makeAIMove() {
       AIMove move = aiOpponent.getBestMove(gameState, Color.BLACK);
       if (move != null) {
           if (move.getType() == MoveType.PLACE) {
               gameState.getBoard().placePiece(move.getPiece(), move.getTo());
               gameState.removePieceFromReserve(move.getPiece());
               if (move.getPiece().getType() == PieceType.QUEEN) {
                   gameState.setQueenPlaced(Color.BLACK);
               }
           } else {
               gameState.getBoard().movePiece(move.getFrom(), move.getTo());
           }
           saveGame(move.getTo());
           nextTurn();
           repaint();
       }
   }

5. TESTING:
   java AI.Training.TrainingCoordinator test

6. FEATURES:
   - Reinforcement Learning: Agent learns from winning/losing
   - Evolutionary Algorithm: Population evolves better strategies
   - MCTS: Tactical search for best moves
   - LLM Integration: Strategic creativity (requires API key)
   - Self-play: Continuous improvement

7. TUNING PARAMETERS:

   In HiveAI.java:
   - explorationRate: How often to try new moves (0.15 = 15%)

   In MCTSEngine.java:
   - explorationConstant: Balance exploration vs exploitation
   - iterations: More = stronger but slower (500-1000 recommended)

   In EvolutionaryTrainer.java:
   - populationSize: Larger = more diversity, slower training
   - mutationRate: How often to mutate (0.1 = 10%)
   - mutationStrength: Size of mutations

8. ADVANCED: Multiple AI Personalities

   Train different AIs with different parameters:
   - Aggressive: High exploration, prioritize attacking queen
   - Defensive: Low exploration, protect own queen
   - Balanced: Default parameters

   Store in different model files and randomly select!

============================================ */