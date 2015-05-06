package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import contentAnalysis.Restaurant;
import contentAnalysis.Review;


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
		HashSet<String> foods = absa.readCsvFoodList();
		logActivity("read csv food name list");
		absa.replaceFoods(foods);
		logActivity("replace food name with food");
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
	public void replaceFoods(HashSet<String> foods) throws IOException {
		String newRev = null;
		for (Restaurant restaurant : restaurants) {
			for (Review review : restaurant.getReviews()) {
				String[] reviewSplit = review.toString().split("\\t");
				String revi = reviewSplit[5];
				boolean found = false;
				newRev = review.toString();
				for (String word : revi.split("\\s")) {
					if (foods.contains(word) && !word.equals("at") && !word.equals("am") && !word.equals("as")) {
						System.out.println(word);
						found = true;
						newRev = newRev.toString().replaceAll(word, "food");
					}
				}
				if (found == true) {
					BufferedWriter output = new BufferedWriter(new FileWriter(new File("food/found/" + restaurant.id)));
					output.write(newRev + "\n");
					output.close();
				} else {
					BufferedWriter output = new BufferedWriter(new FileWriter(new File("food/not_found/"
							+ restaurant.id)));
					output.write(review.toString() + "\n");
					output.close();
				}
			}
		}
	}
	
	private HashSet<String> readCsvFoodList() {
		HashSet<String> foodList = new HashSet<String>();
		try {
			Scanner scanner = new Scanner(new File("cofids.csv"));
			scanner.useDelimiter(":");
			while (scanner.hasNext()) {
				scanner.next();
				String food = scanner.next();
				foodList.add(food.split(",")[0].toLowerCase());
				scanner.nextLine();
			}
			scanner.close();

		} catch (Exception e) {
		}
		//custom words
		foodList.add("chicken");
		foodList.add("corn");
		foodList.add("nuggets");
		foodList.add("cakes");
		foodList.add("spinach");
		foodList.add("duck");
		foodList.add("curry");
		foodList.add("steak");
		foodList.add("fish");
		foodList.add("peppers");
		foodList.add("food");
		foodList.add("fries");
		foodList.add("meal");
		foodList.add("beans");
		foodList.add("burger");
		foodList.add("calamari");
		foodList.add("crab legs");
		return foodList;
	}
}

