package cs224n.wordaligner;

import java.util.ArrayList;
import java.util.List;

import cs224n.util.Counter;
import cs224n.util.Pair;

public class PMIModel implements WordAligner {
	Counter<String> c_f = new Counter<String>();
	Counter<String> c_e = new Counter<String>();
	Counter<Pair<String, String>> c_fe = new Counter<Pair<String, String>>();

	@Override
	public Alignment align(SentencePair sentencePair) {
		Alignment alignment = new Alignment();
		
		List<String> f_words = new ArrayList<String>(sentencePair.getSourceWords());
		f_words.add(0, NULL_WORD);
		List<String> e_words = sentencePair.getTargetWords();
		
		double totalPairCounts = c_fe.totalCount();
		double totalCountSource = c_f.totalCount();
		double totalCountTarget = c_e.totalCount();
		
		for (int i = 0; i < e_words.size(); i++) {
			String targetWord = e_words.get(i);
			double maxValue = 0;
			int maxIndex = 0;
			for (int j = 0; j < f_words.size(); j++) {
				String sourceWord = f_words.get(j);
				double p_fe = c_fe.getCount(new Pair<String, String>(sourceWord, targetWord)) / totalPairCounts;
				double p_e = c_e.getCount(targetWord) / totalCountTarget;
				double p_f = c_f.getCount(sourceWord) / totalCountSource;
				double value = p_fe / (p_e * p_f);
				if (value > maxValue) {
					maxValue = value;
					maxIndex = j;
				}
			}
			alignment.addPredictedAlignment(i, maxIndex);
		}
		
		return alignment;
	}

	@Override
	public void train(List<SentencePair> trainingData) {
		for (SentencePair pair : trainingData) {
			List<String> f_words = new ArrayList<String>(pair.getSourceWords());
			f_words.add(0, NULL_WORD);
			List<String> e_words = pair.getTargetWords();

			for (String word : f_words) {
				c_f.incrementCount(word, 1);
			}

			for (String word : e_words) {
				c_e.incrementCount(word, 1);
			}

			for (String sourceWord : f_words) {
				for (String targetWord : e_words) {
					c_fe.incrementCount(new Pair<String, String>(sourceWord, targetWord), 1);
				}
			}
		}
	}

}
