package de.uni_mannheim.informatik.dws.web_mining.ffse.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Restaurant {
	String id = "",
		name = "",
	 	addressAndContact = "",
	 	category = "";
	int food = -1, service = -1, price_value = -1, atmosphere = -1, overall = -1,
			numOfReviews=-1;
	
	ArrayList<Review> reviews = new ArrayList<Review>();
	
	
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
