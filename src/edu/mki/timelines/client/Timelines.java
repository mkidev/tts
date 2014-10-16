package edu.mki.timelines.client;

import java.util.ArrayList;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.web.bindery.event.shared.EventBus;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;

import edu.mki.timelines.client.mvp.AppActivityMapper;
import edu.mki.timelines.client.mvp.AppPlaceHistoryMapper;
import edu.mki.timelines.client.place.SearchPlace;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Timelines implements EntryPoint {

	private Place defaultPlace = new SearchPlace(new ArrayList<String>());
	private SimpleLayoutPanel appWidget = new SimpleLayoutPanel();

	public void onModuleLoad() {
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CONTROLS);
		chartLoader.loadApi(new Runnable() {
			public void run() {
				ClientFactory clientFactory = GWT.create(ClientFactory.class);
				EventBus eventBus = clientFactory.getEventBus();
				PlaceController placeController = clientFactory
						.getPlaceController();

				ActivityMapper activityMapper = new AppActivityMapper(
						clientFactory);
				ActivityManager activityManager = new ActivityManager(
						activityMapper, eventBus);
				activityManager.setDisplay(appWidget);

				AppPlaceHistoryMapper historyMapper = GWT
						.create(AppPlaceHistoryMapper.class);
				PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(
						historyMapper);
				historyHandler
						.register(placeController, eventBus, defaultPlace);

				RootLayoutPanel.get().add(appWidget);

				historyHandler.handleCurrentHistory();
			}
		});

	}
}
