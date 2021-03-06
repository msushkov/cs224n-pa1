package cs224n.wordaligner;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import cs224n.util.CounterMap;

public class IBMModel1 extends IBMModel {

    @Override
    public double q_jilm(int j, int i, int l, int m) {
        return 1.0 / (l + 1.0);
    }
    
    @Override
    public void train(List<SentencePair> trainingData) {
        System.out.println("Starting train() for model 1...");
        
        // count all the French words
        HashSet<String> fr_words = new HashSet<String>();
        for (SentencePair dataPoint : trainingData) {
            fr_words.addAll(dataPoint.sourceWords);
        }
        
        double tfe_p0 = 1.0 / fr_words.size();
        
        // Iterate a bunch of times; hopefully it converges by then
        for (int p = 0; p < NUM_ITERS; p++) {
            System.out.println("Model 1 iteration " + p);
            
            clearCounts();

            // E-Step
            System.out.println("E-step...");
            for (int k = 0; k < trainingData.size(); k++) {
                List<String> f = trainingData.get(k).getSourceWords();
                List<String> e = new ArrayList<String>(trainingData.get(k).getTargetWords());
                e.add(NULL_WORD);
                
                int m = f.size();
                int l = e.size();
                
                // source
                for (int i = 0; i < m; i++) {
                    String currFWord = f.get(i);
                    
                    // compute the normalization factor for delta
                    double deltaNormalization = 0.0;
                    
                    // target
                    for (int j = 0; j < l; j++) {
                        
                        // on the first iteration, t_fe are just uniformly initialized
                        if (p == 0) {
                            deltaNormalization += q_jilm(j, i, l, m) * tfe_p0;
                        }
                        else {
                            deltaNormalization += q_jilm(j, i, l, m) * t_fe.getCount(currFWord, e.get(j));
                        }
                    }
                    
                    // target
                    for (int j = 0; j < l; j++) {
                        String currEWord = e.get(j);
                        
                        double delta = 0.0;
                        // on the first iteration, t_fe are just uniformly initialized
                        if (p == 0) {
                            delta = q_jilm(j, i, l, m) * tfe_p0 / deltaNormalization;
                        }
                        else {
                            delta = q_jilm(j, i, l, m) * t_fe.getCount(currFWord, currEWord) / deltaNormalization;
                        }
                        
                        c_ef.incrementCount(currEWord, currFWord, delta);
                        c_e.incrementCount(currEWord, delta);
                    }
                }
            }
            
            // M-Step
            System.out.println("M-step...");
            t_fe = new CounterMap<String, String>();
            for (String e : c_ef.keySet()) {
                for (String f : c_ef.getCounter(e).keySet()) {
                    t_fe.setCount(f, e, c_ef.getCount(e, f) / c_e.getCount(e));
                }
            }
            
            System.out.println("Finished train() for Model 1.");
        }
    }
}
