package edu.mki.timelines.shared.model;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.visualization.client.DataTable;

public class TimeLineDataTable extends DataTable {
	void addDates(ArrayList<Date> dates) {
		if (this.getNumberOfColumns() == 0) {
			this.addColumn(ColumnType.DATE, "Datum", "dates");
			this.addRows(dates.size());
			for (int i = 0; i < dates.size(); i++) {
				this.setValue(i, this.getColumnIndex("dates"), dates.get(i));
			}
		}
	}
}
