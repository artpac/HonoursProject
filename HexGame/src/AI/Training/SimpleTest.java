package AI.Training;

import AI.*;
import Game.*;
import java.awt.Color;

/**
 * Simplest possible test - just play 2 AIs against each other
 */
public class SimpleTest {

    public static void main(String[] args) {
        System.out.println("=== Simple AI Test ===\n");

        System.out.println("Creating two AI players...");
        HiveAI white = new HiveAI(false);
        HiveAI black = new HiveAI(false);
        System.out.println("✓ AIs created\n");

        System.out.println("Starting game...");
        GameState state = new GameState();

        int maxTurns = 20;
        for (int turn = 0; turn < maxTurns; turn++) {
            Color currentPlayer = state.getCurrentPlayer();
            String playerName = currentPlayer.equals(Color.WHITE) ? "White" : "Black";
            HiveAI currentAI = currentPlayer.equals(Color.WHITE) ? white : black;

            System.out.println("\nTurn " + (turn + 1) + " (" + playerName + ")");
            System.out.println("  Getting move...");
            System.out.flush();

            long startTime = System.currentTimeMillis();
            AIMove move = currentAI.getBestMove(state, currentPlayer);
            long elapsed = System.currentTimeMillis() - startTime;

            if (move == null) {
                System.out.println("  No legal moves available");
                break;
            }

            System.out.println("  Move: " + move + " (took " + elapsed + "ms)");

            // Execute move
            move.execute(state);

            // Check win
            if (isGameOver(state)) {
                System.out.println("\n=== Game Over! ===");
                break;
            }

            state.nextPlayer();
        }

        System.out.println("\n=== Test Complete ===");
    }

    private static boolean isGameOver(GameState state) {
        HiveBoard board = state.getBoard();

        for (HexCoord coord : board.getAllCoordinates()) {
            Piece piece = board.getTopPieceAt(coord);
            if (piece != null && piece.getType() == PieceType.QUEEN) {
                int neighbors = 0;
                for (HexCoord neighbor : coord.getNeighbors()) {
                    if (board.containsCoord(neighbor)) {
                        neighbors++;
                    }
                }
                if (neighbors == 6) {
                    System.out.println("  " +
                            (piece.getColor().equals(Color.WHITE) ? "White" : "Black") +
                            " Queen is surrounded!");
                    return true;
                }
            }
        }
        return false;
    }
}