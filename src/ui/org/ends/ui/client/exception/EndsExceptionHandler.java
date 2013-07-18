package org.ends.ui.client.exception;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;


public class EndsExceptionHandler {
	
		
	public EndsExceptionHandler(Throwable thro){
		this(null,thro.getMessage());
	}
	
	public EndsExceptionHandler(String errorTitle, String errorDescription){
		final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
	    simplePopup.ensureDebugId("cwBasicPopup-simplePopup");
	    simplePopup.setWidth("150px");
	    simplePopup.setWidget(
	        new HTML(errorDescription));
	    simplePopup.center();
	    simplePopup.show();
	}

}
