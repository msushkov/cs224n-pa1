package cs224n.wordaligner;

import java.util.ArrayList;
import java.util.List;

import cs224n.util.Counter;
import cs224n.util.Pair;

public class PMIModel implements WordAligner {
	Counter<String> wordCountSource = new Counter<String>();
	Counter<String> wordCountTarget = new Counter<String>();
	Counter<Pair<String, String>> pairCounts = new Counter<Pair<String, String>>();

	@Override
	public Alignment align(SentencePair sentencePair) {
		Alignment alignment = new Alignment();
		
		List<String> sourceWords = new ArrayList<String>(sentencePair.getSourceWords());
		sourceWords.add(0, NULL_WORD);
		List<String> targetWords = sentencePair.getTargetWords();
		
		double totalPairCounts = pairCounts.totalCount();
		double totalCountSource = wordCountSource.totalCount();
		double totalCountTarget = wordCountTarget.totalCount();
		
		for (int i = 0; i < targetWords.size(); i++) {
			String targetWord = targetWords.get(i);
			double maxValue = 0;
			int maxIndex = 0;
			for (int j = 0; j < sourceWords.size(); j++) {
				String sourceWord = sourceWords.get(j);
				double p_fe = pairCounts.getCount(new Pair<String, String>(sourceWord, targetWord)) / totalPairCounts;
				double p_e = wordCountTarget.getCount(targetWord) / totalCountTarget;
				double p_f = wordCountSource.getCount(sourceWord) / totalCountSource;
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
			List<String> sourceWords = new ArrayList<String>(pair.getSourceWords());
			sourceWords.add(0, NULL_WORD);
			List<String> targetWords = pair.getTargetWords();

			for (String word : sourceWords) {
				wordCountSource.incrementCount(word, 1);
			}

			for (String word : targetWords) {
				wordCountTarget.incrementCount(word, 1);
			}

			for (String sourceWord : sourceWords) {
				for (String targetWord : targetWords) {
					pairCounts.incrementCount(new Pair<String, String>(sourceWord, targetWord), 1);
				}
			}
		}
	}

}
