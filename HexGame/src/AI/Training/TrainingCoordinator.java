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
        System.out.println("Hive AI Training System\n");
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
        }
    }

    private static void runSelfPlayTraining(String startTime) {
        System.out.println("Starting self-play training...\n");

        HiveAI agent = new HiveAI(true);
        SelfPlayTrainer trainer = new SelfPlayTrainer(agent);

        trainer.train(1, true);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH.mm");
        String endTime = LocalDateTime.now().format(dateFormat);

        trainer.exportTrainingData("training_data " + startTime + "_" + endTime + ".csv");

        System.out.println("\nSelf-play training complete!");
    }

    private static void runEvolutionaryTraining(String startTime) {
        System.out.println("Starting evolutionary training...\n");

        EvolutionaryTrainer evolver = new EvolutionaryTrainer(5);

        evolver.evolve(5, 20);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("HH.mm");
        String endTime = LocalDateTime.now().format(dateFormat);
        evolver.exportStats("evolution_stats " + startTime + "_" + endTime + ".csv");

        System.out.println("\nEvolutionary training complete!");
    }

}
