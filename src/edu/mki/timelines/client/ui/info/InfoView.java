package edu.mki.timelines.client.ui.info;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import edu.mki.timelines.client.activity.InfoActivity;
import edu.mki.timelines.client.place.SearchPlace;

public class InfoView extends Composite {

	private static InfoViewUiBinder uiBinder = GWT
			.create(InfoViewUiBinder.class);

	interface InfoViewUiBinder extends UiBinder<Widget, InfoView> {
	}

	private InfoActivity presenter;

	public InfoView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setPresenter(InfoActivity infoActivity) {
		this.presenter = infoActivity;
	}

	@UiField
	Button timelineButton;

	@UiHandler("timelineButton")
	public void timelineButtonClicked(ClickEvent event) {
		presenter.goTo(new SearchPlace(new ArrayList<String>()));
	}

}
