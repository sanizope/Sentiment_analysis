package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

public class TaggedSentence {
	
	String[] words;
	String[] posTags;
	String sentence;
	
	public TaggedSentence(String[] words, String[] posTags){
		this.words = words;
		this.posTags = posTags;
		this.sentence = joinWordsToSentence(this.words);
	}
	
	/**
	 * joins all words of the sentence to a string
	 * @param words
	 * @return
	 */
	private String joinWordsToSentence(String[] words){
		String sent = "";
		for (String w : words){
			sent = sent.concat(w + " ");
		}
		return sent;
	}
	
	
	public String[] getWords() {
		return words;
	}

	public String[] getPosTags() {
		return posTags;
	}

	public String getWord(int index){
		return this.words[index];
	}
	
	public String getPosTag(int index){
		return this.posTags[index];
	}
	
	public String getSentence(){
		return this.sentence;
	}

}
