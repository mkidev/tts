package edu.mki.timelines.client.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

public class InfoPlace extends Place {
	public static class Tokenizer implements PlaceTokenizer<InfoPlace> {

		@Override
		public InfoPlace getPlace(String token) {
			return new InfoPlace();
		}

		@Override
		public String getToken(InfoPlace place) {
			return "";
		}
	}
}
