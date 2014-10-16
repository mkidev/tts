package edu.mki.timelines.client.ui.search;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.googlecode.gwt.charts.client.ChartType;
import com.googlecode.gwt.charts.client.ChartWrapper;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.controls.Dashboard;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilter;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterOptions;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterState;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.corechart.LineChartOptions;
import com.googlecode.gwt.charts.client.event.SelectEvent;
import com.googlecode.gwt.charts.client.event.SelectHandler;

import edu.mki.timelines.client.activity.SearchPresenter;
import edu.mki.timelines.client.place.InfoPlace;
import edu.mki.timelines.client.place.SearchPlace;

public class SearchViewImpl extends Composite implements SearchView {
	private SearchPresenter presenter;
	private MultiWordSuggestOracle suggestOracle;
	private ListDataProvider<String> dataProvider;
	private MultipleTextBox multiTextBox;
	private Dashboard timeline;
	private ChartWrapper<LineChartOptions> timelineChart;
	private ChartRangeFilter numberRangeFilter;
	private ChartWrapper<BarChartOptions> kookBarChart;
	private ChartWrapper<BarChartOptions> peakBarChart;

	private static SearchViewImplUiBinder uiBinder = GWT
			.create(SearchViewImplUiBinder.class);

	interface SearchViewImplUiBinder extends UiBinder<Widget, SearchViewImpl> {
	}

	private void createTimeline() {
		if (timeline == null) {
			timeline = new Dashboard();
		}
		if (timelineChart == null) {
			timelineChart = new ChartWrapper<LineChartOptions>();
			timelineChart.setChartType(ChartType.LINE);
		}
		if (numberRangeFilter == null) {
			numberRangeFilter = new ChartRangeFilter();
		}
		timelinePanel.addNorth(timeline, 0);
		timelinePanel.addSouth(numberRangeFilter, 5);
		timelinePanel.add(timelineChart);
		timeline.bind(numberRangeFilter, timelineChart);

	}

	public SearchViewImpl() {
		suggestOracle = new MultiWordSuggestOracle();
		multiTextBox = new MultipleTextBox("+");
		this.searchBox = new SuggestBox(suggestOracle, multiTextBox);
		dataProvider = new ListDataProvider<String>();
		initWidget(uiBinder.createAndBindUi(this));
		dataProvider.addDataDisplay(getCellTable());
		createTimeline();
		createKookChart();
		createPeakChart();
		dateBox.setFormat(new DateBox.DefaultFormat(DateTimeFormat
				.getFormat("yyyy-MM-dd")));
	}

	private void createPeakChart() {
		if (peakBarChart == null) {
			peakBarChart = new ChartWrapper<>();
			peakBarChart.setChartType(ChartType.BAR);
			peakBarChart.addSelectHandler(new SelectHandler() {
				@Override
				public void onSelect(SelectEvent event) {
					presenter.peakWordSelected(peakBarChart.getSelection());
				}
			});
		}
		peakChartPanel.add(peakBarChart);
	}

	private void createKookChart() {
		if (kookBarChart == null) {
			kookBarChart = new ChartWrapper<BarChartOptions>();
			kookBarChart.setChartType(ChartType.BAR);
		}
		kookChartPanel.add(kookBarChart);
	}

	public ChartWrapper<BarChartOptions> getKookBarChart() {
		return kookBarChart;
	}

	public FlexTable getFlexTableKook() {
		return null;
		// return flexTableKook;
	}

	public FlexTable getFlexTableSent() {
		return flexTableSent;
	}

