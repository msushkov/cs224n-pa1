package cs224n.wordaligner;

import java.util.List;
import java.util.Random;

import cs224n.util.Counters;
import cs224n.util.Pair;

public class IBMModel2 extends IBMModel {

    @Override
    public double q_jilm(int j, int i, int l, int m) {
        Pair<Pair<Integer, Integer>, Integer> ilm = new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(i, l), m);
        return q_jilm.getCount(ilm, j);
    }

    @Override
    public void train(List<SentencePair> trainingData) {
        initParams(trainingData);
        for (int p = 0; p < NUM_ITERS; p++) {
            clearCounts();

            // E-Step
            for (int k = 0; k < trainingData.size(); k++) {
                List<String> f = trainingData.get(k).getSourceWords();
                f.add(NULL_WORD);
                List<String> e = trainingData.get(k).getTargetWords();
                for (int i = 0; i < f.size(); i++) {
                    // compute the normalization factor for delta
                    double deltaNormalization = 0.0;
                    for (int j = 0; j < e.size(); j++) {
                        deltaNormalization += q_jilm(j, i, e.size(), f.size()) * t_fe.getCount(f.get(i), e.get(j));
                    }

                    for (int j = 0; j < e.size(); j++) {
                        double delta = q_jilm(j, i, e.size(), f.size()) * t_fe.getCount(f.get(i), e.get(j)) / deltaNormalization; 

                        c_ef.incrementCount(e.get(j), f.get(i), delta);
                        c_e.incrementCount(e.get(j), delta);

                        Pair<Integer, Integer> il = new Pair<Integer, Integer>(i, e.size());
                        Pair<Pair<Integer, Integer>, Integer> ilm = new Pair<Pair<Integer, Integer>, Integer>(il, f.size());

                        c_jilm.incrementCount(ilm, j, delta);
                        c_ilm.incrementCount(ilm, delta);
                    }
                }
            }

            // M-Step
            for (String e : c_ef.keySet()) {
                for (String f : c_ef.getCounter(e).keySet()) {
                    t_fe.setCount(f, e, c_ef.getCount(e, f) / c_e.getCount(e));
                }
            }

            
            for (Pair<Pair<Integer, Integer>, Integer> ilm : c_jilm.keySet()) {
                for (Integer j : c_jilm.getCounter(ilm).keySet()) {
                    q_jilm.setCount(ilm, j, c_jilm.getCount(ilm, j) / c_ilm.getCount(ilm));
                }
            }
        }
    }
    
    /*
     * Run IBM Model 1 to initialize the parameters.
     */
    private void initParams(List<SentencePair> trainingData) {
        // initialize t_fe to the output of model 1
        IBMModel1 model1 = new IBMModel1();
        model1.train(trainingData);
        t_fe = model1.t_fe;
        
        // initialize q_jilm randomly, then normalize
        for (int k = 0; k < trainingData.size(); k++) {
            List<String> f = trainingData.get(k).getSourceWords();
            f.add(NULL_WORD);
            List<String> e = trainingData.get(k).getTargetWords();
            
            int l = e.size();
            int m = f.size();
            
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < l; j++) {
                    Pair<Pair<Integer, Integer>, Integer> ilm = new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(i, l), m);
                    q_jilm.setCount(ilm, j, getRand());
                }
            }
        }
        
        q_jilm = Counters.conditionalNormalize(q_jilm);
    }
    
    /*
     * Returns a random double between 0 and 0.1.
     */
    private double getRand() {
        return (new Random()).nextDouble() / 10.0;
    }
}