package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

import org.tartarus.snowball.SnowballStemmer;

public class SnowballStemmerSingelton {
	
	private static SnowballStemmerSingelton instance = null;
	private SnowballStemmer stemmer;
	
	public static SnowballStemmerSingelton instance() {
		if(instance==null) {
			instance = new SnowballStemmerSingelton();
		}
		return instance;
	}
	
	private SnowballStemmerSingelton() {
		Class stemClass;
		try {
			stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String stemm(String value) {
		stemmer.setCurrent(value);
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
