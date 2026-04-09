package AI;

import Game.*;
import java.awt.Color;
import java.util.*;


public class MCTSEngine {
    private NeuralNetwork policyNet;
    private NeuralNetwork valueNet;
    private double explorationConstant = 1.41;

    public MCTSEngine(NeuralNetwork policy, NeuralNetwork value) {
        this.policyNet = policy;
        this.valueNet = value;
    }


    public AIMove search(GameState rootState, Color aiColor, int iterations) {
        MCTSNode root = new MCTSNode(null, null, rootState.clone());

        // Safety: limit iterations timeout
        int maxIterations = Math.min(iterations, 50);
        long startTime = System.currentTimeMillis();
        long timeoutMs = 2000;

        for (int i = 0; i < maxIterations; i++) {
            if (i % 10 == 0 && System.currentTimeMillis() - startTime > timeoutMs) {
                break;
            }

            MCTSNode node = root;
            GameState state = rootState.clone();


            int selectionDepth = 0;
            while (!node.isLeaf() && !isTerminal(state) && selectionDepth < 100) {
                node = selectChild(node);
                if (node == null || node.move == null) break;
                node.move.execute(state);
                selectionDepth++;
            }

            if(node == null) continue;
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


    private MCTSNode selectChild(MCTSNode node) {
        if (node.children.isEmpty()) return null;

        MCTSNode best = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (MCTSNode child : node.children) {
            double exploitation = child.totalValue / (child.visits + 1e-8);
            double exploration = explorationConstant *
                    Math.sqrt(Math.log(node.visits + 1) / (child.visits + 1e-8));
            double uctValue = exploitation + exploration;

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


    private void expand(MCTSNode node, GameState state) {
        Color currentPlayer = state.getCurrentPlayer();
        List<AIMove> moves = generateMoves(state, currentPlayer);

        int maxChildren = 50;
        moves = moves.size() > maxChildren ? moves.subList(0, maxChildren) : moves;

        double[] priors = null;
        if (policyNet != null) {
            try {
                double[] stateVec = encodeState(state, currentPlayer);
                priors = policyNet.forward(stateVec);
            } catch (Exception e) {
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


    private double evaluate(GameState state, Color aiColor) {
        String winResult = checkWinCondition(state);

        if (winResult != null) {
            if (winResult.contains("Draw")) return 0.5;
            boolean aiWon = (aiColor.equals(Color.WHITE) && winResult.contains("White wins")) ||
                    (aiColor.equals(Color.BLACK) && winResult.contains("Black wins"));
            return aiWon ? 1.0 : 0.0;
        }

        if (valueNet != null) {
            double[] stateVec = encodeState(state, aiColor);
            double[] output = valueNet.forward(stateVec);
            return output[0];
        }

        return heuristicEval(state, aiColor);
    }


    private double heuristicEval(GameState state, Color aiColor) {
        HiveBoard board = state.getBoard();
        double score = 0.5;

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
            // Penalize surrounded queen
            score -= aiQueenNeighbors * 0.08;
        }

        if (oppQueenPos != null) {
            int oppQueenNeighbors = countNeighbors(board, oppQueenPos);
            // Reward surrounding opponent queen
            score += oppQueenNeighbors * 0.08;
        }

        // Piece mobility
        int aiMobility = countMobility(state, aiColor);
        int oppMobility = countMobility(state, getOpponent(aiColor));
        score += (aiMobility - oppMobility) * 0.01;

        // Control of center
        score += evaluatePosition(board, aiColor) * 0.05;

        // Penalty for long games
        int turnCount = state.getTurnCount();
        if (turnCount > 60) {
            score -= (turnCount - 60) * 0.01;
        }

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
                    double distance = Math.sqrt(coord.getQ() * coord.getQ() + coord.getR() * coord.getR());
                    score += 1.0 / (1.0 + distance);
                }
            }
        }
        return score;
    }


    private void backpropagate(MCTSNode node, double value) {
        while (node != null) {
            node.visits++;
            node.totalValue += value;
            node = node.parent;
            value = 1.0 - value;
        }
    }

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

        return null;
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