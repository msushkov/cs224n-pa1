package cs224n.wordaligner;

import java.util.ArrayList;
import java.util.List;

import cs224n.util.Counter;
import cs224n.util.CounterMap;

/*
 * Superclass for IBM Models 1 and 2.
 */
public abstract class IBMModel implements WordAligner {

    int NUM_ITERS = 30;
    
    // Parameters
    CounterMap<String, String> t_fe = new CounterMap<String, String>();
    CounterMap<String, Integer> q_jilm = new CounterMap<String, Integer>();
    
    // Counters
    CounterMap<String, String> c_ef = new CounterMap<String, String>();
    Counter<String> c_e = new Counter<String>();
    
    // c(j, i, l, m)
    CounterMap<String, Integer> c_jilm = new CounterMap<String, Integer>();
    Counter<String> c_ilm = new Counter<String>(); 
    
	@Override
	public Alignment align(SentencePair sentencePair) {	    
		Alignment alignment = new Alignment();
		
		List<String> f_words = sentencePair.getSourceWords();
		List<String> e_words = new ArrayList<String>(sentencePair.getTargetWords());
		e_words.add(NULL_WORD);
		
		int m = f_words.size();
		int l = e_words.size();
		
		// source
		for (int i = 0; i < m; i++) {
            String curr_f_word = f_words.get(i);
            
            double maxValue = 0;
            int maxIndex = 0;
            
            // target
            for (int j = 0; j < l; j++) {
                double value = q_jilm(j, i, l, m) * t_fe.getCount(curr_f_word, e_words.get(j));
                
                if (value > maxValue) {
                    maxValue = value;
                    maxIndex = j;
                }
            }
            if (maxIndex != l - 1) {
                alignment.addPredictedAlignment(maxIndex, i);
            }
        }
		
		return alignment;
	}

	/*
	 * To be implemented by Models 1 and 2 directly.
	 */
    public abstract void train(List<SentencePair> trainingData);
    
    /*
     * To be implemented by Models 1 and 2 directly.
     */
    public abstract double q_jilm(int j, int i, int l, int m);
    
    /*
     * Set all Counter entries to 0.
     */
    public void clearCounts() {
    	c_ef = new CounterMap<String, String>();
        c_e = new Counter<String>();
        c_jilm = new CounterMap<String, Integer>();
        c_ilm = new Counter<String>(); 
    }
    
    /*
     * Given three ints, converts them to a unique string representation.
     */
    public String convertIntsToStringKey(int i, int l, int m) {
        return "" + i + "_" + l + "_" + m;
    }
}
