package edu.mki.timelines.client.place;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class SearchPlace extends Place {
	private List<String> wordList = new ArrayList<String>();
	private String period = "";
	private String timeSearchDate = "";

	public SearchPlace(List<String> wordList, String period) {
		this(wordList, period, "");
	}

	public SearchPlace(List<String> wordList, String period,
			String timeSearchDate) {
		this.wordList = wordList;
		this.period = period;
		this.timeSearchDate = timeSearchDate;
	}

	public SearchPlace(List<String> wordList) {
		this(wordList, "", "");
	}

	public List<String> getWordList() {
		return wordList;
	}

	public String getPeriod() {
		return period;
	}

	public String getTimeSearchDate() {
		return timeSearchDate;
	}

	public static class Tokenizer implements PlaceTokenizer<SearchPlace> {

		@Override
		public SearchPlace getPlace(String token) {
			String[] fields = token.split("&");
			String period = "monthly";
			List<String> result = new ArrayList<String>();
			String timeSearchDate = "";
			String peakDate = "";
			int peakBoarder = 1;
			String wordDate = "";
			GWT.log("Fields " + fields.length);
			for (String field : fields) {
				switch (field.substring(0, field.indexOf("=")).toLowerCase()) {
				case "words":
					String[] words = field.substring(6).split("(,)", 3);
					for (String word : words) {
						if (word.length() > 0)
							result.add(word);
					}
					break;
				case "period":
					period = field.substring(7).toLowerCase();
					GWT.log("period:" + period);
					break;
				case "timeSearchDate":
					timeSearchDate = field.substring(15).toLowerCase();
					GWT.log("timedate " + timeSearchDate.toString());
					break;
				default:
					break;
				}
			}

			return new SearchPlace(result, period, timeSearchDate);
		}

		@Override
		public String getToken(SearchPlace place) {
			StringBuilder sb = new StringBuilder();
			List<String> wordList = place.getWordList();
			sb.append("words=");
			for (int i = 0; i < wordList.size(); i++) {
				sb.append(wordList.get(i));
				if (i < wordList.size() - 1)
					sb.append(",");
			}
			sb.append("&period=");
			sb.append(place.getPeriod());
			sb.append("&timeSearchDate=");
			sb.append(place.getTimeSearchDate());
			return sb.toString();
		}
	}

}
