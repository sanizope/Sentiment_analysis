package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Properties;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.EditDistance;
import edu.stanford.nlp.util.Pair;


public class Review {
	
	/**
	 * contains all nouns and their frequencies within the whole corpus
	 * key: noun
	 * value: frequency
	 */
	public static HashMap<String, Integer> nounFrequencies = new HashMap<String, Integer>();
	
	int food=-1, service=-1, price_value=-1, atmosphere=-1, overall=-1;
	
	private String text="";
	private ArrayList<TaggedSentence> taggedSentences = new ArrayList<TaggedSentence>();
	
	int pleasant=-1;
	int persons=-1;
	boolean reservations=false;
	int wouldReturn=-1;
	String wine="";
	boolean creditCard=false;
	
	String username="";
	String date="";
	boolean firstReview=false;
	
	/**
	 * Method searches for errors and applies multiple checks to determine if it is an error and how to resolve it best 
	 * @param restaurantName
	 * @throws Exception
	 */
	public void spellcheckAndCorrectText(String restaurantName) throws Exception {
		JLanguageTool tool = new JLanguageTool(new AmericanEnglish());
		EditDistance dist = new EditDistance();
		List<RuleMatch> matches = tool.check(this.text);
		List<Pair<RuleMatch, String>> suggestedMatches = new ArrayList<Pair<RuleMatch, String>>();
		HashSet<String> includedStrings = new HashSet<String>();
		HashSet<String> excludedStrings = new HashSet<String>();
		excludedStrings.addAll(new HashSet<String>(Arrays.asList(restaurantName.split("[^a-zA-Z0-9]"))));

		for (RuleMatch match : matches) {
			if(match.getMessage().equals("Possible spelling mistake found")) {
				String mistake = this.text.substring(match.getFromPos(), match.getToPos());
				String replacementBuffer = "";
				double scoreBuffer = 999999d;
				boolean distinct = true;
				for(String replacement:match.getSuggestedReplacements()) {
					Double score = dist.score(mistake, replacement) + (replacement.contains(" ")?1.5:0);
					if(score<scoreBuffer) {
						replacementBuffer = replacement;
						scoreBuffer = score;
						distinct = true;
					} else if(score==scoreBuffer) {
						distinct = false;
					}
				}
				
				if(replacementBuffer.length()>0 && scoreBuffer>0 && distinct) {
					if(includedStrings.contains(mistake)) {
						excludedStrings.add(mistake);
					}
					suggestedMatches.add(new Pair<RuleMatch, String>(match, replacementBuffer));
					includedStrings.add(mistake);
				}
			}
		}
		
		includedStrings.removeAll(excludedStrings);
		
		int offset = 0;
		Collections.sort(suggestedMatches, new MatchIndexSorter());
		StringBuilder strBui = new StringBuilder(this.text);
		for(Pair<RuleMatch, String> match:suggestedMatches) {
			String mistake = strBui.substring(match.first().getFromPos()+offset, match.first().getToPos()+offset);
			if(!includedStrings.contains(mistake)) {
				continue;
			}
			boolean grammarMistake = false;
			if(!mistake.contains("'")&&match.second.contains("'")) {
				grammarMistake = true;
			}
			String suggestion=""; 
			if(!grammarMistake) {
				suggestion = WikiChecker.instance().checkWord(mistake, match.second());
			}
			if(grammarMistake || suggestion==null || match.second.equals(suggestion)) {
				//System.out.println(mistake + " -> " + match.second());
				strBui.replace(match.first().getFromPos()+offset, match.first().getToPos()+offset, match.second());
				offset += match.second().length()-(match.first().getToPos()-match.first().getFromPos());
			} else {
				//System.out.println(mistake + " -- " + match.second());
			}
		}
		this.text = strBui.toString();
	}
	
