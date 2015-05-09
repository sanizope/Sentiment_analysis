package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Restaurant {
	String id = "",
		name = "",
	 	addressAndContact = "",
	 	category = "";
	int food = -1, service = -1, price_value = -1, atmosphere = -1, overall = -1,
			numOfReviews=-1;
	
	ArrayList<Review> reviews = new ArrayList<Review>();
	
	public void spellcorrectReviews() throws Exception {
		for(Review review:reviews) {
			review.spellcheckAndCorrectText(this.name);
		}
	}
	
	public void tagReviews() {
		for(Review review:reviews) {
			review.tagSentences();
		}
	}
	
	public ArrayList<Review> getReviews(){
		return reviews;
	}
	
	public void replaceWordByCategory(String category, HashSet<String> wordlist){
		for(Review review:reviews) {
			review.replaceWordByCategory(category, wordlist);
		}
	}
	
	public void save(String path) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(new File(path + id)));
			for (Field field : this.getClass().getDeclaredFields()) {
			    field.setAccessible(true);
			    if(!field.getName().equals("reviews")) {
				    Object value = field.get(this);
				    output.write(value + "\n");
				}
			}
			output.write("\n\n");
			for(Review review:reviews) {
				output.write(review + "\n");
			}
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveText(String path) {
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(new File(path + id)));
			for (Review review:reviews) {
				for(TaggedSentence sentence:review.getTaggedSentences()) {
					output.write(sentence.sentence + "\n");
				}
			}
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void load(String path) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(new File(path + id)));
			for (Field field : this.getClass().getDeclaredFields()) {
			    field.setAccessible(true);
			    if(!field.getName().equals("reviews")) {
			    	String line = input.readLine();
			    	try {
			    		field.set(this, line);
			    	} catch(IllegalArgumentException e) {
			    		try {
			    			field.set(this, Integer.parseInt(line));
			    		} catch(IllegalArgumentException e2) {
			    			try {
				    			field.set(this, Boolean.parseBoolean(line));
				    		} catch(IllegalArgumentException e3) {
				    			e3.printStackTrace();
				    		}
			    		}
			    	}
				}
			}
			input.readLine();
			input.readLine();
			for(int i=0; i<this.numOfReviews; ++i) {
				Review review = new Review();
				review.fromString(input.readLine());
				reviews.add(review);
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void print() {
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
			    field.setAccessible(true);
			    String name = field.getName();
			    Object value = field.get(this);
			    System.out.printf("%s -> %s%n", name, value);
			}
			
			String str = "";
			for (Field field : Review.class.getDeclaredFields()) {
			    field.setAccessible(true);
			    str += field.getName() + "\t";
			}
			System.out.println(str);
			for(Review review:reviews) {
				System.out.println(review);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
