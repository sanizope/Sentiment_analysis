package de.uni_mannheim.informatik.dws.web_mining.ffse.analyser;

import java.util.Comparator;

import org.languagetool.rules.RuleMatch;

import edu.stanford.nlp.util.Pair;

public class MatchIndexSorter implements Comparator<Pair<RuleMatch, String>>{
	
	public int compare(Pair<RuleMatch, String> o1, Pair<RuleMatch, String> o2) {
		//System.out.println(o1.first.getFromPos() + " - " + o2.first.getFromPos() + " = " +  (o1.first.getFromPos()-o2.first.getFromPos()));
	    return o1.first.getFromPos()-o2.first.getFromPos();
	}
}
