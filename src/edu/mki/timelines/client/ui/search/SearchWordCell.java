package edu.mki.timelines.client.ui.search;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import edu.mki.timelines.shared.model.SearchWord;

public class SearchWordCell extends AbstractCell<SearchWord> {

	public void render(com.google.gwt.cell.client.Cell.Context context,
			SearchWord value, SafeHtmlBuilder sb) {
		sb.appendEscaped(value.toString());
	}

}
