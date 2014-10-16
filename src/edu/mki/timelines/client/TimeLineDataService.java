package edu.mki.timelines.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.mki.timelines.shared.model.WordAtDateData;

@RemoteServiceRelativePath("timelines")
public interface TimeLineDataService extends RemoteService {
	ArrayList<String> getWords();

	ArrayList<Date> getDates();

	TreeMap<Date, WordAtDateData> getTimeLine(String word);

	Map<String, Integer> getTopWordsAtDate(Date startDate, Date endDate);

	ArrayList<String[]> getSentences(String dbName, String wort);

	Map<String, Integer> getKollokSimWords(String dbName, String wort);

	TreeMap<Date, WordAtDateData> getTimeLineFourier(String word);
}
