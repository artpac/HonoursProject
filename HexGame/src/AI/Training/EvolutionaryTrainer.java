package AI.Training;

import AI.*;
import Game.*;
import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Evolutionary algorithm for training diverse AI agents
 */
public class EvolutionaryTrainer {
    private List<AIAgent> population;
    private int populationSize;
    private double mutationRate = 0.1;
    private double mutationStrength = 0.05;
    private int eliteCount = 5;

    public EvolutionaryTrainer(int populationSize) {
        this.populationSize = populationSize;
        this.population = new ArrayList<>();
        initializePopulation();
    }

    /**
     * Initialize random population
     */
    private void initializePopulation() {
        System.out.println("Initializing population of " + populationSize + " agents...");
        System.out.flush();
        for (int i = 0; i < populationSize; i++) {
            System.out.print("  Creating agent " + (i + 1) + "/" + populationSize + "...");
            System.out.flush();
            HiveAI ai = new HiveAI(false);
            AIAgent agent = new AIAgent(ai, i);
            population.add(agent);
            System.out.println(" done");
            System.out.flush();
        }
        System.out.println("✓ Population initialized\n");
        System.out.flush();
    }

    /**
     * Run evolutionary training
     */
    public void evolve(int generations, int gamesPerEval) {
        System.out.println("Starting evolutionary training for " + generations + " generations...");

        for (int gen = 0; gen < generations; gen++) {
            System.out.println("\n=== Generation " + (gen + 1) + " ===");

            // Evaluate fitness
            evaluateFitness(gamesPerEval);

            // Sort by fitness
            population.sort((a, b) -> Double.compare(b.fitness, a.fitness));

            // Print statistics
            printGenerationStats(gen);

            // Create next generation
            List<AIAgent> nextGen = new ArrayList<>();

            // Elitism: keep best agents
            for (int i = 0; i < eliteCount && i < population.size(); i++) {
                nextGen.add(population.get(i).clone());
            }

            // Breed rest of population
            while (nextGen.size() < populationSize) {
                AIAgent parent1 = selectParent();
                AIAgent parent2 = selectParent();
                AIAgent child = crossover(parent1, parent2);
                child.mutate(mutationRate, mutationStrength);
                nextGen.add(child);
            }

            population = nextGen;

            // Save best agent
            if ((gen + 1) % 10 == 0) {
                saveBestAgent(gen + 1);
            }
        }

        System.out.println("\nEvolution complete!");
        saveBestAgent(generations);
    }

    /**
     * Evaluate fitness by playing tournament
     */
    private void evaluateFitness(int gamesPerAgent) {
        System.out.println("Evaluating fitness (" + gamesPerAgent + " games per agent)...");
        System.out.flush();

        // Reset fitness
        for (AIAgent agent : population) {
            agent.fitness = 0.0;
            agent.wins = 0;
            agent.losses = 0;
            agent.draws = 0;
        }

        // Round-robin tournament
        Random rand = new Random();
        int totalGames = population.size() * gamesPerAgent;
        int gamesPlayed = 0;

        for (int agentIdx = 0; agentIdx < population.size(); agentIdx++) {
            AIAgent agent = population.get(agentIdx);
            System.out.println("  Agent " + (agentIdx + 1) + "/" + population.size());
            System.out.flush();

            for (int i = 0; i < gamesPerAgent; i++) {
                gamesPlayed++;
                System.out.print("    Game " + (i + 1) + "/" + gamesPerAgent + "...");
                System.out.flush();

                // Select random opponent
                AIAgent opponent = population.get(rand.nextInt(population.size()));
                if (opponent == agent && population.size() > 1) {
                    // Pick different opponent
                    opponent = population.get((agentIdx + 1) % population.size());
                }

                // Play game
                GameResult result = playGame(agent, opponent);

                // Update fitness
                updateFitness(agent, result, true);
                updateFitness(opponent, result, false);

                System.out.println(" " + result);
                System.out.flush();
            }
        }

        // Normalize fitness
        double maxFitness = population.stream()
                .mapToDouble(a -> a.fitness)
                .max()
                .orElse(1.0);

        if (maxFitness > 0) {
            for (AIAgent agent : population) {
                agent.fitness /= maxFitness;
            }
        }
    }

