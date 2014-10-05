package cs224n.wordaligner;

import java.util.ArrayList;
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
    int NUM_ITERS = 20;
    
    // Parameters
    CounterMap<String, String> t_fe = new CounterMap<String, String>();
    CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>> q_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
    
    // Counters
    CounterMap<String, String> c_ef = new CounterMap<String, String>();
    Counter<String> c_e = new Counter<String>();
    
    // C(j, i, l, m)
    CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>> c_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
    Counter<Pair<Pair<Integer, Integer>, Integer>> c_ilm = new Counter<Pair<Pair<Integer, Integer>, Integer>>(); 
    
    /*
     * Model 1: set randomly; model 2: use the ones from model 1.
     * Model 1: make sure to normalize!
     */
    public abstract void initParams();
    
	@Override
	public Alignment align(SentencePair sentencePair) {
		Alignment alignment = new Alignment();
		
		List<String> f_words = new ArrayList<String>(sentencePair.getSourceWords());
		f_words.add(NULL_WORD);
		List<String> e_words = sentencePair.getTargetWords();
		
		for (int i = 0; i < e_words.size(); i++) {
			String curr_e_word = e_words.get(i);
			double maxValue = 0;
			int maxIndex = 0;
			for (int j = 0; j < f_words.size(); j++) {
				String curr_f_word = f_words.get(j);
				Pair<Pair<Integer, Integer>, Integer> ilm = new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(i, e_words.size()), f_words.size());
				double value = q_jilm.getCount(j, ilm) * t_fe.getCount(curr_f_word, curr_e_word);
				if (value > maxValue) {
					maxValue = value;
					maxIndex = j;
				}
			}
			if (maxIndex != f_words.size()-1) {
				alignment.addPredictedAlignment(i, maxIndex);
			}
		}
		
		return alignment;
	}

    @Override
    public void train(List<SentencePair> trainingData) {
    	initParams();
    	for (int p = 0; p < NUM_ITERS; p++) {
    		clearCounts();

    		// E-Step
    		for (int k = 0; k < trainingData.size(); k++) {
    			List<String> f = trainingData.get(k).getSourceWords();
    			f.add(NULL_WORD);
    			List<String> e = trainingData.get(k).getTargetWords();
    			for (int i = 0; i < f.size(); i++) {
    				for (int j = 0; j < e.size(); j++) {
    					double delta = delta_kij(k, i, j);
    					c_ef.incrementCount(e.get(i), f.get(j), delta);
    					c_e.incrementCount(e.get(i), delta);
    					Pair<Integer, Integer> il = new Pair<Integer, Integer>(i, e.size());
    					Pair<Pair<Integer, Integer>, Integer> ilm = new Pair<Pair<Integer, Integer>, Integer>(il, f.size());
    					c_jilm.incrementCount(j, ilm, delta);
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
    	    
    		for (Integer j : c_jilm.keySet()) {
    			for (Pair<Pair<Integer, Integer>, Integer> ilm : c_jilm.getCounter(j).keySet()) {    				
    				q_jilm.setCount(j, ilm, c_jilm.getCount(j, ilm) / c_ilm.getCount(ilm));
    			}
    		}
    	}
    	

    }
    
    /*
     * The delta for IBM Models 1 and 2. To be overridden.
     */
    public abstract double delta_kij(int k, int i, int j);
    
    //
    // PRIVATE METHODS
    //
    
    /*
     * Set all Counter entries to 0.
     */
    private void clearCounts() {
    	c_ef = new CounterMap<String, String>();
        c_e = new Counter<String>();
        c_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
        c_ilm = new Counter<Pair<Pair<Integer, Integer>, Integer>>(); 
    }
}