	@UiField
	Button infoButton;
	@UiField
	Button peakSearch;
	@UiField
	Button buttonDateLast;
	@UiField
	Button buttonDateNext;
	@UiField
	StackLayoutPanel bigStackLayoutPanel;
	@UiField
	DockLayoutPanel kookChartPanel;
	// @UiField
	// FlexTable flexTableKook;
	@UiField
	FlexTable flexTableSent;
	@UiField
	DockLayoutPanel timelinePanel;
	// @UiField
	// HTMLPanel htmlGoogleData;
	@UiField(provided = true)
	SuggestBox searchBox;
	@UiField
	Button addWordButton;
	@UiField
	Label labelTableDate;
	@UiField
	CellTable<String> wordCellTable;
	@UiField
	DateBox dateBox;
	@UiField
	IntegerBox dateAddSubBox;
	@UiField
	RadioButton radioWeekly;
	@UiField
	RadioButton radioDaily;
	@UiField
	RadioButton radioMonthly;
	@UiField
	RadioButton radioYearly;

	// @UiField
	// TabLayoutPanel topTabLayoutPanel;
	@UiField
	DockLayoutPanel peakChartPanel;

	// FlexTable timeWordDataFlexTable;

	// @UiField
	// TabLayoutPanel mainTabLayoutPanel;

	@UiFactory
	CellTable<String> createCellTable() {
		CellTable<String> cellTable = new CellTable<String>();
		TextColumn<String> wordColumn = new TextColumn<String>() {
			public String getValue(String object) {
				return object.toString();
			}
		};
		TextColumn<String> buttonColumn = new TextColumn<String>() {
			public String getValue(String object) {
				return "X";
			}
		};

		cellTable.addColumn(wordColumn);
		cellTable.addColumn(buttonColumn);
		cellTable.addCellPreviewHandler(new Handler<String>() {

			@Override
			public void onCellPreview(CellPreviewEvent<String> event) {
				if (BrowserEvents.CLICK
						.equals(event.getNativeEvent().getType())) {
					if (event.getColumn() == 1) {
						dataProvider.getList().remove(event.getIndex());
						if (!addWordButton.isEnabled()) {
							setButtonEnabled(true);
						}
						ArrayList<String> result = new ArrayList<>(dataProvider
								.getList());
						presenter.goTo(new SearchPlace(result, presenter
								.getPeriod(), presenter.getTimeSearchDate()));
					}
				}
			}

		});

		return cellTable;
	}

	public HasData<String> getCellTable() {
		return wordCellTable;
	}

	public MultiWordSuggestOracle getSuggestOracle() {
		return suggestOracle;
	}

	// @UiHandler("topTabLayoutPanel")
	// public void tabSelected(SelectionEvent<Integer> event) {
	// GWT.log("Tab select: " + event.getSelectedItem());
	// }
	//
	@UiHandler("infoButton")
	public void infoButtonPressend(ClickEvent event) {
		presenter.goTo(new InfoPlace());
	}

	@UiHandler("dateAddSubBox")
	public void dateAddSubBoxKeyPressed(KeyUpEvent event) {
		try {
			dateAddSubBox.getValueOrThrow();
			dateAddSubBox.setStyleName("");

		} catch (Exception e) {
			GWT.log("addboxerror---");
			dateAddSubBox.setStyleName("errorStyle");
		}
	}

	@UiHandler("buttonDateLast")
	public void buttonDateLastClicked(ClickEvent event) {
		presenter.setTableDate(-1);
	}

	@UiHandler("buttonDateNext")
	public void buttonDateNextClicked(ClickEvent event) {
		presenter.setTableDate(1);
	}

	@UiHandler("addWordButton")
	public void addWordButtonClicked(ClickEvent event) {
		String word = multiTextBox.getString();
		addSearchWord(word);
	}

	public void addSearchWord(String word) {
		if (dataProvider.getList().size() < 3) {
			List<String> dataProviderList = dataProvider.getList();
			if (dataProviderList.size() < 3) {
				dataProviderList.add(word);
			}
			if (dataProviderList.size() == 3) {
				setButtonEnabled(false);
			}
			ArrayList<String> result = new ArrayList<>(dataProviderList);
			presenter.goTo(new SearchPlace(result, presenter.getPeriod(),
					presenter.getTimeSearchDate()));
		}
	}

