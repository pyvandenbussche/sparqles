package org.ends.ui.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

public class ArticlePanel extends ComplexPanel {
	/**
	 * Creates an empty section panel.
	 */

	public ArticlePanel() {
	 setElement(DOM.createElement("article"));
	}

	/**
	 * Adds a new child widget to the panel.
	 * 
	 * @param w
	 *            the widget to be added
	 */
	@Override
	public void add(Widget w) {
	    add(w, getElement());
	}
}
