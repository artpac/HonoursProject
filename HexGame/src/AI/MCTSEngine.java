package AI;

import Game.*;
import java.awt.Color;
import java.util.*;

/**
 * Monte Carlo Tree Search with neural network guidance
 */
public class MCTSEngine {
    private NeuralNetwork policyNet;
    private NeuralNetwork valueNet;
    private double explorationConstant = 1.41; // UCT constant

    public MCTSEngine(NeuralNetwork policy, NeuralNetwork value) {
        this.policyNet = policy;
        this.valueNet = value;
    }

    /**
     * Run MCTS search and return best move
     */
    public AIMove search(GameState rootState, Color aiColor, int iterations) {
        MCTSNode root = new MCTSNode(null, null, rootState.clone());

        // Safety: limit iterations and add timeout
        int maxIterations = Math.min(iterations, 100); // Reduced cap from 1000 to 100
        long startTime = System.currentTimeMillis();
        long timeoutMs = 5000; // Reduced from 10 seconds to 5 seconds

        for (int i = 0; i < maxIterations; i++) {
            // Timeout check every 10 iterations to reduce overhead
            if (i % 10 == 0 && System.currentTimeMillis() - startTime > timeoutMs) {
                System.out.println("MCTS timeout after " + i + " iterations");
                break;
            }

            MCTSNode node = root;
            GameState state = rootState.clone();

            // Selection: traverse tree using UCT
            int selectionDepth = 0;
            while (!node.isLeaf() && !isTerminal(state) && selectionDepth < 100) {
                node = selectChild(node);
                if (node == null || node.move == null) break;
                node.move.execute(state);
                selectionDepth++;
            }

            // Expansion: add children
            if (!isTerminal(state) && node.visits > 0) {
                expand(node, state);
                if (!node.children.isEmpty()) {
                    node = node.children.get(0);
                    if (node.move != null) {
                        node.move.execute(state);
                    }
                }
            }

            // Simulation: evaluate position
            double value = evaluate(state, aiColor);

            // Backpropagation: update statistics
            backpropagate(node, value);
        }

        // Return move with highest visit count
        return getBestMove(root);
    }

    /**
     * Select child using UCT formula
     */
    private MCTSNode selectChild(MCTSNode node) {
        if (node.children.isEmpty()) return null;

        MCTSNode best = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (MCTSNode child : node.children) {
            double exploitation = child.totalValue / (child.visits + 1e-8);
            double exploration = explorationConstant *
                    Math.sqrt(Math.log(node.visits + 1) / (child.visits + 1e-8));
            double uctValue = exploitation + exploration;

            // Add policy network prior
            if (policyNet != null) {
                uctValue += 0.3 * child.prior;
            }

            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = child;
            }
        }

