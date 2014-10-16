package edu.mki.timelines.client.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.place.shared.Place;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.googlecode.gwt.charts.client.ChartType;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.Selection;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterOptions;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterState;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterStateRange;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterUi;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.corechart.LineChartOptions;
import com.googlecode.gwt.charts.client.options.AxisTitlesPosition;
import com.googlecode.gwt.charts.client.options.ChartArea;
import com.googlecode.gwt.charts.client.options.Legend;
import com.googlecode.gwt.charts.client.options.LegendPosition;
import com.googlecode.gwt.charts.client.options.VAxis;

import edu.mki.timelines.client.ClientFactory;
import edu.mki.timelines.client.events.LoadingSearchDatesEvent;
import edu.mki.timelines.client.events.LoadingSearchWordsEvent;
import edu.mki.timelines.client.events.LoadingTimelineEvent;
import edu.mki.timelines.client.place.SearchPlace;
import edu.mki.timelines.client.ui.search.SearchView;
import edu.mki.timelines.shared.model.TimeLine;
import edu.mki.timelines.shared.model.WordAtDateData;

public class SearchActivity extends AbstractActivity implements SearchPresenter {

	private ClientFactory clientFactory;
	private List<String> wordList;
	private HashMap<String, TimeLine> timeLines = new HashMap<>();
	private HashMap<String, Boolean> timeLineLoaded = new HashMap<>();
	private ArrayList<Date> dates = new ArrayList<>();
	private DataTable dataTable;
	private DataTable kookDataTable;
	private DataTable wordDataTable;
	private boolean datesLoaded = false;
	private String period;
	String timeSearchDate;
	private HashMap<String, Integer> wordToTableWordIDMap = new HashMap<>();
	private ChartArea chartArea;
	private Storage storage = Storage.getLocalStorageIfSupported();
	private Date wordTableDate;
	private String wordTable;
	protected Timer resizeTimer;
	private Date lastDate;

	public SearchActivity(SearchPlace place, ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		wordList = place.getWordList();
		period = place.getPeriod();
		timeSearchDate = place.getTimeSearchDate();
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		SearchView view = clientFactory.getSearchView();
		view.setWords(wordList);
		view.setPresenter(this);
		view.setPeriod(period);
		view.setTimeSearchDate(timeSearchDate);
		resizeTimer = new Timer() {

			@Override
			public void run() {
				if (wordDataTable != null) {
					clientFactory.getSearchView().getPeakBarChart().draw();
				}
				if (dataTable != null) {
					clientFactory.getSearchView().getTimeLine().draw(dataTable);
				}
				if (kookDataTable != null) {
					clientFactory.getSearchView().getKookBarChart().draw();
				}
				cancel();
			}
		};
		panel.setWidget(view.asWidget());
		if (dates.size() == 0) {
			getDates();
		}
		getWords();
		if (wordList.size() > 0) {
			view.getTimeLine().setVisible(true);
			drawTimeLines();
		} else {
			view.getTimeLine().setVisible(false);
		}
		if (timeSearchDate != null) {
			DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd");
			dateSelected(dtf.parse(timeSearchDate));
		}
		Window.addResizeHandler(new ResizeHandler() {

			public void onResize(ResizeEvent event) {
				resizeTimer.cancel();
				resizeTimer.schedule(250);
			}
		});
	}

	private void drawTimeLines() {
		Timer timer = new Timer() {
			public void run() {
				if (datesLoaded) {
					for (String word : wordList) {
						addTimeLine(word);
					}
					this.cancel();
				}
			}
		};
		timer.scheduleRepeating(1000);
	}

	public void addWordToSuggestOracle(String word) {
		clientFactory.getSearchView().getSuggestOracle().add(word);
	}

	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

	private class TimeLineCallback implements
			AsyncCallback<TreeMap<Date, WordAtDateData>> {

		private String word;

		public TimeLineCallback(String word) {
			this.word = word;
		}

		public void onFailure(Throwable caught) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSuccess(TreeMap<Date, WordAtDateData> result) {
			timeLineLoaded.put(word, true);
			for (Date date : result.keySet()) {
				timeLines.get(word).getFrequencies()
						.put(date, result.get(date));
			}
			GWT.log("Freqs loaded for word:" + word);

			if (!timeLineLoaded.containsValue(false)) {
				refreshGraph();
			}
		}

	}

	public void addTimeLine(String word) {
		timeLines.put(word, new TimeLine(word));
		// timeLines.put(word + "Four", new TimeLine(word + "Four", dates));
		timeLineLoaded.put(word, false);
		clientFactory.getEventBus().fireEvent(new LoadingTimelineEvent(word));
		clientFactory.getDataService().getTimeLine(word,
				new TimeLineCallback(word));
		// clientFactory.getDataService().getTimeLineFourier(word,
		// new TimeLineCallback(word + "Four"));
	}

