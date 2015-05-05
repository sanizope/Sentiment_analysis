package de.uni_mannheim.informatik.dws.web_mining.ffse.crawler;

import java.lang.reflect.Field;

public class Review {
	
	int food=-1, service=-1, price_value=-1, atmosphere=-1, overall=-1;
	
	String text="";
	
	int pleasant=-1;
	int persons=-1;
	boolean reservations=false;
	int wouldReturn=-1;
	String wine="";
	boolean creditCard=false;
	
	String username="";
	String date="";
	boolean firstReview=false;
	
	@Override
	public String toString() {
		String str = "";
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
			    field.setAccessible(true);
			    str += field.get(this) + "\t";
			}
		} catch(Exception ex) {
			
		}
		return str;
	}

}
