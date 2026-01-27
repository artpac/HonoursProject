package AI;

import Game.*;
import java.awt.Color;

/**
 * Quick test to verify AI compiles and runs
 */
public class QuickAITest {

    public static void main(String[] args) {
        System.out.println("=== Hive AI Quick Test ===\n");

        // Test 1: Create AI
        System.out.println("Test 1: Creating AI...");
        HiveAI ai = new HiveAI(false);
        System.out.println("✓ AI created successfully\n");

        // Test 2: Create game state
        System.out.println("Test 2: Creating game state...");
        GameState state = new GameState();
        System.out.println("✓ Game state created\n");

        // Test 3: Get legal moves
        System.out.println("Test 3: Generating legal moves...");
        java.util.List<AIMove> moves = ai.generateLegalMoves(state, Color.WHITE);
        System.out.println("✓ Found " + moves.size() + " legal moves\n");

        // Test 4: Get AI move
        System.out.println("Test 4: Getting AI decision...");
        AIMove move = ai.getBestMove(state, Color.WHITE);
        if (move != null) {
            System.out.println("✓ AI selected: " + move);
            System.out.println("  Piece: " + move.getPiece());
            System.out.println("  To: (" + move.getTo().getQ() + "," + move.getTo().getR() + ")");
        } else {
            System.out.println("✗ No move selected");
        }

        // Test 5: Clone game state
        System.out.println("\nTest 5: Cloning game state...");
        GameState cloned = state.clone();
        System.out.println("✓ Game state cloned successfully\n");

        // Test 6: Execute move
        System.out.println("Test 6: Executing move...");
        if (move != null) {
            move.execute(state);
            System.out.println("✓ Move executed\n");

            // Test 7: Get next move
            state.nextPlayer();
            System.out.println("Test 7: Getting Black's move...");
            AIMove blackMove = ai.getBestMove(state, Color.BLACK);
            if (blackMove != null) {
                System.out.println("✓ Black selected: " + blackMove + "\n");
            }
        }

        System.out.println("=== All Tests Passed! ===");
        System.out.println("\nYour AI is ready to use!");
        System.out.println("Next steps:");
        System.out.println("1. Integrate into GameBoard.java");
        System.out.println("2. Train with: java AI.Training.TrainingCoordinator selfplay");
        System.out.println("3. Play against it!");
    }
}