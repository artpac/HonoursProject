package AI;

import Game.*;
import java.awt.Color;
import java.util.*;

/**
 * Hybrid AI System combining RL, Evolutionary Algorithms, and MCTS
 */
public class HiveAI {
    public NeuralNetwork policyNetwork;  // Changed to public
    public NeuralNetwork valueNetwork;   // Changed to public
    private MCTSEngine mctsEngine;
    private double explorationRate = 0.15;

    public HiveAI(boolean loadWeights) {
        this.policyNetwork = new NeuralNetwork(loadWeights);
        this.valueNetwork = new NeuralNetwork(loadWeights);
        this.mctsEngine = new MCTSEngine(policyNetwork, valueNetwork);
    }

    /**
     * Main decision method - chooses best move
     */
    public AIMove getBestMove(GameState state, Color aiColor) {
        // Get board state encoding
        double[] stateVector = encodeGameState(state, aiColor);

        // Generate all legal moves
        List<AIMove> legalMoves = generateLegalMoves(state, aiColor);
        if (legalMoves.isEmpty()) return null;

        // Blend different AI approaches based on game phase
        int turnCount = state.getTurnCount();

        if (turnCount < 4) {
            // Early game: Simple heuristic
            return getEarlyGameMove(state, legalMoves, aiColor);
        } else if (turnCount < 12) {
            // Mid game: Blend RL + MCTS
            return getBlendedMove(state, legalMoves, stateVector);
        } else {
            // End game: Pure MCTS for tactical precision
            return mctsEngine.search(state, aiColor, 200);  // Reduced from 1000
        }
    }

    /**
     * Generate all legal moves for current state
     */
    public List<AIMove> generateLegalMoves(GameState state, Color color) {  // Changed to public
        List<AIMove> moves = new ArrayList<>();
        HiveBoard board = state.getBoard();

        // Placement moves - use ACTUAL pieces from reserve
        List<Piece> reserve = state.getReserve(color);
        for (Piece piece : reserve) {
            // Must place queen by turn 4
            if (state.mustPlaceQueen() && piece.getType() != PieceType.QUEEN) {
                continue;
            }

            List<HexCoord> validPlacements = getValidPlacementCoords(board, piece, color);
            for (HexCoord coord : validPlacements) {
                // Use the ACTUAL piece from reserve, not a new one
                moves.add(new AIMove(piece, null, coord, MoveType.PLACE));
            }
        }

        // Movement moves (only if queen is placed)
        if (state.isQueenPlaced(color)) {
            // Create a copy of the coordinates to avoid ConcurrentModificationException
            Set<HexCoord> coordinates = new HashSet<>(board.getBoard().keySet());
            for (HexCoord coord : coordinates) {
                Piece topPiece = board.getTopPieceAt(coord);
                if (topPiece != null && topPiece.getColor().equals(color)) {
                    MoveCalculator calc = new MoveCalculator(board, new MovementValidator(board));
                    List<HexCoord> validMoves = calc.getValidMoves(topPiece, coord);
                    for (HexCoord dest : validMoves) {
                        moves.add(new AIMove(topPiece, coord, dest, MoveType.MOVE));
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Get valid placement coordinates
     */
    private List<HexCoord> getValidPlacementCoords(HiveBoard board, Piece piece, Color color) {
        List<HexCoord> coords = new ArrayList<>();
        PlacementValidator validator = new PlacementValidator(board);

        if (board.isEmpty()) {
            coords.add(new HexCoord(0, 0));
            return coords;
        }

        // Check all positions adjacent to existing pieces
        Set<HexCoord> checked = new HashSet<>();
        for (HexCoord existing : board.getAllCoordinates()) {
            for (HexCoord neighbor : existing.getNeighbors()) {
                if (!checked.contains(neighbor)) {
                    // Use the actual piece passed in, don't create a new one
                    if (validator.canPlaceAt(neighbor, piece)) {
                        coords.add(neighbor);
                    }
                    checked.add(neighbor);
                }
            }
        }

        return coords;
    }

    /**
     * Encode game state as neural network input
     */
    public double[] encodeGameState(GameState state, Color aiColor) {  // Changed to public
        // Feature vector: 11x11 grid * 2 colors * 5 piece types = 1210 features
        // + game phase features = 1220 total
        double[] features = new double[1220];
        int idx = 0;

        // Encode board state
        HiveBoard board = state.getBoard();
        for (int q = -5; q <= 5; q++) {
            for (int r = -5; r <= 5; r++) {
                HexCoord coord = new HexCoord(q, r);
                Piece piece = board.getTopPieceAt(coord);

                if (piece != null) {
                    int colorOffset = piece.getColor().equals(aiColor) ? 0 : 5;
                    int typeIdx = piece.getType().ordinal();
                    features[idx + colorOffset + typeIdx] = 1.0;
                }
                idx += 10; // 5 types * 2 colors
            }
        }

        // Game phase features
        features[1210] = state.getTurnCount() / 30.0; // Normalize turn count
        features[1211] = state.isQueenPlaced(aiColor) ? 1.0 : 0.0;
        features[1212] = state.isQueenPlaced(getOpponentColor(aiColor)) ? 1.0 : 0.0;
        features[1213] = state.getReserve(aiColor).size() / 11.0; // Normalize reserve
        features[1214] = board.size() / 22.0; // Normalize board size

        return features;
    }

    /**
     * Blend RL policy with MCTS
     */
    private AIMove getBlendedMove(GameState state, List<AIMove> moves, double[] stateVector) {
        // Get policy network probabilities
        double[] policyProbs = policyNetwork.forward(stateVector);

        // Run limited MCTS (reduced iterations for speed)
        AIMove mctsMove = mctsEngine.search(state, state.getCurrentPlayer(), 100);  // Reduced from 500

        // Blend: 70% MCTS, 30% policy network
        if (Math.random() < 0.7 && mctsMove != null) {
            return mctsMove;
        } else {
            return selectMoveFromPolicy(moves, policyProbs);
        }
    }

    /**
     * Get early game move using simple heuristics
     */
    private AIMove getEarlyGameMove(GameState state, List<AIMove> moves, Color color) {
        // First move: place at center
        if (state.getTurnCount() == 0) {
            for (AIMove move : moves) {
                if (move.getTo().equals(new HexCoord(0, 0))) {
                    return move;
                }
            }
        }

        // Must place queen by turn 3-4
        if (state.mustPlaceQueen()) {
            for (AIMove move : moves) {
                if (move.getPiece().getType() == PieceType.QUEEN) {
                    return move;
                }
            }
        }

        // Otherwise, select a random valid move
        return moves.isEmpty() ? null : moves.get(new Random().nextInt(moves.size()));
    }

    /**
     * Select move based on policy probabilities
     */
    private AIMove selectMoveFromPolicy(List<AIMove> moves, double[] probs) {
        if (moves.isEmpty()) return null;

        // Softmax with temperature
        double temperature = 0.5;
        double[] adjustedProbs = new double[moves.size()];
        double sum = 0.0;

        for (int i = 0; i < moves.size(); i++) {
            adjustedProbs[i] = Math.exp(probs[i % probs.length] / temperature);
            sum += adjustedProbs[i];
        }

        // Sample from distribution
        double rand = Math.random() * sum;
        double cumulative = 0.0;
        for (int i = 0; i < moves.size(); i++) {
            cumulative += adjustedProbs[i];
            if (rand <= cumulative) {
                return moves.get(i);
            }
        }

        return moves.get(0);
    }

    private Color getOpponentColor(Color color) {
        return color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
}