	/**
	 * applies Stanford Core NLP tools to content of a file
	 * writes processed output to new files
	 * @param fileName
	 * @throws IOException
	 */
	public void tagSentences() {
		PrintStream err = System.err;

		System.setErr(new PrintStream(new OutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				// TODO Auto-generated method stub
				
			}
		}));

	    StanfordCoreNLP pipeline;
		
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    pipeline = new StanfordCoreNLP(props);

	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(this.text);
	    //this.text = null;
	    
	    // run all Annotators on this text
	    //document.
	    pipeline.annotate(document);
	    
	    // these are all the sentences in this document
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    /*
	     * Iterate over all sentences
	     */
	    for(CoreMap sentence: sentences) {	    
	    	// create arrays for sentence
	    	String[] words = new String[sentence.get(TokensAnnotation.class).size()];
	    	String[] posTags = new String[sentence.get(TokensAnnotation.class).size()];
	    	int index = 0;

	    	/*
	    	 * Iterate over all tokens in sentence, 
	    	 * put information into two arrays (one for words, one for posTags)
	    	 */
	    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {

	    		// this is the text of the token
	    		String word = token.get(TextAnnotation.class); 
	    		// this is the POS tag of the token 
	    		String posTag = token.get(PartOfSpeechAnnotation.class); 
	    		
	    		
	    		// track occurrence frequencies of all nouns
	    		if (TaggedSentence.isNoun(posTag)){
	    			if (Review.nounFrequencies.containsKey(word)){
	    				Review.nounFrequencies.put(word, Review.nounFrequencies.get(word) + 1);
	    			}
	    			else {
	    				Review.nounFrequencies.put(word, 1);
	    			}
	    		}
	    		
	    		// put words and posTags in arrays
	    		words[index] = word;
	    		posTags[index] = posTag;
	    		index ++;
	    	}	      
	    	
	    	//create a TaggedSentence object and save it in list 
	    	taggedSentences.add(new TaggedSentence(words, posTags));
			System.setErr(err); 
	    	//extractInformation(words, posTags);
	    }
	}
	
	@Override
	public String toString() {
		String str = "";
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				if(!field.getName().equals("taggedSentences") && !field.getName().equals("nounFrequencies")) {
				    field.setAccessible(true);
				    str += field.get(this) + "\t";
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	
	public void fromString(String line) {
		String[] columns = line.split("\t");
		int column = 0;
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				if(!field.getName().equals("taggedSentences") && !field.getName().equals("nounFrequencies")) {
				    field.setAccessible(true);
			    	try {
			    		field.set(this, columns[column]);
			    	} catch(IllegalArgumentException e) {
			    		try {
			    			field.set(this, Integer.parseInt(columns[column]));
			    		} catch(IllegalArgumentException e2) {
			    			try {
				    			field.set(this, Boolean.parseBoolean(columns[column]));
				    		} catch(IllegalArgumentException e3) {
				    			e3.printStackTrace();
				    		}
			    		}
			    	}
			    	column++;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getText() {
		return text;
	}
	
	public ArrayList<TaggedSentence> getTaggedSentences() {
		return taggedSentences;
	}
	
	public void replaceWordByCategory(String category, HashSet<String> wordlist)  {
		for (TaggedSentence sentence : taggedSentences) {
			for (int wordCounter=0; wordCounter<sentence.words.length; ++wordCounter) {
				if (wordlist.contains(sentence.words[wordCounter].toLowerCase())) {
					//System.out.println(word);
					text = text.replaceAll(sentence.words[wordCounter], category);
					if(!TaggedSentence.isNoun(sentence.posTags[wordCounter])){
						System.out.println("Replaced non-noun " + sentence.words[wordCounter]);
					}
					sentence.words[wordCounter] = category;
				}
			}
		}
	}
	
	public static void clearNounfrequencies() {
		HashMap<String, Integer> newNounFrequencies = new HashMap<String, Integer>();
		for(Entry<String, Integer> entry:nounFrequencies.entrySet()) {
			String stemmedNoun = SnowballStemmerSingelton.instance().stemm(entry.getKey());
			if(newNounFrequencies.containsKey(stemmedNoun)) {
				newNounFrequencies.put(stemmedNoun, newNounFrequencies.get(stemmedNoun) + entry.getValue());
			} else {
				newNounFrequencies.put(stemmedNoun, entry.getValue());
			}
		}
		nounFrequencies = newNounFrequencies;
	}

	public static void saveNounList() throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(new File("createdData/nounList")));
		Set<String> nouns = Review.nounFrequencies.keySet();
		for(String noun:nouns) {
			output.write(noun +  "\n");
		}
		output.close();
	}
}