	private void refreshGraph() {
		dataTable = DataTable.create();
		dataTable.addColumn(ColumnType.DATE, "Date", "0");
		for (String word : timeLines.keySet()) {
			wordToTableWordIDMap.put(word,
					dataTable.addColumn(ColumnType.NUMBER, word, word + "id"));
			// DataColumn dc = DataColumn.create(ColumnType.STRING,
			// RoleType.TOOLTIP);
			// wordToTableWordIDMap.put(word + "TT", dataTable.addColumn(dc));
			// wordToTableWordIDMap.put(
			// word + "MA",
			// dataTable.addColumn(ColumnType.NUMBER, word + "MA", word
			// + "MAid"));
			// wordToTableCommentIDMap.put(
			// word,
			// dataTable.addColumn(ColumnType.STRING, "annotation", word
			// + "annotid"));
		}
		Date firstDate = dates.get(0);
		CalendarUtil.addDaysToDate(firstDate, -1);
		lastDate = new Date();
		CalendarUtil.addDaysToDate(lastDate, 1);
		Date date = CalendarUtil.copyDate(firstDate);
		int totalDates = CalendarUtil.getDaysBetween(firstDate, lastDate) + 1;
		lastDate = dates.get(dates.size() - 1);

		dataTable.addRows(totalDates);

		for (int i = 0; i < totalDates; i++) {
			// date = dates.get(i);
			dataTable.setValue(i, 0, date);
			Float value = 0F;
			// Float maValue = 0F;
			Boolean peak = false;
			String comment = "";

			WordAtDateData wordDataObject;
			// WordAtDateData wordDataMA;
			for (String word : timeLines.keySet()) {
				// /wordDataMA = timeLines.get(word).getTest().get(date);
				// if (wordDataMA != null) {
				// maValue = wordDataMA.getFreq();
				// } else {
				// maValue = null;
				// }
				switch (period) {
				case "monthly":
					wordDataObject = timeLines.get(word).getMonthly().get(date);
					if (wordDataObject != null) {
						value = wordDataObject.getFreq();
						peak = wordDataObject.isPeak();
						comment = wordDataObject.getPeakComment();
					} else {
						value = null;
					}
					break;
				case "weekly":
					wordDataObject = timeLines.get(word).getWeekly().get(date);
					if (wordDataObject != null) {
						value = wordDataObject.getFreq();
						peak = wordDataObject.isPeak();
						comment = wordDataObject.getPeakComment();
					} else {
						value = null;
					}
					break;

				case "daily":
					wordDataObject = timeLines.get(word).getDaily().get(date);
					if (wordDataObject != null) {
						value = wordDataObject.getFreq();
						peak = wordDataObject.isPeak();
						comment = wordDataObject.getPeakComment();

					} else {
						value = null;
					}
					break;

				case "yearly":
					wordDataObject = timeLines.get(word).getYearly().get(date);
					if (wordDataObject != null) {
						value = wordDataObject.getFreq();
						peak = wordDataObject.isPeak();
						comment = wordDataObject.getPeakComment();

					} else {
						value = null;
					}
					break;

				default:
					wordDataObject = timeLines.get(word).getMonthly().get(date);
					if (wordDataObject != null) {
						value = wordDataObject.getFreq();
						peak = wordDataObject.isPeak();
						comment = wordDataObject.getPeakComment();
					} else {
						value = null;
					}
					break;

				}
				if (value != null) {
					/**
					 * Wenn Date in monthly/yearly/weekly liegt.
					 */
					dataTable
							.setValue(i, wordToTableWordIDMap.get(word), value);
					// dataTable.setValue(i,
					// wordToTableWordIDMap.get(word + "TT"), "TTT");
					// if (peak && annotations) {
					// dataTable.setValue(i,
					// wordToTableCommentIDMap.get(word), comment);
					// }
					// dataTable.setValue(i,
					// dataTable.getColumnIndex(word + "annotid"),
					// value.toString());
				} else {
					/*
					 * dataTable.setValue(i, wordToTableWordIDMap.get(word), 0 *
					 * 100);
					 */
				}
				// if (maValue != null) {
				// dataTable.setValue(i,
				// wordToTableWordIDMap.get(word + "MA"),
				// maValue * 100);
				// }
			}
			lastDate = CalendarUtil.copyDate(date);
			CalendarUtil.addDaysToDate(date, 1);
		}
		clientFactory.getSearchView().draw(dataTable,
				getTimelineChartOptions(), getChartRangeFilterOptions(),
				getChartRangeFilterState());
	}

	private ChartArea getChartArea() {
		if (chartArea == null) {
			chartArea = ChartArea.create();
			chartArea.setWidth("80%");
			chartArea.setHeight("90%");
		}
		return chartArea;
	}

