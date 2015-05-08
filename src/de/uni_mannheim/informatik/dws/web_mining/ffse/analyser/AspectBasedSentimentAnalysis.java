package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class AspectBasedSentimentAnalysis {

	private final static int maxNumberOfReviewsLoaded = 7000;
	ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
	public static Long activityTime = System.nanoTime();

	public AspectBasedSentimentAnalysis() throws IOException {
		int i = 0;
		File[] files = new File("data/").listFiles();
		for (File file : files) {
			Restaurant restaurant = new Restaurant();
			restaurant.id = file.getName();
			restaurant.load("data/");
			if (restaurant.numOfReviews > 0) {
				restaurants.add(restaurant);
				i++;
			}
			if (i >= maxNumberOfReviewsLoaded) {
				break;
			}
		}
	}

	/*
	 * MAIN
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Start program");
		AspectBasedSentimentAnalysis absa = new AspectBasedSentimentAnalysis();
		logActivity("Loaded reviews");
		absa.spellcorrectReviews();
		logActivity("Spellcorrected reviews");
		absa.tagReviews();
		logActivity("Tagged sentences of reviews");
		// Review.clearNounfrequencies();
		Review.saveNounList();
		logActivity("Saved nounlist");
		HashSet<String> foods = absa.getFoodList();
		logActivity("read csv food name list");
		absa.replaceWordByCategoryInReviews("food", foods);
		logActivity("replace food name with food");
		HashSet<String> service = absa.getServiceList();
		absa.replaceWordByCategoryInReviews("service", service);
		logActivity("replace service synonyms with service");
		HashSet<String> priceValue = absa.getPriceValueList();
		absa.replaceWordByCategoryInReviews("price_value", priceValue);
		logActivity("replace price/value synonyms with price_value");
		HashSet<String> atmosphere = absa.getAtmosphereList();
		absa.replaceWordByCategoryInReviews("_atmosphere_", atmosphere);
		logActivity("replace atmosphere synonyms with _atmosphere_");
		absa.saveReviews();
		System.out.println("Finished");

	}

	public static void logActivity(String text) {
		System.out.println(text + " in " + ((System.nanoTime() - activityTime) / 1000000000.0f) + " seconds");
		activityTime = System.nanoTime();
	}

	public void spellcorrectReviews() throws Exception {
		WikiChecker.instance().loadDictionary();
		try {
			for (Restaurant restaurant : restaurants) {
				restaurant.spellcorrectReviews();
			}
		} finally {
			WikiChecker.instance().saveDictionary();
		}
	}

	public void replaceWordByCategoryInReviews(String category, HashSet<String> wordlist) {
		for (Restaurant restaurant : restaurants) {
			restaurant.replaceWordByCategory(category, wordlist);
		}
	}

	public void saveReviews() throws IOException {
		for (Restaurant restaurant : restaurants) {
			restaurant.save("data-preprocessed/");
		}
	}

	public void tagReviews() {
		for (Restaurant restaurant : restaurants) {
			restaurant.tagReviews();
		}
	}

	private HashSet<String> getFoodList() {
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
		foodList.remove("at");
		foodList.remove("as");
		foodList.remove("am");
		// custom words
		foodList.add("food");
		foodList.add("chicken");
		foodList.add("corn");
		foodList.add("nuggets");
		foodList.add("cakes");
		foodList.add("spinach");
		foodList.add("duck");
		foodList.add("curry");
		foodList.add("steak");
		foodList.add("steaks");
		foodList.add("fish");
		foodList.add("peppers");
		foodList.add("food");
		foodList.add("foods");
		foodList.add("fries");
		foodList.add("meal");
		foodList.add("meals");
		foodList.add("beans");
		foodList.add("burger");
		foodList.add("burgers");
		foodList.add("calamari");
		foodList.add("crab legs");
		foodList.add("shrimp");
		foodList.add("prime rib");
		foodList.add("fajitas");
		foodList.add("wine");
		foodList.add("dinner");
		foodList.add("dishes");
		foodList.add("dish");
		foodList.add("desserts");
		foodList.add("dessert");
		foodList.add("pies");
		foodList.add("pie");
		foodList.add("cassoulet");
		foodList.add("porkchops");
		foodList.add("porkchop");
		foodList.add("ribeye");
		foodList.add("sandwiches");
		foodList.add("cheddar");
		foodList.add("appetizer");
		foodList.add("appetizers");
		foodList.add("entree");
		foodList.add("entrees");
		foodList.add("sausage");
		foodList.add("sausages");
		foodList.add("nachos");
		foodList.add("beers");
		foodList.add("breakfast");
		foodList.add("ribs");
		foodList.add("salads");
		return foodList;
	}

	private HashSet<String> getServiceList() {
		HashSet<String> serviceList = new HashSet<String>();
		serviceList.add("services");
		serviceList.add("waiter");
		serviceList.add("waitress");
		serviceList.add("server");
		serviceList.add("bartender");
		serviceList.add("barkeeper");
		serviceList.add("barman");
		serviceList.add("barmaid");
		serviceList.add("staff");
		serviceList.add("management");
		serviceList.add("serviceability");
		serviceList.add("servicing");
		serviceList.add("serving");
		serviceList.add("labor");
		serviceList.add("assist");
		serviceList.add("assisting");
		serviceList.add("work");
		serviceList.add("working");
		serviceList.add("appropriateness");
		serviceList.add("duty");
		return serviceList;
	}

	// \d+([\,\.]\d{1,2})?\s*\$
	private HashSet<String> getPriceValueList() {
		HashSet<String> priceValueList = new HashSet<String>();
		priceValueList.add("prices");
		priceValueList.add("value");
		priceValueList.add("values");
		priceValueList.add("cost");
		priceValueList.add("costs");
		priceValueList.add("price");
		return priceValueList;
	}

	private HashSet<String> getAtmosphereList() {
		HashSet<String> atmosphereList = new HashSet<String>();
		atmosphereList.add("ambience");
		atmosphereList.add("ambiance");
		atmosphereList.add("appearance");
		atmosphereList.add("aura");
		atmosphereList.add("feel");
		atmosphereList.add("feeling");
		atmosphereList.add("mood");
		atmosphereList.add("presence");
		atmosphereList.add("semblance");
		atmosphereList.add("climate");
		atmosphereList.add("surroundings");
		atmosphereList.add("surrounding");
		atmosphereList.add("culture");
		atmosphereList.add("environment");
		atmosphereList.add("locality");
		atmosphereList.add("location");
		atmosphereList.add("locale");
		atmosphereList.add("scenery");
		atmosphereList.add("place");
		atmosphereList.add("view");
		atmosphereList.add("outlook");
		atmosphereList.add("setting");
		atmosphereList.add("spot");
		atmosphereList.add("decoration");
		atmosphereList.add("decorated");
		atmosphereList.add("setup");
		return atmosphereList;
	}
}
