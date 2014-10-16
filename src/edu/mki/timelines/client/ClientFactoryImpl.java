package edu.mki.timelines.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

import edu.mki.timelines.client.ui.info.InfoView;
import edu.mki.timelines.client.ui.search.SearchView;
import edu.mki.timelines.client.ui.search.SearchViewImpl;

public class ClientFactoryImpl implements ClientFactory {

	private EventBus eventBus = new SimpleEventBus();
	private PlaceController placeController = new PlaceController(eventBus);
	private SearchView searchView = new SearchViewImpl();
	private TimeLineDataServiceAsync dataService = GWT
			.create(TimeLineDataService.class);
	private InfoView infoView = new InfoView();

	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	@Override
	public SearchView getSearchView() {
		return searchView;
	}

	@Override
	public TimeLineDataServiceAsync getDataService() {
		return dataService;
	}

	@Override
	public InfoView getInfoView() {
		return infoView;
	}

}
