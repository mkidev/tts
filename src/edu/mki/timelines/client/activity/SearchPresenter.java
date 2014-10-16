package edu.mki.timelines.client.activity;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.place.shared.Place;
import com.googlecode.gwt.charts.client.Selection;

public interface SearchPresenter {
	String getPeakDate = null;

	void goTo(Place place);

	void addTimeLine(String word);

	void addWord(String word);

	void dateSelected(Date value, Integer timeSpan);

	void dateSelected(Date value);

	String getPeriod();

	List<String> getWordList();

	void dataSelected(JsArray<Selection> jsArray);

	void setTableDate(int i);

	void peakWordSelected(JsArray<Selection> selection);

	String getTimeSearchDate();

}
