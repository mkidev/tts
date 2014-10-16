package edu.mki.timelines.client.ui.search;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.googlecode.gwt.charts.client.ChartWrapper;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.controls.Dashboard;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterOptions;
import com.googlecode.gwt.charts.client.controls.filter.ChartRangeFilterState;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.corechart.LineChartOptions;

import edu.mki.timelines.client.activity.SearchPresenter;

public interface SearchView extends IsWidget {
	void setPresenter(SearchPresenter presenter);

	FlexTable getFlexTableSent();

	FlexTable getFlexTableKook();

	void setLabelTableDate(String date);

	StackLayoutPanel getBigStackLayoutPanel();

	void setDataTable(DataTable dataTable);

	void setDataTable(DataTable dataTable, LineChartOptions lcOptions,
			ChartRangeFilterOptions crfOptions, ChartRangeFilterState crfState);

	void draw(DataTable dataTable, LineChartOptions lcOptions,
			ChartRangeFilterOptions crfOptions, ChartRangeFilterState crfState);

	void setWords(List<String> wordList);

	MultiWordSuggestOracle getSuggestOracle();

	Dashboard getTimeLine();

	ChartWrapper<BarChartOptions> getKookBarChart();

	void addSearchWord(String word);

	void setPeriod(String period);

	// FlexTable getTimeWordDataFlexTable();
	void setTimeSearchDate(String date);

	ChartWrapper<BarChartOptions> getPeakBarChart();

}
