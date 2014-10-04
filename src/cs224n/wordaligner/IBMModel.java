package cs224n.wordaligner;

import java.util.List;

import cs224n.util.Counter;
import cs224n.util.CounterMap;
import cs224n.util.Pair;

/*
 * Superclass for IBM Models 1 and 2.
 */
public abstract class IBMModel implements WordAligner {

    // Convergence threshold
    double EPSILON = 0.0001;
    
    // Parameters
    CounterMap<String, String> t_fe = new CounterMap<String, String>();
    CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>> q_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
    
    // Counters
    CounterMap<String, String> C_ef = new CounterMap<String, String>();
    Counter<String> C_e = new Counter<String>();
    
    // C(j, i, l, m)
    CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>> C_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
    Counter<Pair<Pair<Integer, Integer>, Integer>> C_ilm = new Counter<Pair<Pair<Integer, Integer>, Integer>>(); 
    
    /*
     * Model 1: set randomly; model 2: use the ones from model 1.
     * Model 1: make sure to normalize!
     */
    public abstract void initParams();
    
    @Override
    public Alignment align(SentencePair sentencePair) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void train(List<SentencePair> trainingData) {
        // TODO Auto-generated method stub

    }
    
    /*
     * The delta for IBM Models 1 and 2. To be overridden.
     */
    public abstract double delta_kij(int k, int i, int j);
    
    /*
     * Compute the value of the t(f|e) parameter on the fly. Don't need to store.
     */
    public double t_fe(String source, String target) {
        // TODO
        return 0.0;
    }
    
    /*
     * Compute the value of the q(j|i, l, m) parameter on the fly.
     */
    public double q_jilm(int j, int i, int l, int m) {
        // TODO
        return 0.0;
    }
    
    //
    // PRIVATE METHODS
    //
    
    /*
     * Set all Counter entries to 0.
     */
    private void clearCounts() {
        // TODO
    }
    
    /*
     * Checks for convergence of EM. Use EPSILON.
     */
    private boolean isDone() {
        // TODO
        return false;
    }

}
