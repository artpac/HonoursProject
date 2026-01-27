
package AI.Training;

import AI.*;

/**
 * Wrapper for AI agent with fitness tracking
 */
public class AIAgent {
    public HiveAI ai;
    public int id;
    public double fitness;
    public int wins;
    public int losses;
    public int draws;

    public AIAgent(HiveAI ai, int id) {
        this.ai = ai;
        this.id = id;
        this.fitness = 0.0;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }

    public AIAgent clone() {
        HiveAI clonedAI = new HiveAI(false);
        clonedAI.policyNetwork = this.ai.policyNetwork.clone();
        clonedAI.valueNetwork = this.ai.valueNetwork.clone();
        return new AIAgent(clonedAI, this.id);
    }

    public void mutate(double rate, double strength) {
        ai.policyNetwork.mutate(rate, strength);
        ai.valueNetwork.mutate(rate, strength);
    }
}