	private ChartRangeFilterOptions getChartRangeFilterOptions() {
		ChartRangeFilterOptions chartRangeFilterOptions = ChartRangeFilterOptions
				.create();
		chartRangeFilterOptions.setFilterColumnIndex(0);
		LineChartOptions controlChartOptions = LineChartOptions.create();
		controlChartOptions.setHeight(50);
		controlChartOptions.setChartArea(chartArea);
		ChartRangeFilterUi chartRangeFilterUi = ChartRangeFilterUi.create();
		chartRangeFilterUi.setChartType(ChartType.LINE);
		chartRangeFilterUi.setChartOptions(controlChartOptions);
		chartRangeFilterUi.setMinRangeSize(2 * 24 * 60 * 60 * 1000);
		chartRangeFilterOptions.setUi(chartRangeFilterUi);
		return chartRangeFilterOptions;
	}

	private ChartRangeFilterState getChartRangeFilterState() {
		ChartRangeFilterStateRange stateRange = ChartRangeFilterStateRange
				.create();
		stateRange.setStart(dates.get(0));
		stateRange.setEnd(new Date());
		ChartRangeFilterState controlState = ChartRangeFilterState.create();
		controlState.setRange(stateRange);
		return controlState;
	}

	private LineChartOptions getTimelineChartOptions() {
		LineChartOptions options = LineChartOptions.create();
		options.setLineWidth(2);
		options.setInterpolateNulls(true);
		options.setLegend(Legend.create(LegendPosition.IN));
		options.setChartArea(getChartArea());
		VAxis vAxis = VAxis.create();
		vAxis.setFormat("#.###");
		options.setVAxis(vAxis);
		return options;
	}

	private void getWords() {
		clientFactory.getEventBus().fireEvent(new LoadingSearchWordsEvent());
		clientFactory.getDataService().getWords(
				new AsyncCallback<ArrayList<String>>() {
					public void onSuccess(ArrayList<String> result) {
						for (String word : result) {
							addWordToSuggestOracle(word);
						}
						GWT.log("Words loaded: " + result.size());
					}

					@Override
					public void onFailure(Throwable caught) {

					}
				});
	}

	private void getDates() {
		dates = datesInCache();
		if (dates.size() == 0) {
			clientFactory.getEventBus()
					.fireEvent(new LoadingSearchDatesEvent());
			clientFactory.getDataService().getDates(
					new AsyncCallback<ArrayList<Date>>() {
						public void onSuccess(ArrayList<Date> result) {
							dates = result;
							datesLoaded = true;
							GWT.log("Dates Loaded: " + dates.size());
						}

						@Override
						public void onFailure(Throwable caught) {

						}
					});
		}
	}

	private ArrayList<Date> datesInCache() {
		ArrayList<Date> datesInCache = new ArrayList<>();
		if (storage != null) {
			String datesString = storage.getItem("dates");

		}
		return datesInCache;
	}

	@Override
	public void addWord(String word) {
		addTimeLine(word);
	}

	@Override
	public void dateSelected(Date value, Integer timeSpan) {
		CalendarUtil.addDaysToDate(value, -timeSpan);
		Date startDate = CalendarUtil.copyDate(value);
		CalendarUtil.addDaysToDate(value, timeSpan * 2);
		Date endDate = CalendarUtil.copyDate(value);
		clientFactory.getDataService().getTopWordsAtDate(startDate, endDate,
				new AsyncCallback<Map<String, Integer>>() {
					public void onSuccess(Map<String, Integer> result) {
						GWT.log("loaded peak words: " + result.size());
						createPeakChart(result);
						// FlexTable flexTable = clientFactory.getSearchView()
						// .getTimeWordDataFlexTable();
						// flexTable.removeAllRows();
						// for (String word : result) {
						// int row = flexTable.getRowCount();
						// flexTable.setText(row, 0, word);
						// }
					}

					@Override
					public void onFailure(Throwable caught) {
					}
				});
	}

	protected void createPeakChart(Map<String, Integer> result) {
		wordDataTable = DataTable.create();
		wordDataTable.addColumn(ColumnType.STRING, "Word");
		wordDataTable.addColumn(ColumnType.NUMBER, "Rank");
		wordDataTable.addColumn(ColumnType.STRING, "Annot");
		wordDataTable.setColumnProperty(2, "role", "annotation");
		int i = 0;
		wordDataTable.addRows(result.keySet().size());
		for (String word : result.keySet()) {
			wordDataTable.setValue(i, 0, "");
			wordDataTable.setValue(i, 1, result.get(word));
			wordDataTable.setValue(i, 2, word);
			i++;
		}
		clientFactory.getSearchView().getPeakBarChart()
				.setDataTable(wordDataTable);
		clientFactory.getSearchView().getPeakBarChart()
				.setOptions(getPeakGraphOptions());
		clientFactory.getSearchView().getPeakBarChart().draw();
	}