        return best;
    }

    /**
     * Expand node by adding all legal moves as children
     */
    private void expand(MCTSNode node, GameState state) {
        Color currentPlayer = state.getCurrentPlayer();
        List<AIMove> moves = generateMoves(state, currentPlayer);

        // Safety: Limit number of children
        int maxChildren = 50;
        moves = moves.size() > maxChildren ? moves.subList(0, maxChildren) : moves;

        // Get policy priors from neural network
        double[] priors = null;
        if (policyNet != null) {
            try {
                double[] stateVec = encodeState(state, currentPlayer);
                priors = policyNet.forward(stateVec);
            } catch (Exception e) {
                // If neural network fails, continue without priors
                priors = null;
            }
        }

        for (int i = 0; i < moves.size(); i++) {
            double prior = (priors != null && i < priors.length) ? priors[i] : 1.0 / moves.size();
            GameState childState = state.clone();
            MCTSNode child = new MCTSNode(node, moves.get(i), childState);
            child.prior = prior;
            node.children.add(child);
        }
    }

    /**
     * Evaluate position using neural network or heuristic
     */
    private double evaluate(GameState state, Color aiColor) {
        // Check for terminal state using our own win detection
        String winResult = checkWinCondition(state);

        if (winResult != null) {
            if (winResult.contains("Draw")) return 0.5;
            boolean aiWon = (aiColor.equals(Color.WHITE) && winResult.contains("White wins")) ||
                    (aiColor.equals(Color.BLACK) && winResult.contains("Black wins"));
            return aiWon ? 1.0 : 0.0;
        }

        // Use value network if available
        if (valueNet != null) {
            double[] stateVec = encodeState(state, aiColor);
            double[] output = valueNet.forward(stateVec);
            return output[0];
        }

        // Fallback: heuristic evaluation
        return heuristicEval(state, aiColor);
    }

    /**
     * Heuristic evaluation function
     */
    private double heuristicEval(GameState state, Color aiColor) {
        HiveBoard board = state.getBoard();
        double score = 0.5; // Neutral starting point

        // Find queens - use copy to avoid ConcurrentModificationException
        HexCoord aiQueenPos = null;
        HexCoord oppQueenPos = null;

        Set<HexCoord> coordinates = new HashSet<>(board.getBoard().keySet());
        for (HexCoord coord : coordinates) {
            List<Piece> stack = board.getStackAt(coord);
            if (stack != null && !stack.isEmpty()) {
                Piece piece = stack.get(0);
                if (piece.getType() == PieceType.QUEEN) {
                    if (piece.getColor().equals(aiColor)) {
                        aiQueenPos = coord;
                    } else {
                        oppQueenPos = coord;
                    }
                }
            }
        }

        // Evaluate queen safety
        if (aiQueenPos != null) {
            int aiQueenNeighbors = countNeighbors(board, aiQueenPos);
            score -= aiQueenNeighbors * 0.08; // Penalize surrounded queen
        }

        if (oppQueenPos != null) {
            int oppQueenNeighbors = countNeighbors(board, oppQueenPos);
            score += oppQueenNeighbors * 0.08; // Reward surrounding opponent queen
        }

        // Piece mobility
        int aiMobility = countMobility(state, aiColor);
        int oppMobility = countMobility(state, getOpponent(aiColor));
        score += (aiMobility - oppMobility) * 0.01;

        // Control of center
        score += evaluatePosition(board, aiColor) * 0.05;

        return Math.max(0.0, Math.min(1.0, score));
    }

    private int countNeighbors(HiveBoard board, HexCoord coord) {
        int count = 0;
        for (HexCoord neighbor : coord.getNeighbors()) {
            if (board.containsCoord(neighbor)) count++;
        }
        return count;
    }

    private int countMobility(GameState state, Color color) {
        int moves = 0;
        Set<HexCoord> coordinates = new HashSet<>(state.getBoard().getBoard().keySet());
        for (HexCoord coord : coordinates) {
            Piece piece = state.getBoard().getTopPieceAt(coord);
            if (piece != null && piece.getColor().equals(color)) {
                MoveCalculator calc = new MoveCalculator(
                        state.getBoard(), new MovementValidator(state.getBoard()));
                moves += calc.getValidMoves(piece, coord).size();
            }
        }
        return moves;
    }

    private double evaluatePosition(HiveBoard board, Color color) {
        double score = 0.0;
        Set<HexCoord> coordinates = new HashSet<>(board.getBoard().keySet());
        for (HexCoord coord : coordinates) {
            List<Piece> stack = board.getStackAt(coord);
            if (stack != null && !stack.isEmpty()) {
                Piece piece = stack.get(0);
                if (piece.getColor().equals(color)) {
                    // Prefer pieces closer to center
                    double distance = Math.sqrt(coord.getQ() * coord.getQ() + coord.getR() * coord.getR());
                    score += 1.0 / (1.0 + distance);
                }
            }
        }
        return score;
    }

    /**
     * Backpropagate result up the tree
     */
    private void backpropagate(MCTSNode node, double value) {
        while (node != null) {
            node.visits++;
            node.totalValue += value;
            node = node.parent;
            value = 1.0 - value; // Flip for opponent
        }
    }

    /**
     * Get best move based on visit counts
     */
    private AIMove getBestMove(MCTSNode root) {
        MCTSNode best = null;
        int maxVisits = -1;

        for (MCTSNode child : root.children) {
            if (child.visits > maxVisits) {
                maxVisits = child.visits;
                best = child;
            }
        }

        return best != null ? best.move : null;
    }

    private boolean isTerminal(GameState state) {
        return checkWinCondition(state) != null;
    }

    /**
     * Check win condition without needing a save file
     */
    private String checkWinCondition(GameState state) {
        HiveBoard board = state.getBoard();
        boolean whiteQueenSurrounded = false;
        boolean blackQueenSurrounded = false;

        Set<HexCoord> coordinates = new HashSet<>(board.getBoard().keySet());
        for (HexCoord coord : coordinates) {
            List<Piece> stack = board.getStackAt(coord);
            if (stack != null && !stack.isEmpty()) {
                Piece piece = stack.get(0); // Bottom piece
                if (piece.getType() == PieceType.QUEEN) {
                    // Count neighbors
                    int neighbors = 0;
                    for (HexCoord neighbor : coord.getNeighbors()) {
                        if (board.containsCoord(neighbor)) {
                            neighbors++;
                        }
                    }

                    if (neighbors == 6) {
                        if (piece.getColor().equals(Color.WHITE)) {
                            whiteQueenSurrounded = true;
                        } else {
                            blackQueenSurrounded = true;
                        }
                    }
                }
            }
        }

        if (whiteQueenSurrounded && blackQueenSurrounded) {
            return "Draw! Both Queens surrounded!";
        } else if (whiteQueenSurrounded) {
            return "Black wins! White Queen surrounded!";
        } else if (blackQueenSurrounded) {
            return "White wins! Black Queen surrounded!";
        }

        return null; // Game continues
    }

    private List<AIMove> generateMoves(GameState state, Color color) {
        // Create temporary AI to access the public method
        HiveAI tempAI = new HiveAI(false);
        return tempAI.generateLegalMoves(state, color);
    }

    private double[] encodeState(GameState state, Color color) {
        // Create temporary AI to access the public method
        HiveAI tempAI = new HiveAI(false);
        return tempAI.encodeGameState(state, color);
    }

    private Color getOpponent(Color color) {
        return color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;
    }
}

/**
 * MCTS Tree Node
 */
class MCTSNode {
    MCTSNode parent;
    AIMove move;
    GameState state;
    List<MCTSNode> children;
    int visits;
    double totalValue;
    double prior;

    public MCTSNode(MCTSNode parent, AIMove move, GameState state) {
        this.parent = parent;
        this.move = move;
        this.state = state;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.totalValue = 0.0;
        this.prior = 1.0;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}