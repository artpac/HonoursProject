package AI.Training;

import AI.*;
import Game.*;
import java.awt.Color;
import java.util.*;
import java.io.*;


public class SelfPlayTrainer {
    private HiveAI agent;
    private List<GameExperience> replayBuffer;
    private int maxBufferSize = 10000;
    private int batchSize = 64;
    private double discountFactor = 0.99;
    public String finalStats;

    public SelfPlayTrainer(HiveAI agent) {
        this.agent = agent;
        this.replayBuffer = new ArrayList<>();
    }


    public void train(int numGames, boolean verbose) {
        System.out.println("Starting self-play training for " + numGames + " games...");

        int whiteWins = 0;
        int blackWins = 0;
        int draws = 0;

        for (int game = 0; game < numGames; game++) {
            List<GameExperience> gameHistory = playSelfPlayGame();

            GameResult result = gameHistory.get(gameHistory.size() - 1).result;
            assignRewards(gameHistory, result);

            replayBuffer.addAll(gameHistory);
            if (replayBuffer.size() > maxBufferSize) {
                replayBuffer = replayBuffer.subList(
                        replayBuffer.size() - maxBufferSize, replayBuffer.size());
            }

            if (replayBuffer.size() >= batchSize) {
                trainOnBatch();
            }

            if (result == GameResult.WHITE_WIN) whiteWins++;
            else if (result == GameResult.BLACK_WIN) blackWins++;
            else draws++;

            if (verbose && (game + 1) % 10 == 0) {
                System.out.printf("Game %d/%d - W:%d B:%d D:%d - Buffer:%d\n",
                        game + 1, numGames, whiteWins, blackWins, draws, replayBuffer.size());
            }

            if ((game + 1) % 100 == 0) {
                saveCheckpoint(game + 1);
            }
        }

        System.out.println("Training complete");
        finalStats = "Final Stats - White wins: " + whiteWins + " Black wins: " + blackWins + " Draws: " + draws + "\n";
        System.out.println(finalStats);
    }


