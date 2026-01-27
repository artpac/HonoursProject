package AI.Training;

import AI.*;
import Game.*;
import java.awt.Color;
import java.util.*;
import java.io.*;

/**
 * Self-play training system for reinforcement learning
 */
public class SelfPlayTrainer {
    private HiveAI agent;
    private List<GameExperience> replayBuffer;
    private int maxBufferSize = 10000;
    private int batchSize = 64;
    private double discountFactor = 0.99;

    public SelfPlayTrainer(HiveAI agent) {
        this.agent = agent;
        this.replayBuffer = new ArrayList<>();
    }

    /**
     * Run self-play training loop
     */
    public void train(int numGames, boolean verbose) {
        System.out.println("Starting self-play training for " + numGames + " games...");

        int whiteWins = 0;
        int blackWins = 0;
        int draws = 0;

        for (int game = 0; game < numGames; game++) {
            List<GameExperience> gameHistory = playSelfPlayGame();

            // Determine winner and assign rewards
            GameResult result = gameHistory.get(gameHistory.size() - 1).result;
            assignRewards(gameHistory, result);

            // Add to replay buffer
            replayBuffer.addAll(gameHistory);
            if (replayBuffer.size() > maxBufferSize) {
                replayBuffer = replayBuffer.subList(
                        replayBuffer.size() - maxBufferSize, replayBuffer.size());
            }

            // Train on batch
            if (replayBuffer.size() >= batchSize) {
                trainOnBatch();
            }

            // Track statistics
            if (result == GameResult.WHITE_WIN) whiteWins++;
            else if (result == GameResult.BLACK_WIN) blackWins++;
            else draws++;

            if (verbose && (game + 1) % 10 == 0) {
                System.out.printf("Game %d/%d - W:%d B:%d D:%d - Buffer:%d\n",
                        game + 1, numGames, whiteWins, blackWins, draws, replayBuffer.size());
            }

            // Save checkpoint every 100 games
            if ((game + 1) % 100 == 0) {
                saveCheckpoint(game + 1);
            }
        }

        System.out.println("Training complete!");
        System.out.printf("Final stats - White wins: %d, Black wins: %d, Draws: %d\n",
                whiteWins, blackWins, draws);
    }

    /**
     * Play one self-play game
     */
    private List<GameExperience> playSelfPlayGame() {
        GameState state = new GameState();
        List<GameExperience> history = new ArrayList<>();
        int maxMoves = 100; // Prevent infinite games

        for (int turn = 0; turn < maxMoves; turn++) {
            Color currentPlayer = state.getCurrentPlayer();

            // Get AI move
            AIMove move = agent.getBestMove(state, currentPlayer);
            if (move == null) break; // No legal moves

            // Record experience
            double[] stateBefore = agent.encodeGameState(state, currentPlayer);
            move.execute(state);
            double[] stateAfter = agent.encodeGameState(state, currentPlayer);

            GameExperience exp = new GameExperience(
                    stateBefore, move, stateAfter, 0.0, GameResult.ONGOING);
            history.add(exp);

            // Check for terminal state
            String winMessage = checkWinCondition(state);

            if (winMessage != null) {
                GameResult result;
                if (winMessage.contains("Draw")) {
                    result = GameResult.DRAW;
                } else if (winMessage.contains("White wins")) {
                    result = GameResult.WHITE_WIN;
                } else {
                    result = GameResult.BLACK_WIN;
                }

                history.get(history.size() - 1).result = result;
                break;
            }

            state.nextPlayer();
        }

        return history;
    }

    /**
     * Assign rewards based on game outcome
     */
    private void assignRewards(List<GameExperience> history, GameResult result) {
        double finalReward;
        if (result == GameResult.DRAW) {
            finalReward = 0.5;
        } else {
            finalReward = 1.0; // Win for that player
        }

        // Propagate rewards backward with discount
        for (int i = history.size() - 1; i >= 0; i--) {
            GameExperience exp = history.get(i);

            // Alternate rewards (opponent gets negative reward)
            boolean isWinner = (result == GameResult.WHITE_WIN && i % 2 == 0) ||
                    (result == GameResult.BLACK_WIN && i % 2 == 1);

            if (result == GameResult.DRAW) {
                exp.reward = 0.5;
            } else {
                exp.reward = isWinner ? finalReward : (1.0 - finalReward);
            }

            // Apply temporal discount
            finalReward *= discountFactor;
        }
    }

