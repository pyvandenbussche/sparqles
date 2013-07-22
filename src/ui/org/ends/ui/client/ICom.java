package org.ends.ui.client;


public interface ICom {
//	public void requestSearchOffset(int offset);
	
	//HistoryManager
		public void requestPage(String page);
		public void requestPage(String page, String endpoint, String dimension);
}
