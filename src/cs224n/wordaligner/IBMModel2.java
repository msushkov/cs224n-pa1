package cs224n.wordaligner;

import java.util.List;
import java.util.Random;

import cs224n.util.Counters;
import cs224n.util.Pair;

public class IBMModel2 extends IBMModel {

    private Random rand = new Random();
    
    @Override
    public double q_jilm(int j, int i, int l, int m) {
        return q_jilm.getCount(convertIntsToStringKey(i, l, m), j);
    }

    @Override
    public void train(List<SentencePair> trainingData) {
        System.out.println("Starting train() for model 2...");
        
        initParams(trainingData);
        
        for (int p = 0; p < NUM_ITERS; p++) {
            System.out.println("Model 2 iteration " + p);
            
            clearCounts();

            // E-Step
            System.out.println("E-step...");
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

                        String ilm = convertIntsToStringKey(i, e.size(), f.size());
                        c_jilm.incrementCount(ilm, j, delta);
                        c_ilm.incrementCount(ilm, delta);
                    }
                }
            }

            // M-Step
            System.out.println("M-step...");
            for (String e : c_ef.keySet()) {
                for (String f : c_ef.getCounter(e).keySet()) {
                    t_fe.setCount(f, e, c_ef.getCount(e, f) / c_e.getCount(e));
                }
            }

            
            for (String ilm : c_jilm.keySet()) {
                for (Integer j : c_jilm.getCounter(ilm).keySet()) {
                    q_jilm.setCount(ilm, j, c_jilm.getCount(ilm, j) / c_ilm.getCount(ilm));
                }
            }
        }
        
        System.out.println("Finished train() for Model 2.");
    }
    
    /*
     * Run IBM Model 1 to initialize the parameters.
     */
    private void initParams(List<SentencePair> trainingData) {
        System.out.println("Starting initParams()...");
        
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
                    q_jilm.setCount(convertIntsToStringKey(i, l, m), j, getRand());
                }
            }
        }
        
        q_jilm = Counters.conditionalNormalize(q_jilm);
        
        System.out.println("Finished initParams().");
    }
    
    /*
     * Returns a random double between 0 and 0.1.
     */
    private double getRand() {
        return rand.nextDouble() / 10.0;
    }
}
