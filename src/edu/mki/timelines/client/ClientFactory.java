package edu.mki.timelines.client;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

import edu.mki.timelines.client.ui.info.InfoView;
import edu.mki.timelines.client.ui.search.SearchView;

public interface ClientFactory {
	EventBus getEventBus();

	PlaceController getPlaceController();

	SearchView getSearchView();

	TimeLineDataServiceAsync getDataService();

	InfoView getInfoView();

}
