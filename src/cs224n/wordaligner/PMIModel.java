package cs224n.wordaligner;

import java.util.ArrayList;
import java.util.List;

import cs224n.util.Counter;
import cs224n.util.CounterMap;

public class PMIModel implements WordAligner {
	Counter<String> c_f = new Counter<String>();
	Counter<String> c_e = new Counter<String>();
	CounterMap<String, String> c_fe = new CounterMap<String, String>();

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
				double p_fe = c_fe.getCount(curr_f_word, curr_e_word) / c_fe.totalCount();
				double p_e = c_e.getCount(curr_e_word) / c_e.totalCount();
				double p_f = c_f.getCount(curr_f_word) / c_f.totalCount();
				double value = p_fe / (p_e * p_f);
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
		for (SentencePair pair : trainingData) {
			List<String> f_words = new ArrayList<String>(pair.getSourceWords());
			f_words.add(NULL_WORD);
			List<String> e_words = pair.getTargetWords();

			for (String word : f_words) {
				c_f.incrementCount(word, 1);
			}

			for (String word : e_words) {
				c_e.incrementCount(word, 1);
			}

			for (String f_word : f_words) {
				for (String e_word : e_words) {
					c_fe.incrementCount(f_word, e_word, 1);
				}
			}
		}
	}

}
