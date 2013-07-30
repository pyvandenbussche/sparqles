package org.ends.ui.client.page;

import org.ends.ui.client.Ends;
import org.ends.ui.client.HistoryManager;
import org.ends.ui.client.ICom;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;

public class MainPage  extends Grid{
	private ICom com;
	
	public MainPage(ICom com){
		super(5, 5);
		this.setCellSpacing(0);
		this.com=com;
		
//		buildDiscoverability();
//		buildInteroperability();
//		buildPerformance();
//		buildAvailability();
	}
	
	private void buildDiscoverability(){
		//build the box
		SimplePanel content = buildBox("Discoverability", "Discoverability Details", HistoryManager.DIMENSION_DISCOVERABILITY, 0, 1);
	}
	
	private void buildInteroperability(){
		//build the box
		SimplePanel content = buildBox("Interoperability", "Interoperability Details", HistoryManager.DIMENSION_INTEROPERABILITY, 0, 3);
	}
	
	private void buildPerformance(){
		//build the box
		SimplePanel content = buildBox("Performance", "Performance Details", HistoryManager.DIMENSION_PERFORMANCE, 3, 1);
	}
	
	private void buildAvailability(){
		//build the box
		SimplePanel content = buildBox("Availability", "Availability Details", HistoryManager.DIMENSION_AVAILABILITY, 3, 3);
	}
	
	private SimplePanel buildBox(String dimensionName, String dimensionInfoBox, final String historyDimension, int row, int column){
		//build the header
		SimplePanel spHeader = new SimplePanel();
		HTML header = new HTML("<table width=100%><tr><td style='width:50%; text-align:left; font-size: 16px;'>"+dimensionName+
				"</td><td style='width:50%; text-align:right; font-size: 16px;'>&rarr;</td></tr></table>");
		spHeader.setStyleName("dimensionBoxHeader");
		spHeader.setWidget(header);
		this.setWidget(row, column, spHeader);
		
		
		//build the footer (display a loading logo while fetching the data)
		SimplePanel spFooter = new SimplePanel();
		spFooter.setStyleName("dimensionBoxFooter");
		spFooter.setWidget(Ends.createLoadingWidget());
		this.setWidget(row+1, column, spFooter);
		
		
		//add the click action
		ClickHandler clickHandler = new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {
	        		com.requestPage(HistoryManager.PAGE_DIMENSION,null,historyDimension);
	        }
	    };
		spHeader.sinkEvents(Event.ONCLICK);
		spHeader.setTitle(dimensionInfoBox);
		spHeader.addHandler(clickHandler, ClickEvent.getType());
		spFooter.sinkEvents(Event.ONCLICK);
		spFooter.setTitle(dimensionInfoBox);
		spFooter.addHandler(clickHandler, ClickEvent.getType());
		
		return spFooter;
	}
}