    /**
     * Train neural networks on a batch of experiences
     */
    private void trainOnBatch() {
        Random rand = new Random();

        for (int i = 0; i < batchSize; i++) {
            // Sample random experience
            GameExperience exp = replayBuffer.get(rand.nextInt(replayBuffer.size()));

            // Compute TD target
            double[] currentQ = agent.policyNetwork.forward(exp.stateBefore);
            double[] nextQ = agent.policyNetwork.forward(exp.stateAfter);

            double maxNextQ = 0.0;
            for (double q : nextQ) {
                maxNextQ = Math.max(maxNextQ, q);
            }

            double target = exp.reward + discountFactor * maxNextQ;

            // Create target vector (only update for taken action)
            double[] targetVector = currentQ.clone();
            int actionIdx = getMoveIndex(exp.move);
            if (actionIdx < targetVector.length) {
                targetVector[actionIdx] = target;
            }

            // Compute gradient
            double[] gradient = new double[currentQ.length];
            for (int j = 0; j < gradient.length; j++) {
                gradient[j] = currentQ[j] - targetVector[j];
            }

            // Train policy network
            agent.policyNetwork.train(exp.stateBefore, targetVector, gradient);

            // Train value network (separate target)
            double[] valueTarget = {exp.reward};
            double[] valueOutput = agent.valueNetwork.forward(exp.stateBefore);
            double[] valueGradient = {valueOutput[0] - exp.reward};
            agent.valueNetwork.train(exp.stateBefore, valueTarget, valueGradient);
        }
    }

    /**
     * Save training checkpoint
     */
    private void saveCheckpoint(int gameNumber) {
        agent.policyNetwork.saveToFile();
        agent.valueNetwork.saveToFile();

        try (PrintWriter writer = new PrintWriter(new FileWriter("models/training_log.txt", true))) {
            writer.printf("Checkpoint at game %d - Buffer size: %d\n",
                    gameNumber, replayBuffer.size());
        } catch (IOException e) {
            System.err.println("Error saving checkpoint log: " + e.getMessage());
        }

        System.out.println("Checkpoint saved at game " + gameNumber);
    }

    /**
     * Get index for move (simplified mapping)
     */
    private int getMoveIndex(AIMove move) {
        // Simple hash-based indexing
        return Math.abs(move.hashCode()) % 64;
    }

    /**
     * Export training data for analysis
     */
    public void exportTrainingData(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Move,Reward,Result");
            for (GameExperience exp : replayBuffer) {
                writer.printf("%s,%.3f,%s\n",
                        exp.move.toString(), exp.reward, exp.result);
            }
            System.out.println("Training data exported to " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }

    /**
     * Check win condition without needing a save file
     */
    private String checkWinCondition(GameState state) {
        HiveBoard board = state.getBoard();
        boolean whiteQueenSurrounded = false;
        boolean blackQueenSurrounded = false;

        for (Map.Entry<HexCoord, List<Piece>> entry : board.getBoard().entrySet()) {
            Piece piece = entry.getValue().get(0);
            if (piece.getType() == PieceType.QUEEN) {
                int neighbors = 0;
                for (HexCoord neighbor : entry.getKey().getNeighbors()) {
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

        if (whiteQueenSurrounded && blackQueenSurrounded) {
            return "Draw! Both Queens surrounded!";
        } else if (whiteQueenSurrounded) {
            return "Black wins! White Queen surrounded!";
        } else if (blackQueenSurrounded) {
            return "White wins! Black Queen surrounded!";
        }

        return null;
    }
}

/**
 * Stores one experience from self-play
 */
class GameExperience {
    double[] stateBefore;
    AIMove move;
    double[] stateAfter;
    double reward;
    GameResult result;

    public GameExperience(double[] stateBefore, AIMove move,
                          double[] stateAfter, double reward, GameResult result) {
        this.stateBefore = stateBefore;
        this.move = move;
        this.stateAfter = stateAfter;
        this.reward = reward;
        this.result = result;
    }
}