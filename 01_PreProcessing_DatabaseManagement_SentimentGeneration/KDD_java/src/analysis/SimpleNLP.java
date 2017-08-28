package analysis;

import java.util.Properties;

import edu.stanford.nlp.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.neural.rnn.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.sentiment.*;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;


public class SimpleNLP {
	
	static StanfordCoreNLP pipeline;

	public static void init() {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		pipeline = new StanfordCoreNLP(props);
	}

	public static double findSentiment(String text) {

		double mainSentiment = 0;
		if (text != null && text.length() > 0) {
			
			Long textLength = 0L;
		    int sumOfValues = 0;

			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
			if (text != null && text.length() > 0) {
		        int longest = 0;
		        Annotation annotation = pipeline.process(text);
		        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
		            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
		            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
		            String partText = sentence.toString();
		            if (partText.length() > longest) {
		                textLength += partText.length();
		                sumOfValues = sumOfValues + sentiment * partText.length();

		                System.out.println(sentiment + " " + partText);
		            }
		        }
		    }
			mainSentiment = Double.valueOf(sumOfValues)/textLength;
		}
		
		return mainSentiment;
	}

}
