package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class WikiChecker {
	
	private static final int querysPerSecond = 10;
	private static final String userAgent = "Project FFSEfRR/FSS15, vschulz(at)mail.uni-mannheim.de";
	private static WikiChecker instance = null;
	private long timestampBlock = 0;
	private int blockCounter = 0;
	private HashMap<String, String> dictionary = new HashMap<String, String>();
	private HashMap<String, String> negativeCache = new HashMap<String, String>();
	private boolean hasDictionaryChanged = false;
	
	public static WikiChecker instance() {
		if(instance==null) {
			instance = new WikiChecker();
		}
		return instance;
	}
	
	private WikiChecker() {	    
	}
	
	public void loadDictionary() {
		loadHashmap(dictionary, "createdData/wiki.dic");
		loadHashmap(negativeCache, "createdData/neg.cache");
	}
	
	private void loadHashmap(HashMap<String, String> hashmap, String path) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
			String line = null;
			while((line=reader.readLine())!=null) {
				hashmap.put(line, line);
			}
			reader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveDictionary() {
		saveHashmap(dictionary, "createdData/wiki.dic");
		saveHashmap(negativeCache, "createdData/neg.cache");
	}
	
	private void saveHashmap(HashMap<String, String> hashmap, String path) {
		try {
			if(!hasDictionaryChanged) {
				return;
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)));
			for(String line:hashmap.values()) {
				writer.write(line + "\n");
			}
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public String checkWord(String word, String alternative) throws IOException, InterruptedException {
		if(dictionary.containsKey(word)) {
			return dictionary.get(word);
		}
		if(dictionary.containsKey(alternative)) {
			return dictionary.get(alternative);
		}
		if(negativeCache.containsKey(word)) {
			return null;
		}
		if(this.blockCounter>=10) {
			this.blockCounter=0;
			if((System.currentTimeMillis()-this.timestampBlock)/1000f<querysPerSecond/10f) {
				System.out.println("*********************************SLEEP*******************");
				Thread.sleep(500);
				this.timestampBlock = System.currentTimeMillis();
			}
		}
		blockCounter++;
		Document doc = Jsoup.connect("http://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + word + "&srprop=&format=xml&continue=&srlimit=5")
				 			.userAgent(userAgent)
				 			.parser(Parser.xmlParser())
				 			.get();
		String stemmedAlternative = SnowballStemmerSingelton.instance().stemm(alternative).toLowerCase();
		Elements results = doc.getElementsByAttribute("suggestion");
		if(results.size()==1) {
			String suggestion = results.get(0).attr("suggestion");
			if(suggestion.toLowerCase().equals(alternative.toLowerCase()) || SnowballStemmerSingelton.instance().stemm(suggestion).toLowerCase().equals(stemmedAlternative)) {
				hasDictionaryChanged = true;
				dictionary.put(alternative, alternative);
				return alternative;
			}
		}
		String stemmedWord = SnowballStemmerSingelton.instance().stemm(word).toLowerCase();
		results = doc.getElementsByAttribute("ns");
		for(Element el:results) {
			String[] titleWords = el.attr("title").split(" ");
			for(String titleWord:titleWords) {
				titleWord = titleWord.toLowerCase();
				String stemmedTitleWord = SnowballStemmerSingelton.instance().stemm(titleWord).toLowerCase();
				if(titleWord.equals(alternative.toLowerCase()) || stemmedTitleWord.equals(stemmedAlternative)) {
					hasDictionaryChanged = true;
					dictionary.put(alternative, alternative);
					return alternative;
				} else if(titleWord.equals(word.toLowerCase()) || stemmedTitleWord.equals(stemmedWord)) {
					hasDictionaryChanged = true;
					dictionary.put(word, word);
					return word;
				}				
			}
		}
		if(results.size()>0) {
			hasDictionaryChanged = true;
			dictionary.put(word, word);
			return word;
		}
		negativeCache.put(word, word);
		return null;
	}

}
