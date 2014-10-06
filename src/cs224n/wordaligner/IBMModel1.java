package cs224n.wordaligner;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

public class IBMModel1 extends IBMModel {

    @Override
    public double q_jilm(int j, int i, int l, int m) {
        return 1.0 / (l + 1.0);
    }
    
    @Override
    public void train(List<SentencePair> trainingData) {
        // count all the French words
        HashSet<String> fr_words = new HashSet<String>();
        for (SentencePair dataPoint : trainingData) {
            fr_words.addAll(dataPoint.targetWords); // sourceWords
        }
        int numFWords = fr_words.size();
        
        // Iterate a bunch of times; hopefully it converges by then
        for (int p = 0; p < NUM_ITERS; p++) {
            System.out.println("Iteration " + p);
            
            clearCounts();

            // E-Step
            for (int k = 0; k < trainingData.size(); k++) {
                List<String> f = new ArrayList<String>(trainingData.get(k).getSourceWords());
                f.add(NULL_WORD);
                List<String> e = trainingData.get(k).getTargetWords();
                for (int i = 0; i < f.size(); i++) {
                    double tfe_p0 = 1.0 / numFWords;
                    
                    // compute the normalization factor for delta
                    double deltaNormalization = 0.0;
                    for (int j = 0; j < e.size(); j++) {
                        // on the first iteration, t_fe are just uniformly initialized
                        if (p == 0) {
                            deltaNormalization += q_jilm(j, i, e.size(), f.size()) * tfe_p0;
                        }
                        else {
                            deltaNormalization += q_jilm(j, i, e.size(), f.size()) * t_fe.getCount(f.get(i), e.get(j));
                        }
                    }
                    
                    for (int j = 0; j < e.size(); j++) {
                        double delta = 0.0;
                        // on the first iteration, t_fe are just uniformly initialized
                        if (p == 0) {
                            delta = q_jilm(j, i, e.size(), f.size()) * tfe_p0 / deltaNormalization;
                        }
                        else {
                            delta = q_jilm(j, i, e.size(), f.size()) * t_fe.getCount(f.get(i), e.get(j)) / deltaNormalization;
                        }
                        
                        c_ef.incrementCount(e.get(j), f.get(i), delta);
                        c_e.incrementCount(e.get(j), delta);
                    }
                }
            }
            
            // M-Step
            for (String e : c_ef.keySet()) {
                for (String f : c_ef.getCounter(e).keySet()) {
                    t_fe.setCount(f, e, c_ef.getCount(e, f) / c_e.getCount(e));
                }
            }
        }
    }
}
