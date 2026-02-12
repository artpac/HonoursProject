package AI.Training;

import AI.*;
import Game.*;
import java.awt.Color;

/**
 * Find exactly where playGame() hangs
 */
public class DetailedGameDebug {

    public static void main(String[] args) {
        System.out.println("=== Detailed Game Debug ===\n");

        System.out.println("Creating two agents...");
        System.out.flush();
        HiveAI ai1 = new HiveAI(false);
        HiveAI ai2 = new HiveAI(false);
        System.out.println("✓ Agents created\n");

        System.out.println("Creating game state...");
        System.out.flush();
        GameState state = new GameState();
        System.out.println("✓ Game state created\n");

        System.out.println("Starting game simulation...");
        System.out.flush();

        int maxMoves = 20;
        for (int turn = 0; turn < maxMoves; turn++) {
            Color currentPlayer = state.getCurrentPlayer();
            HiveAI currentAI = currentPlayer.equals(Color.WHITE) ? ai1 : ai2;
            String playerName = currentPlayer.equals(Color.WHITE) ? "White" : "Black";

            System.out.println("\n--- Turn " + (turn + 1) + " (" + playerName + ") ---");
            System.out.flush();

            // CRITICAL: Time this operation
            System.out.print("  Calling getBestMove()...");
            System.out.flush();

            long startTime = System.currentTimeMillis();
            AIMove move = null;

            try {
                move = currentAI.getBestMove(state, currentPlayer);
                long elapsed = System.currentTimeMillis() - startTime;
                System.out.println(" took " + elapsed + "ms");
                System.out.flush();
            } catch (Exception e) {
                System.err.println(" ERROR!");
                System.err.flush();
                e.printStackTrace();
                return;
            }

            if (move == null) {
                System.out.println("  No legal moves");
                System.out.flush();
                break;
            }

            System.out.println("  Move: " + move);
            System.out.flush();

            // Execute move
            System.out.print("  Executing move...");
            System.out.flush();
            try {
                move.execute(state);
                System.out.println(" done");
                System.out.flush();
            } catch (Exception e) {
                System.err.println(" ERROR!");
                System.err.flush();
                e.printStackTrace();
                return;
            }

            // Check win
            System.out.print("  Checking win condition...");
            System.out.flush();
            if (checkWin(state)) {
                System.out.println(" Game over!");
                System.out.flush();
                break;
            }
            System.out.println(" continuing");
            System.out.flush();

            // Next player
            System.out.print("  Switching player...");
            System.out.flush();
            state.nextPlayer();
            System.out.println(" done");
            System.out.flush();

            // If first turn took more than 10 seconds, warn
            if (turn == 0 && (System.currentTimeMillis() - startTime) > 10000) {
                System.out.println("\n⚠️  WARNING: First move took over 10 seconds!");
                System.out.println("   This suggests MCTS is too slow or stuck.");
                System.out.println("   Training will be EXTREMELY slow.");
                System.out.flush();
            }
        }

        System.out.println("\n=== Debug Complete ===");
        System.out.println("If you see this message, basic game play works!");
        System.out.flush();
    }

    private static boolean checkWin(GameState state) {
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
                    return true;
                }
            }
        }
        return false;
    }
}