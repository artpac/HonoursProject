package AI.Training;

import AI.*;
import Game.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main coordinator for all AI training methods
 */
public class TrainingCoordinator {

    public static void main(String[] args) {
        System.out.println("=== Hive AI Training System ===\n");
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH.mm");
        String startTime = LocalDateTime.now().format(dateFormat);
        System.out.println(startTime);

        String mode = args.length > 0 ? args[0] : "selfplay";

        switch (mode.toLowerCase()) {
            case "selfplay":
                runSelfPlayTraining(startTime);
                break;
            case "evolution":
                runEvolutionaryTraining(startTime);
                break;
            case "combined":
                runCombinedTraining();
                break;
            case "test":
                testAI();
                break;
            default:
                System.out.println("Usage: java TrainingCoordinator [selfplay|evolution|combined|test]");
        }
    }

    /**
     * Self-play reinforcement learning
     */
    private static void runSelfPlayTraining(String startTime) {
        System.out.println("Starting self-play training...\n");

        HiveAI agent = new HiveAI(true);
        SelfPlayTrainer trainer = new SelfPlayTrainer(agent);

        // Set number of games
        trainer.train(10, true);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH.mm");
        String endTime = LocalDateTime.now().format(dateFormat);

        // Export training data
        trainer.exportTrainingData("training_data " + startTime + "_" + endTime + ".csv");

        System.out.println("\nSelf-play training complete!");
    }

    /**
     * Evolutionary algorithm training
     */
    private static void runEvolutionaryTraining(String startTime) {
        System.out.println("Starting evolutionary training...\n");

        EvolutionaryTrainer evolver = new EvolutionaryTrainer(5); // Population of 50

        // Evolve for 100 generations
        evolver.evolve(5, 20); // 5 games per evaluation

        // Export population statistics
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH.mm");
        String endTime = LocalDateTime.now().format(dateFormat);
        evolver.exportStats("evolution_stats " + startTime + "_" + endTime + ".csv");

        System.out.println("\nEvolutionary training complete!");
    }

    /**
     * Combined training approach
     */
    private static void runCombinedTraining() {
        System.out.println("Starting combined training...\n");

        // Phase 1: Evolutionary training for diversity
        System.out.println("Phase 1: Evolutionary initialization (50 generations)");
        EvolutionaryTrainer evolver = new EvolutionaryTrainer(30);
        evolver.evolve(50, 3);

        // Get best evolved agent
        HiveAI bestAgent = evolver.getBestAgent();

        // Phase 2: Refine with self-play
        System.out.println("\nPhase 2: Self-play refinement (500 games)");
        SelfPlayTrainer refiner = new SelfPlayTrainer(bestAgent);
        refiner.train(500, true);

        System.out.println("\nCombined training complete!");
        System.out.println("Best agent saved to models/hive_network.dat");
    }

    /**
     * Test trained AI
     */
    private static void testAI() {
        System.out.println("Testing trained AI...\n");

        HiveAI agent = new HiveAI(true); // Load trained weights
        GameState testState = new GameState();

        // Play a few test moves
        for (int i = 0; i < 10; i++) {
            AIMove move = agent.getBestMove(testState, testState.getCurrentPlayer());
            if (move == null) {
                System.out.println("No legal moves available");
                break;
            }

            System.out.printf("Turn %d: %s %s at (%d,%d)\n",
                    i + 1,
                    move.getPiece().getColor().equals(java.awt.Color.WHITE) ? "White" : "Black",
                    move.getPiece().getType().name(),
                    move.getTo().getQ(),
                    move.getTo().getR()
            );

            move.execute(testState);
            testState.nextPlayer();
        }

        System.out.println("\nTest complete!");
    }
}
