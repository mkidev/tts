package edu.mki.timelines.shared.model;

import java.util.ArrayList;
import java.util.List;

public class SearchWordList {
	private ArrayList<SearchWord> searchWordList;

	public SearchWordList() {
		searchWordList = new ArrayList<SearchWord>();
	}

	public void addWord(String word) {
		searchWordList.add(new SearchWord(word));
	}

	public List<SearchWord> getSearchWords() {
		return searchWordList;
	}

	public List<String> getWordStrings() {
		List<String> result = new ArrayList<>();
		for (SearchWord word : searchWordList) {
			result.add(word.toString());
		}
		return result;
	}
}
