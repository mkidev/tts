package edu.mki.timelines.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.mki.timelines.shared.model.WordAtDateData;

public interface TimeLineDataServiceAsync {

	void getWords(AsyncCallback<ArrayList<String>> callback);

	void getDates(AsyncCallback<ArrayList<Date>> callback);

	void getTimeLine(String word,
			AsyncCallback<TreeMap<Date, WordAtDateData>> callback);

	void getTopWordsAtDate(Date startDate, Date endDate,
			AsyncCallback<Map<String, Integer>> callback);

	void getSentences(String dbName, String wort,
			AsyncCallback<ArrayList<String[]>> callback);

	void getKollokSimWords(String dbName, String wort,
			AsyncCallback<Map<String, Integer>> callback);

	void getTimeLineFourier(String word,
			AsyncCallback<TreeMap<Date, WordAtDateData>> callback);

}
