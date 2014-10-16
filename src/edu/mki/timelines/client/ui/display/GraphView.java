package edu.mki.timelines.client.ui.display;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;

public class GraphView extends Composite {

	private static GraphViewUiBinder uiBinder = GWT
			.create(GraphViewUiBinder.class);

	interface GraphViewUiBinder extends UiBinder<Widget, GraphView> {
	}

	public GraphView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField(provided = true)
	AnnotatedTimeLine timeline;

	public GraphView(String firstName) {
		timeline = new AnnotatedTimeLine("900px", "400px");
		initWidget(uiBinder.createAndBindUi(this));
	}

}