    /**
     * Play game between two agents
     */
    private GameResult playGame(AIAgent white, AIAgent black) {
        GameState state = new GameState();
        int maxMoves = 100;

        for (int turn = 0; turn < maxMoves; turn++) {
            Color currentPlayer = state.getCurrentPlayer();
            AIAgent currentAgent = currentPlayer.equals(Color.WHITE) ? white : black;

            AIMove move = null;
            try {
                move = currentAgent.ai.getBestMove(state, currentPlayer);
            } catch (Exception e) {
                System.err.println("\nERROR getting move: " + e.getMessage());
                e.printStackTrace();
                return GameResult.DRAW; // Abort game
            }

            if (move == null) break;

            try {
                move.execute(state);
            } catch (Exception e) {
                System.err.println("\nERROR executing move: " + e.getMessage());
                e.printStackTrace();
                return GameResult.DRAW; // Abort game
            }

            String winMessage = checkWinCondition(state);

            if (winMessage != null) {
                if (winMessage.contains("Draw")) return GameResult.DRAW;
                if (winMessage.contains("White wins")) return GameResult.WHITE_WIN;
                if (winMessage.contains("Black wins")) return GameResult.BLACK_WIN;
            }

            state.nextPlayer();
        }

        return GameResult.DRAW; // Timeout
    }

    /**
     * Update agent fitness based on game result
     */
    private void updateFitness(AIAgent agent, GameResult result, boolean wasWhite) {
        if (result == GameResult.DRAW) {
            agent.fitness += 0.5;
            agent.draws++;
        } else {
            boolean won = (wasWhite && result == GameResult.WHITE_WIN) ||
                    (!wasWhite && result == GameResult.BLACK_WIN);
            if (won) {
                agent.fitness += 1.0;
                agent.wins++;
            } else {
                agent.losses++;
            }
        }
    }

    /**
     * Select parent using tournament selection
     */
    private AIAgent selectParent() {
        int tournamentSize = 5;
        Random rand = new Random();

        AIAgent best = null;
        double bestFitness = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < tournamentSize; i++) {
            AIAgent candidate = population.get(rand.nextInt(population.size()));
            if (candidate.fitness > bestFitness) {
                bestFitness = candidate.fitness;
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Crossover two agents to create offspring
     */
    private AIAgent crossover(AIAgent parent1, AIAgent parent2) {
        NeuralNetwork childPolicyNet = parent1.ai.policyNetwork.crossover(parent2.ai.policyNetwork);
        NeuralNetwork childValueNet = parent1.ai.valueNetwork.crossover(parent2.ai.valueNetwork);

        HiveAI childAI = new HiveAI(false);
        childAI.policyNetwork = childPolicyNet;
        childAI.valueNetwork = childValueNet;

        return new AIAgent(childAI, -1);
    }

    /**
     * Print generation statistics
     */
    private void printGenerationStats(int gen) {
        AIAgent best = population.get(0);
        double avgFitness = population.stream()
                .mapToDouble(a -> a.fitness)
                .average()
                .orElse(0.0);

        System.out.printf("Best fitness: %.3f (W:%d L:%d D:%d)\n",
                best.fitness, best.wins, best.losses, best.draws);
        System.out.printf("Average fitness: %.3f\n", avgFitness);
        System.out.printf("Diversity score: %.3f\n", calculateDiversity());
    }

    /**
     * Calculate population diversity (fitness variance)
     */
    private double calculateDiversity() {
        double mean = population.stream()
                .mapToDouble(a -> a.fitness)
                .average()
                .orElse(0.0);

        double variance = population.stream()
                .mapToDouble(a -> Math.pow(a.fitness - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    /**
     * Save best agent to file
     */
    private void saveBestAgent(int generation) {
        AIAgent best = population.get(0);
        best.ai.policyNetwork.saveToFile();
        best.ai.valueNetwork.saveToFile();
        System.out.println("Best agent saved at generation " + generation);
    }

    /**
     * Get best agent from population
     */
    public HiveAI getBestAgent() {
        return population.get(0).ai;
    }

    /**
     * Export population statistics
     */
    public void exportStats(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("AgentID,Fitness,Wins,Losses,Draws");
            for (AIAgent agent : population) {
                writer.printf("%d,%.3f,%d,%d,%d\n",
                        agent.id, agent.fitness, agent.wins, agent.losses, agent.draws);
            }
            System.out.println("Population stats exported to " + filename);
        } catch (java.io.IOException e) {
            System.err.println("Error exporting stats: " + e.getMessage());
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
