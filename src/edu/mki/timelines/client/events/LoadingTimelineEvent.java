package edu.mki.timelines.client.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

public class LoadingTimelineEvent extends GenericEvent {
	private String word;

	public LoadingTimelineEvent(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}

}