	private BarChartOptions getPeakGraphOptions() {
		BarChartOptions options = BarChartOptions.create();
		options.setTitle("Top Words by Rank");
		options.setLegend(Legend.create(LegendPosition.TOP));
		options.setAxisTitlesPosition(AxisTitlesPosition.NONE);
		options.setChartArea(getChartArea());
		return options;
	}

	@Override
	public void dateSelected(Date value) {
		dateSelected(value, 15);
	}

	@Override
	public String getPeriod() {
		return period;
	}

	@Override
	public List<String> getWordList() {
		return wordList;
	}

	@Override
	public void dataSelected(JsArray<Selection> selections) {
		int row = selections.get(0).getRow();
		int col = selections.get(0).getColumn();
		String wort = dataTable.getColumnLabel(col);
		wordTableDate = dataTable.getValueDate(row, 0);
		clientFactory.getSearchView().getBigStackLayoutPanel().showWidget(1);
		loadWordTableData(wordTableDate, wort);
		// do something with word+dbname
		GWT.log("selected" + row + " : " + col + " wort:" + wort);
	}

	private void loadWordTableData(Date date, String wort) {
		wordTable = wort;
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyyMMdd");
		String dbName = "wdt" + dtf.format(date);
		dtf = DateTimeFormat.getFormat("yyyy-MM-dd");
		clientFactory.getSearchView().setLabelTableDate(dtf.format(date));
		FlexTable table = clientFactory.getSearchView().getFlexTableSent();
		table.removeAllRows();
		// table = clientFactory.getSearchView().getFlexTableKook();
		// table.removeAllRows();

		clientFactory.getDataService().getSentences(dbName, wort,
				new AsyncCallback<ArrayList<String[]>>() {
					public void onSuccess(ArrayList<String[]> result) {
						GWT.log("sentences: " + result.size());
						FlexTable table = clientFactory.getSearchView()
								.getFlexTableSent();
						for (String[] sentence : result) {
							table.setText(table.getRowCount(), 0, sentence[0]);
							table.setWidget(table.getRowCount() - 1, 1,
									new Anchor("Link", sentence[1]));
						}
					}

					@Override
					public void onFailure(Throwable caught) {

					}
				});
		clientFactory.getDataService().getKollokSimWords(dbName, wort,
				new AsyncCallback<Map<String, Integer>>() {

					@Override
					public void onSuccess(Map<String, Integer> result) {
						GWT.log("kook: " + result.size());
						// FlexTable table = clientFactory.getSearchView()
						// .getFlexTableKook();
						// for (String word : result.keySet()) {
						// table.setText(table.getRowCount(), 0, word);
						// table.setText(table.getRowCount() - 1, 1,
						// result.get(word) + "");
						// }
						creatKookGraph(result);
					}

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub

					}
				});
	}

	private void creatKookGraph(Map<String, Integer> result) {
		kookDataTable = DataTable.create();
		kookDataTable.addColumn(ColumnType.STRING, "Wort");
		kookDataTable.addColumn(ColumnType.NUMBER, "Signifikanz");
		kookDataTable.addColumn(ColumnType.STRING, "Annot");
		kookDataTable.setColumnProperty(2, "role", "annotation");
		int i = 0;
		kookDataTable.addRows(result.keySet().size());
		for (String word : result.keySet()) {
			kookDataTable.setValue(i, 0, "");
			kookDataTable.setValue(i, 1, result.get(word));
			kookDataTable.setValue(i, 2, word);
			i++;
		}
		clientFactory.getSearchView().getKookBarChart()
				.setDataTable(kookDataTable);
		clientFactory.getSearchView().getKookBarChart()
				.setOptions(getKookGraphOptions());
		clientFactory.getSearchView().getKookBarChart().draw();
	}

	private BarChartOptions getKookGraphOptions() {
		BarChartOptions options = BarChartOptions.create();
		options.setTitle("Top 15 cooccurrences");
		options.setLegend(Legend.create(LegendPosition.NONE));
		options.setAxisTitlesPosition(AxisTitlesPosition.NONE);
		options.setChartArea(getChartArea());
		return options;
	}

	@Override
	public String getTimeSearchDate() {
		return timeSearchDate;
	}

	@Override
	public void setTableDate(int i) {
		CalendarUtil.addDaysToDate(wordTableDate, i);
		loadWordTableData(this.wordTableDate, this.wordTable);
	}

	@Override
	public void peakWordSelected(JsArray<Selection> selections) {
		int row = selections.get(0).getRow();
		int col = selections.get(0).getColumn();
		String word = wordDataTable.getValueString(row, 2);
		clientFactory.getSearchView().addSearchWord(word);
	}

}
