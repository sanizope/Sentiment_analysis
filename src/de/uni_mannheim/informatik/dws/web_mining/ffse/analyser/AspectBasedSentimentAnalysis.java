package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class AspectBasedSentimentAnalysis {
	
	private final static int maxNumberOfReviewsLoaded = 50;
	ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
	public static Long activityTime = System.nanoTime();

	
	
	public AspectBasedSentimentAnalysis() throws IOException {
		int i = 0;
		File[] files = new File("data/").listFiles();
		for(File file:files) {
			Restaurant restaurant = new Restaurant();
			restaurant.id = file.getName();
			restaurant.load("data/");
			if(restaurant.numOfReviews>0) {
				restaurants.add(restaurant);
				i++;
			}
			if(i>=maxNumberOfReviewsLoaded) {
				break;
			}
		}
	}

	
	/*
	 *   MAIN 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Start program");
		AspectBasedSentimentAnalysis absa = new AspectBasedSentimentAnalysis();
		logActivity("Loaded reviews");
		absa.spellcorrectReviews();
		logActivity("Spellcorrected reviews");
		absa.tagReviews();
		logActivity("Tagged sentences of reviews");
		Review.clearNounfrequencies();
		absa.saveNounList();
		logActivity("Saved nounlist");
		absa.saveReviews();
		System.out.println("Finished");

	}
	
	public static void logActivity(String text) {
		System.out.println(text + " in " + ((System.nanoTime()-activityTime)/1000000000.0f) + " seconds");
		activityTime = System.nanoTime();
	}
	
	public void spellcorrectReviews() throws Exception {
		WikiChecker.instance().loadDictionary();
		try {
			for(Restaurant restaurant:restaurants) {
				restaurant.spellcorrectReviews();
			}
		} finally {
			WikiChecker.instance().saveDictionary();
		}
	}
	
	public void saveReviews() throws IOException {
		for(Restaurant restaurant:restaurants) {
			restaurant.save("data-preprocessed/");
		}
	}
	
	public void tagReviews()  {
		for(Restaurant restaurant:restaurants) {
			restaurant.tagReviews();
		}
	}
	
	public void saveNounList() throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(new File("createdData/nounList")));
		Set<String> nouns = Review.nounFrequencies.keySet();
		for(String noun:nouns) {
			output.write(noun +  "\n");
		}
		output.close();
	}
}

