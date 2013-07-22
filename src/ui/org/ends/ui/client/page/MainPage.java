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
		
		buildDiscoverability();
		buildInteroperability();
		buildPerformance();
		buildAvailability();
	}
	
	private void buildDiscoverability(){
		//build the header
		SimplePanel spHeader = new SimplePanel();
		HTML header = new HTML("Discoverability");
		spHeader.setStyleName("dimensionBoxHeader");
		spHeader.setWidget(header);
		this.setWidget(0, 1, spHeader);
		
		
		//build the footer (display a loading logo while fetching the data)
		SimplePanel spFooter = new SimplePanel();
		spFooter.setStyleName("dimensionBoxFooter");
		spFooter.setWidget(Ends.createLoadingWidget());
		this.setWidget(1, 1, spFooter);
		
		
		//add the click action
		ClickHandler clickHandler = new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {
	        		com.requestPage(HistoryManager.PAGE_DETAIL,null,HistoryManager.DIMENSION_DISCOVERABILITY);
	        }
	    };
		spHeader.sinkEvents(Event.ONCLICK);
		spHeader.setTitle("Discoverability Details");
		spHeader.addHandler(clickHandler, ClickEvent.getType());
		spFooter.sinkEvents(Event.ONCLICK);
		spFooter.setTitle("Discoverability Details");
		spFooter.addHandler(clickHandler, ClickEvent.getType());
	}
	
	private void buildInteroperability(){
		//build the header
		SimplePanel spHeader = new SimplePanel();
		HTML header = new HTML("Interoperability");
		spHeader.setStyleName("dimensionBoxHeader");
		spHeader.setWidget(header);
		this.setWidget(0, 3, spHeader);
		
		
		//build the footer (display a loading logo while fetching the data)
		SimplePanel spFooter = new SimplePanel();
		spFooter.setStyleName("dimensionBoxFooter");
		spFooter.setWidget(Ends.createLoadingWidget());
		this.setWidget(1, 3, spFooter);
		
		//add the click action
		ClickHandler clickHandler = new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {
	        		com.requestPage(HistoryManager.PAGE_DETAIL,null,HistoryManager.DIMENSION_INTEROPERABILITY);
	        }
	    };
		spHeader.sinkEvents(Event.ONCLICK);
		spHeader.setTitle("Interoperability Details");
		spHeader.addHandler(clickHandler, ClickEvent.getType());
		spFooter.sinkEvents(Event.ONCLICK);
		spFooter.setTitle("Interoperability Details");
		spFooter.addHandler(clickHandler, ClickEvent.getType());
	}
	
	private void buildPerformance(){
		//build the header
		SimplePanel spHeader = new SimplePanel();
		HTML header = new HTML("Performance");
		spHeader.setStyleName("dimensionBoxHeader");
		spHeader.setWidget(header);
		this.setWidget(3, 1, spHeader);
		
		
		//build the footer (display a loading logo while fetching the data)
		SimplePanel spFooter = new SimplePanel();
		spFooter.setStyleName("dimensionBoxFooter");
		spFooter.setWidget(Ends.createLoadingWidget());
		this.setWidget(4, 1, spFooter);
		
		
		//add the click action
		ClickHandler clickHandler = new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {
	        		com.requestPage(HistoryManager.PAGE_DETAIL,null,HistoryManager.DIMENSION_PERFORMANCE);
	        }
	    };
		spHeader.sinkEvents(Event.ONCLICK);
		spHeader.setTitle("Performance Details");
		spHeader.addHandler(clickHandler, ClickEvent.getType());
		spFooter.sinkEvents(Event.ONCLICK);
		spFooter.setTitle("Performance Details");
		spFooter.addHandler(clickHandler, ClickEvent.getType());
	}
	
	private void buildAvailability(){
		//build the header
		SimplePanel spHeader = new SimplePanel();
		HTML header = new HTML("Availability");
		spHeader.setStyleName("dimensionBoxHeader");
		spHeader.setWidget(header);
		this.setWidget(3, 3, spHeader);
		
		
		//build the footer (display a loading logo while fetching the data)
		SimplePanel spFooter = new SimplePanel();
		spFooter.setStyleName("dimensionBoxFooter");
		spFooter.setWidget(Ends.createLoadingWidget());
		this.setWidget(4, 3, spFooter);
		
		
		//add the click action
		ClickHandler clickHandler = new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {
	        		com.requestPage(HistoryManager.PAGE_DETAIL,null,HistoryManager.DIMENSION_AVAILABILITY);
	        }
	    };
		spHeader.sinkEvents(Event.ONCLICK);
		spHeader.setTitle("Availability Details");
		spHeader.addHandler(clickHandler, ClickEvent.getType());
		spFooter.sinkEvents(Event.ONCLICK);
		spFooter.setTitle("Availability Details");
		spFooter.addHandler(clickHandler, ClickEvent.getType());
	}
	
	
}