    private List<GameExperience> playSelfPlayGame() {
        GameState state = new GameState();
        List<GameExperience> history = new ArrayList<>();
        int maxMoves = 100;

        for (int turn = 0; turn < maxMoves; turn++) {
            Color currentPlayer = state.getCurrentPlayer();

            AIMove move = agent.getBestMove(state, currentPlayer);
            //No legal moves
            if (move == null) break;


            double[] stateBefore = agent.encodeGameState(state, currentPlayer);
            move.execute(state);
            double[] stateAfter = agent.encodeGameState(state, currentPlayer);
            double posReward = computePositionalReward(state, currentPlayer);

            GameExperience exp = new GameExperience(
                    stateBefore, move, stateAfter, 0.0, posReward, GameResult.ONGOING);
            history.add(exp);

            if (state.isThreefoldRepetition()) {
                history.get(history.size() - 1).result = GameResult.DRAW;
                break;
            }

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


    //Rewards Assigned on game outcome
    private void assignRewards(List<GameExperience> history, GameResult result) {
        double finalReward = (result == GameResult.DRAW) ? 0.5 : 1.0;

        for (int i = history.size() - 1; i >= 0; i--) {
            GameExperience exp = history.get(i);

            double outcomeReward;
            if (result == GameResult.DRAW) {
                outcomeReward = 0.5;
            } else {
                boolean isWinner = (result == GameResult.WHITE_WIN && i % 2 == 0) ||
                        (result == GameResult.BLACK_WIN && i % 2 == 1);
                outcomeReward = isWinner ? finalReward : (1.0 - finalReward);
            }

            // Blend: 70% outcome signal, 30% live positional heuristic
            exp.reward = 0.7 * outcomeReward + 0.3 * exp.positionalReward;

            finalReward *= discountFactor;
        }

        // Extra penalty for repetition-draws: halve all rewards
        if (result == GameResult.DRAW && history.size() >= 95) {
            for (GameExperience exp : history) {
                exp.reward *= 0.5;
            }
        }
    }


    private void trainOnBatch() {
        Random rand = new Random();

        for (int i = 0; i < batchSize; i++) {
            GameExperience exp = replayBuffer.get(rand.nextInt(replayBuffer.size()));

            double[] currentQ = agent.policyNetwork.forward(exp.stateBefore);
            double[] nextQ = agent.policyNetwork.forward(exp.stateAfter);

            double maxNextQ = 0.0;
            for (double q : nextQ) {
                maxNextQ = Math.max(maxNextQ, q);
            }

            double target = exp.reward + discountFactor * maxNextQ;

            double[] targetVector = currentQ.clone();
            int actionIdx = getMoveIndex(exp.move);
            if (actionIdx < targetVector.length) {
                targetVector[actionIdx] = target;
            }

            double[] gradient = new double[currentQ.length];
            for (int j = 0; j < gradient.length; j++) {
                gradient[j] = currentQ[j] - targetVector[j];
            }

            agent.policyNetwork.train(exp.stateBefore, targetVector, gradient);

            double[] valueTarget = {exp.reward};
            double[] valueOutput = agent.valueNetwork.forward(exp.stateBefore);
            double[] valueGradient = {valueOutput[0] - exp.reward};
            agent.valueNetwork.train(exp.stateBefore, valueTarget, valueGradient);
        }
    }


    private void saveCheckpoint(int gameNumber) {
        System.out.println("\nSaving Checkpoint");
        System.out.println("After game: " + gameNumber);
        System.out.println("Buffer size: " + replayBuffer.size());

        agent.policyNetwork.saveToFile();
        agent.valueNetwork.saveToFile();

        try (PrintWriter writer = new PrintWriter(new FileWriter("models/training_log.txt", true))) {
            writer.printf("Checkpoint at game %d - Buffer size: %d\n",
                    gameNumber, replayBuffer.size());
        } catch (IOException e) {
            System.err.println("Error saving checkpoint log: " + e.getMessage());
        }

        System.out.println("Checkpoint saved to models/");
    }

    private int getMoveIndex(AIMove move) {
        return Math.abs(move.hashCode()) % 64;
    }

    public void exportTrainingData(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Move,Reward,Result");
            for (GameExperience exp : replayBuffer) {
                writer.printf("%s,%.3f,%s\n",
                        exp.move.toString(), exp.reward, exp.result);
            }
            writer.println(finalStats);
            System.out.println("Training data exported to " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting data: " + e.getMessage());
        }
    }


    //Reward based on queen pressure
    private double computePositionalReward(GameState state, Color player) {
        HiveBoard board = state.getBoard();
        Color opponent = player.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;

        int ownQueenNeighbors = 0;
        int oppQueenNeighbors = 0;
        boolean ownQueenPlaced = false;
        boolean oppQueenPlaced = false;

        for (Map.Entry<HexCoord, List<Piece>> entry : board.getBoard().entrySet()) {
            Piece piece = entry.getValue().get(0);
            if (piece.getType() == PieceType.QUEEN) {
                int neighbors = 0;
                for (HexCoord neighbor : entry.getKey().getNeighbors()) {
                    if (board.containsCoord(neighbor)) neighbors++;
                }
                if (piece.getColor().equals(player)) {
                    ownQueenPlaced = true;
                    ownQueenNeighbors = neighbors;
                } else {
                    oppQueenPlaced = true;
                    oppQueenNeighbors = neighbors;
                }
            }
        }

        double score = 0.5;
        if (oppQueenPlaced) score += (oppQueenNeighbors / 6.0) * 0.35; // reward surrounding enemy queen
        if (ownQueenPlaced) score -= (ownQueenNeighbors / 6.0) * 0.35; // penalise own queen being surrounded
        return Math.max(0.0, Math.min(1.0, score));
    }

    //Check win condition without needing a save file
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


class GameExperience {
    double[] stateBefore;
    AIMove move;
    double[] stateAfter;
    double reward;
    double positionalReward;
    GameResult result;

    public GameExperience(double[] stateBefore, AIMove move,
                          double[] stateAfter, double reward,
                          double positionalReward, GameResult result) {
        this.stateBefore = stateBefore;
        this.move = move;
        this.stateAfter = stateAfter;
        this.reward = reward;
        this.positionalReward = positionalReward;
        this.result = result;
    }
}
