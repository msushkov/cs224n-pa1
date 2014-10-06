package cs224n.wordaligner;

import java.util.List;
import java.util.Random;

import cs224n.util.CounterMap;
import cs224n.util.Counters;

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
                SentencePair currDataPoint = trainingData.get(k);
                
                List<String> f = currDataPoint.getSourceWords();
                f.add(NULL_WORD);
                List<String> e = currDataPoint.getTargetWords();
                
                int m = f.size();
                int l = e.size();
                
                for (int i = 0; i < m; i++) {
                    String currFWord = f.get(i);
                    
                    // compute the normalization factor for delta
                    double deltaNormalization = 0.0;
                    for (int j = 0; j < l; j++) {
                        deltaNormalization += q_jilm(j, i, l, m) * t_fe.getCount(currFWord, e.get(j));
                    }

                    for (int j = 0; j < l; j++) {
                        String currEWord = e.get(j);
                        
                        double delta = q_jilm(j, i, l, m) * t_fe.getCount(currFWord, currEWord) / deltaNormalization; 

                        c_ef.incrementCount(currEWord, currFWord, delta);
                        c_e.incrementCount(currEWord, delta);

                        String ilm = convertIntsToStringKey(i, l, m);
                        c_jilm.incrementCount(ilm, j, delta);
                        c_ilm.incrementCount(ilm, delta);
                    }
                }
            }

            // M-Step
            t_fe = new CounterMap<String, String>();
            q_jilm = new CounterMap<String, Integer>();
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
