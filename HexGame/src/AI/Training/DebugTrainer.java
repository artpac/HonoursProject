package AI.Training;

import AI.*;
import Game.*;
import java.awt.Color;

/**
 * Diagnostic tool to find where training gets stuck
 */
public class DebugTrainer {

    public static void main(String[] args) {
        System.out.println("=== Training Debug Tool ===\n");

        // Test 1: Can we create an AI?
        System.out.println("Test 1: Creating AI...");
        try {
            HiveAI ai = new HiveAI(false);
            System.out.println("✓ AI created successfully\n");
        } catch (Exception e) {
            System.err.println("✗ Failed to create AI: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Test 2: Can we create a game state?
        System.out.println("Test 2: Creating game state...");
        GameState state = null;
        try {
            state = new GameState();
            System.out.println("✓ Game state created\n");
        } catch (Exception e) {
            System.err.println("✗ Failed to create game state: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Test 3: Can we get a move?
        System.out.println("Test 3: Getting AI move...");
        HiveAI testAI = new HiveAI(false);
        try {
            System.out.println("  Calling getBestMove()...");
            AIMove move = testAI.getBestMove(state, Color.WHITE);
            System.out.println("✓ Got move: " + move + "\n");
        } catch (Exception e) {
            System.err.println("✗ Failed to get move: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Test 4: Can we play a simple game?
        System.out.println("Test 4: Playing a simple game...");
        try {
            GameState gameState = new GameState();
            HiveAI ai1 = new HiveAI(false);

            for (int turn = 0; turn < 10; turn++) {
                System.out.println("  Turn " + (turn + 1) + "...");
                Color currentPlayer = gameState.getCurrentPlayer();

                AIMove move = ai1.getBestMove(gameState, currentPlayer);
                if (move == null) {
                    System.out.println("  No legal moves");
                    break;
                }

                System.out.println("    Move: " + move);
                move.execute(gameState);
                gameState.nextPlayer();
            }
            System.out.println("✓ Played 10 turns successfully\n");
        } catch (Exception e) {
            System.err.println("✗ Failed during game: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Test 5: Can we create a trainer?
        System.out.println("Test 5: Creating evolutionary trainer...");
        try {
            System.out.println("  Creating trainer with 2 agents...");
            EvolutionaryTrainer trainer = new EvolutionaryTrainer(2);
            System.out.println("✓ Trainer created\n");
        } catch (Exception e) {
            System.err.println("✗ Failed to create trainer: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Test 6: Can we run ONE generation?
        System.out.println("Test 6: Running ONE generation with 2 agents, 1 game...");
        try {
            EvolutionaryTrainer trainer = new EvolutionaryTrainer(2);
            System.out.println("  Starting evolution...");
            trainer.evolve(1, 1); // Just 1 generation, 1 game
            System.out.println("✓ Evolution completed!\n");
        } catch (Exception e) {
            System.err.println("✗ Evolution failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("=== All Tests Passed! ===");
        System.out.println("Training should work. If it still hangs, check:");
        System.out.println("1. Is models/ directory writable?");
        System.out.println("2. Enough RAM available?");
        System.out.println("3. Any infinite loops in MCTS?");
    }
}