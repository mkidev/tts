package edu.mki.timelines.client.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

public class WordAddedEvent extends GenericEvent {
	private final String word;

	public WordAddedEvent(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}
}
