package edu.mki.timelines.client.ui.search;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.TextBoxBase;

public class MultipleTextBox extends TextBoxBase {
	private String seperator;

	/**
	 * Creates an empty multiple text box.
	 */
	public MultipleTextBox(String seperator) {
		this(Document.get().createTextInputElement(), "gwt-TextBox");
		this.seperator = seperator;
	}

	/**
	 * This constructor may be used by subclasses to explicitly use an existing
	 * element. This element must be an <input> element whose type is 'text'.
	 * 
	 * @param element
	 *            the element to be used
	 */
	protected MultipleTextBox(Element element) {
		super(element);
		assert InputElement.as(element).getType().equalsIgnoreCase("text");
	}

	MultipleTextBox(Element element, String styleName) {
		super(element);
		if (styleName != null) {
			setStyleName(styleName);
		}
	}

	@Override
	public String getText() {
		String wholeString = super.getText();
		String lastString = wholeString;
		if (wholeString != null && !wholeString.trim().equals(seperator)) {
			int lastSep = wholeString.trim().lastIndexOf(seperator);
			if (lastSep > 0) {
				lastString = wholeString.trim().substring(lastSep + 1);
			}
		}
		return lastString;
	}

	public String getString() {
		return super.getText();
	}

	@Override
	public void setText(String text) {
		String wholeString = super.getText();
		if (text != null && text.equals("")) {
			super.setText(text);
		} else {
			if (wholeString != null) {
				int lastSep = wholeString.trim().lastIndexOf(seperator);
				if (lastSep > 0) {
					wholeString = wholeString.trim().substring(0, lastSep);
				} else {
					wholeString = "";
				}

				if (!wholeString.trim().endsWith(seperator)
						&& !wholeString.trim().equals("")) {
					wholeString = wholeString + seperator;
				}

				wholeString = wholeString + text;
				super.setText(wholeString);
			}
		}
	}
}