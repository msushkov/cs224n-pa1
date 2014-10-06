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

    int NUM_ITERS = 100;
    
    // Parameters
    CounterMap<String, String> t_fe = new CounterMap<String, String>();
    CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>> q_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
    
    // Counters
    CounterMap<String, String> c_ef = new CounterMap<String, String>();
    Counter<String> c_e = new Counter<String>();
    
    // C(j, i, l, m)
    CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>> c_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
    Counter<Pair<Pair<Integer, Integer>, Integer>> c_ilm = new Counter<Pair<Pair<Integer, Integer>, Integer>>(); 
    
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
				double value = q_jilm(j, i, e_words.size(), f_words.size()) * t_fe.getCount(curr_f_word, curr_e_word);
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

	/*
	 * To be implemented by Models 1 and 2 directly.
	 */
    public abstract void train(List<SentencePair> trainingData);
    
    /*
     * To be implemented by Models 1 and 2 directly.
     */
    //Pair<Pair<Integer, Integer>, Integer> ilm = new Pair<Pair<Integer, Integer>, Integer>(new Pair<Integer, Integer>(i, e_words.size()), f_words.size());
    public abstract double q_jilm(int j, int i, int l, int m);
    
    /*
     * Set all Counter entries to 0.
     */
    public void clearCounts() {
    	c_ef = new CounterMap<String, String>();
        c_e = new Counter<String>();
        c_jilm = new CounterMap<Integer, Pair<Pair<Integer, Integer>, Integer>>();
        c_ilm = new Counter<Pair<Pair<Integer, Integer>, Integer>>(); 
    }
}
