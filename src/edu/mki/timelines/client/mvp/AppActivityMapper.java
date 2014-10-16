package edu.mki.timelines.client.mvp;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;

import edu.mki.timelines.client.ClientFactory;
import edu.mki.timelines.client.activity.InfoActivity;
import edu.mki.timelines.client.activity.SearchActivity;
import edu.mki.timelines.client.place.InfoPlace;
import edu.mki.timelines.client.place.SearchPlace;

public class AppActivityMapper implements ActivityMapper {

	private ClientFactory clientFactory;

	public AppActivityMapper(ClientFactory clientFactory) {
		super();
		this.clientFactory = clientFactory;
	}

	@Override
	public Activity getActivity(Place place) {
		if (place instanceof SearchPlace) {
			return new SearchActivity((SearchPlace) place, clientFactory);
		} else if (place instanceof InfoPlace) {
			return new InfoActivity((InfoPlace) place, clientFactory);
		}
		GWT.log("AppAcMap: no SearchPlace");
		return null;
	}

}
