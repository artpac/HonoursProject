package AI;

import java.io.*;
import java.util.*;

/**
 * Deep Neural Network for policy and value estimation
 */
public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[] layerSizes;
    private double[][][] weights; // [layer][neuron][input]
    private double[][] biases;    // [layer][neuron]
    private double learningRate;

    public NeuralNetwork(boolean loadWeights) {
        // Architecture: 1220 -> 512 -> 256 -> 128 -> output
        this.layerSizes = new int[]{1220, 512, 256, 128, 64};
        this.learningRate = 0.001;

        if (loadWeights && weightsExist()) {
            loadFromFile();
        } else {
            initializeWeights();
        }
    }

    /**
     * Initialize weights with Xavier initialization
     */
    private void initializeWeights() {
        weights = new double[layerSizes.length - 1][][];
        biases = new double[layerSizes.length - 1][];
        Random rand = new Random(42);

        for (int l = 0; l < layerSizes.length - 1; l++) {
            int inputSize = layerSizes[l];
            int outputSize = layerSizes[l + 1];

            weights[l] = new double[outputSize][inputSize];
            biases[l] = new double[outputSize];

            // Xavier initialization
            double std = Math.sqrt(2.0 / inputSize);
            for (int i = 0; i < outputSize; i++) {
                for (int j = 0; j < inputSize; j++) {
                    weights[l][i][j] = rand.nextGaussian() * std;
                }
                biases[l][i] = 0.0;
            }
        }
    }

    /**
     * Forward pass through network
     */
    public double[] forward(double[] input) {
        double[] activation = input.clone();

        for (int l = 0; l < weights.length; l++) {
            double[] nextActivation = new double[layerSizes[l + 1]];

            for (int i = 0; i < nextActivation.length; i++) {
                double sum = biases[l][i];
                for (int j = 0; j < activation.length; j++) {
                    sum += weights[l][i][j] * activation[j];
                }

                // ReLU activation for hidden layers, sigmoid for output
                if (l < weights.length - 1) {
                    nextActivation[i] = relu(sum);
                } else {
                    nextActivation[i] = sigmoid(sum);
                }
            }

            activation = nextActivation;
        }

        return activation;
    }

    /**
     * Train network using gradient descent
     */
    public void train(double[] input, double[] targetOutput, double[] outputGradient) {
        // Forward pass to get activations
        List<double[]> activations = new ArrayList<>();
        double[] current = input.clone();
        activations.add(current);

        for (int l = 0; l < weights.length; l++) {
            double[] next = new double[layerSizes[l + 1]];
            for (int i = 0; i < next.length; i++) {
                double sum = biases[l][i];
                for (int j = 0; j < current.length; j++) {
                    sum += weights[l][i][j] * current[j];
                }
                next[i] = (l < weights.length - 1) ? relu(sum) : sigmoid(sum);
            }
            current = next;
            activations.add(current);
        }

        // Backward pass
        double[] delta = outputGradient.clone();

        for (int l = weights.length - 1; l >= 0; l--) {
            double[] prevActivation = activations.get(l);
            double[] currentActivation = activations.get(l + 1);

            // Update weights and biases
            for (int i = 0; i < weights[l].length; i++) {
                for (int j = 0; j < weights[l][i].length; j++) {
                    weights[l][i][j] -= learningRate * delta[i] * prevActivation[j];
                }
                biases[l][i] -= learningRate * delta[i];
            }

            // Compute delta for previous layer
            if (l > 0) {
                double[] newDelta = new double[prevActivation.length];
                for (int j = 0; j < prevActivation.length; j++) {
                    double sum = 0.0;
                    for (int i = 0; i < delta.length; i++) {
                        sum += delta[i] * weights[l][i][j];
                    }
                    newDelta[j] = sum * reluDerivative(prevActivation[j]);
                }
                delta = newDelta;
            }
        }
    }

    /**
     * Activation functions
     */
    private double relu(double x) {
        return Math.max(0, x);
    }

    private double reluDerivative(double x) {
        return x > 0 ? 1.0 : 0.0;
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double x) {
        double s = sigmoid(x);
        return s * (1 - s);
    }

    /**
     * Save weights to file
     */
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("models/hive_network.dat"))) {
            oos.writeObject(weights);
            oos.writeObject(biases);
            System.out.println("Network weights saved successfully");
        } catch (IOException e) {
            System.err.println("Error saving network: " + e.getMessage());
        }
    }

    /**
     * Load weights from file
     */
    private void loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("models/hive_network.dat"))) {
            weights = (double[][][]) ois.readObject();
            biases = (double[][]) ois.readObject();
            System.out.println("Network weights loaded successfully");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading network, using random weights: " + e.getMessage());
            initializeWeights();
        }
    }

    private boolean weightsExist() {
        return new File("models/hive_network.dat").exists();
    }

    /**
     * Clone this network (for evolutionary algorithms)
     */
    public NeuralNetwork clone() {
        NeuralNetwork clone = new NeuralNetwork(false);

        // Deep copy weights and biases
        for (int l = 0; l < weights.length; l++) {
            for (int i = 0; i < weights[l].length; i++) {
                clone.weights[l][i] = weights[l][i].clone();
            }
            clone.biases[l] = biases[l].clone();
        }

        return clone;
    }

    /**
     * Mutate weights (for evolutionary algorithm)
     */
    public void mutate(double mutationRate, double mutationStrength) {
        Random rand = new Random();

        for (int l = 0; l < weights.length; l++) {
            for (int i = 0; i < weights[l].length; i++) {
                for (int j = 0; j < weights[l][i].length; j++) {
                    if (rand.nextDouble() < mutationRate) {
                        weights[l][i][j] += rand.nextGaussian() * mutationStrength;
                    }
                }
            }

            for (int i = 0; i < biases[l].length; i++) {
                if (rand.nextDouble() < mutationRate) {
                    biases[l][i] += rand.nextGaussian() * mutationStrength;
                }
            }
        }
    }

    /**
     * Crossover with another network (for evolutionary algorithm)
     */
    public NeuralNetwork crossover(NeuralNetwork other) {
        NeuralNetwork child = new NeuralNetwork(false);
        Random rand = new Random();

        for (int l = 0; l < weights.length; l++) {
            for (int i = 0; i < weights[l].length; i++) {
                for (int j = 0; j < weights[l][i].length; j++) {
                    child.weights[l][i][j] = rand.nextBoolean() ?
                            this.weights[l][i][j] : other.weights[l][i][j];
                }
            }

            for (int i = 0; i < biases[l].length; i++) {
                child.biases[l][i] = rand.nextBoolean() ?
                        this.biases[l][i] : other.biases[l][i];
            }
        }

        return child;
    }
}