package edu.mki.timelines.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import edu.mki.timelines.client.ClientFactory;
import edu.mki.timelines.client.place.InfoPlace;
import edu.mki.timelines.client.ui.info.InfoView;

public class InfoActivity extends AbstractActivity {

	private ClientFactory clientFactory;

	public InfoActivity(InfoPlace place, ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		InfoView view = clientFactory.getInfoView();
		view.setPresenter(this);
		panel.setWidget(view.asWidget());
	}

	public void goTo(Place place) {
		clientFactory.getPlaceController().goTo(place);
	}

}
