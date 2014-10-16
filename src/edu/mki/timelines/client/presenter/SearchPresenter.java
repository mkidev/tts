package edu.mki.timelines.client.presenter;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import edu.mki.timelines.client.TimeLineDataService;
import edu.mki.timelines.client.events.WordAddedEvent;
import edu.mki.timelines.shared.model.SearchWord;
import edu.mki.timelines.shared.model.SearchWordList;

public class SearchPresenter {
	interface MyEventBinder extends EventBinder<SearchPresenter> {
	}

	private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

	private TimeLineDataService rpcSercice;
	private EventBus eventBus;
	private ListDataProvider<SearchWord> dataProvider;
	private MultiWordSuggestOracle suggestOracle;
	private SearchWordList searchWordList;

	public SearchPresenter(EventBus eventBus, TimeLineDataService rpcService) {
		this.eventBus = eventBus;
		this.rpcSercice = rpcService;
		eventBinder.bindEventHandlers(this, eventBus);
		searchWordList = new SearchWordList();
		dataProvider = new ListDataProvider<SearchWord>();

	}

	@EventHandler
	void onWordAdded(WordAddedEvent event) {
		getTimeLineData(event.getWord());
	}

	void getTimeLineData(String word) {
		DataTable data = DataTable.create();
		Queue<Double> queue = new LinkedList<>();
		CalendarUtil cu = new CalendarUtil();
		DateTimeFormat sdf = DateTimeFormat.getFormat("MM-dd-yyyy");
		data.addColumn(ColumnType.DATE);
		data.addColumn(ColumnType.NUMBER);
		Date start = sdf.parseStrict("08-13-2014");
		double calc = Math.random();
		double monthCount = 0;
		boolean monthly = false;
		boolean yearly = true;
		int lastMonth = start.getMonth();
		int lastYear = start.getYear();

		for (int i = 0; i < 999; i++) {
			monthCount += calc;
			if (monthly) {
				if (lastMonth != start.getMonth()) {
					lastMonth = start.getMonth();
					data.addRow();
					data.setValue(data.getNumberOfRows() - 1, 0, start);
					data.setValue(data.getNumberOfRows() - 1, 1, monthCount);
				}
			} else if (yearly) {
				if (lastMonth != start.getYear()) {
					lastMonth = start.getYear();
					data.addRow();
					data.setValue(data.getNumberOfRows() - 1, 0, start);
					data.setValue(data.getNumberOfRows() - 1, 1, monthCount);
				}
			}
			CalendarUtil.addDaysToDate(start, -1);
			calc = Math.random();

		}

	}

	public void go(HasWidgets container) {
		test();
	}

	public void test() {
		dataProvider.getList().add(new SearchWord("test1"));
		// dataProvider.getList().add(new SearchWord("test2"));
		// dataProvider.getList().add(new SearchWord("test3"));

		suggestOracle.add("hallo");
		suggestOracle.add("holla");
		suggestOracle.add("hodeo");
		suggestOracle.add("rasen");
		suggestOracle.add("zaster");
		suggestOracle.add("ruder");
		suggestOracle.add("rasant");
		suggestOracle.add("ziege");

		eventBus.fireEvent(new WordAddedEvent(""));

	}

}
