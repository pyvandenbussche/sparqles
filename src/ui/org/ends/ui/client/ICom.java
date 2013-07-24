package org.ends.ui.client;

import com.google.gwt.user.client.ui.RootPanel;


public interface ICom {
//	public void requestSearchOffset(int offset);
	
	//HistoryManager
		public void requestPage(String page);
		public void requestPage(String page, String endpoint, String dimension);
		
		
	//RootPanel
		public RootPanel getDescriptionPanel();
}