	private void periodChangedTo(String period) {
		ArrayList<String> result = new ArrayList<>(dataProvider.getList());
		presenter.goTo(new SearchPlace(result, period, presenter
				.getTimeSearchDate()));
	}

	@UiHandler("peakSearch")
	public void peakSearchClick(ClickEvent event) {

		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd");
		GWT.log("date changed" + dtf.format(dateBox.getValue()));
		try {
			// presenter.goTo(new SearchPlace(presenter.getWordList(), presenter
			// .getPeriod(), dtf.format(dateBox.getValue())));
			presenter.dateSelected(dateBox.getValue(),
					dateAddSubBox.getValueOrThrow());
		} catch (Exception e) {
			presenter.dateSelected(dateBox.getValue());

		}
	}

	@UiHandler("radioWeekly")
	public void radioWeeklyValueChanged(ValueChangeEvent<Boolean> event) {
		GWT.log("radio Weekly" + event.getValue());
		periodChangedTo("weekly");

	}

	@UiHandler("radioDaily")
	public void radioDailyValueChanged(ValueChangeEvent<Boolean> event) {
		GWT.log("radio dailyy" + event.getValue());
		periodChangedTo("daily");
	}

	@UiHandler("radioMonthly")
	public void radioMonthlyValueChanged(ValueChangeEvent<Boolean> event) {
		GWT.log("radio monthly" + event.getValue());
		periodChangedTo("monthly");
	}

	@UiHandler("radioYearly")
	public void radioYearlyValueChanged(ValueChangeEvent<Boolean> event) {
		GWT.log("radio Yearly" + event.getValue());
		periodChangedTo("yearly");
	}

	public void setPeriod(String period) {
		switch (period) {
		case "monthly":
			radioMonthly.setValue(true, false);
			break;
		case "weekly":
			radioWeekly.setValue(true, false);
			break;
		case "daily":
			radioDaily.setValue(true, false);
			break;
		case "yearly":
			radioYearly.setValue(true, false);
		default:
			radioMonthly.setValue(true, false);
			break;
		}
	}

	public void setButtonEnabled(boolean enabled) {
		addWordButton.setEnabled(enabled);
	}

	public Dashboard getTimeline() {
		return timeline;
	}

	@Override
	public void setDataTable(DataTable dataTable) {
		timeline.draw(dataTable);
	}

	@Override
	public void setDataTable(DataTable dataTable, LineChartOptions lcOptions,
			ChartRangeFilterOptions crfOptions, ChartRangeFilterState crfState) {
		timelineChart.setOptions(lcOptions);
		numberRangeFilter.setOptions(crfOptions);
		numberRangeFilter.setState(crfState);
	}

	@Override
	public void setPresenter(SearchPresenter searchPresenter) {
		this.presenter = searchPresenter;
		timelineChart.addSelectHandler(new SelectHandler() {
			public void onSelect(SelectEvent event) {
				presenter.dataSelected(timelineChart.getSelection());
			}
		});
	}

	@Override
	public void setWords(List<String> wordList) {
		dataProvider.getList().clear();
		for (String word : wordList) {
			dataProvider.getList().add(word);
		}
	}

	@Override
	public Dashboard getTimeLine() {
		return timeline;
	}

	@Override
	public ChartWrapper<BarChartOptions> getPeakBarChart() {
		return peakBarChart;
	}

	@Override
	public void draw(DataTable dataTable, LineChartOptions lcOptions,
			ChartRangeFilterOptions crfOptions, ChartRangeFilterState crfState) {
		setDataTable(dataTable, lcOptions, crfOptions, crfState);
		timeline.draw(dataTable);
	}

	@Override
	public StackLayoutPanel getBigStackLayoutPanel() {
		return bigStackLayoutPanel;
	}

	@Override
	public void setLabelTableDate(String date) {
		labelTableDate.setText(date);
	}

	@Override
	public void setTimeSearchDate(String date) {
		if (date.length() > 0) {
			try {
				DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd");
				dateBox.setValue(dtf.parse(date), false);
			} catch (Exception e) {

			}
		}
	}
}
