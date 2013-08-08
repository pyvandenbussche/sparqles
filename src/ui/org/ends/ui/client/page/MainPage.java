package org.ends.ui.client.page;

import org.ends.ui.client.ArticlePanel;
import org.ends.ui.client.ICom;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class MainPage extends FlowPanel{
	private ICom com;
	
	public MainPage(ICom com){
		super();
		this.com=com;
		this.addStyleName("col-2 clear");
		buildAvailabilityBox();
		
//		buildDiscoverability();
//		buildInteroperability();
//		buildPerformance();
//		buildAvailability();
	}
	
	private void buildAvailabilityBox(){
		//build the box
		ArticlePanel articlePanel = new ArticlePanel();
		articlePanel.setStyleName("wrpl minWidth400");
		this.add(articlePanel);
		
		FlowPanel innerBox = new FlowPanel();
		innerBox.setStyleName("wrplInnerBox");
		innerBox.setWidth("100%");
		articlePanel.add(innerBox);
		innerBox.add(new HTML("<a href='#p=dimension&dim=avail'><h2>Availability</h2></a>"));
		HTML svg = new HTML("<svg id='avail'/>");
		svg.setHeight("100%");
		svg.setWidth("100%");
		innerBox.add(svg);
		createAvailChart();
	}
		
//		<div class="wrplInnerBox">
//		<a href="#p=dimension&dim=avail"><h2>Availability</h2></a>
//		<svg id="avail"/>
//		</div>
		
		
//		SimplePanel content = buildBox("Availability", "Availability Details", HistoryManager.DIMENSION_AVAILABILITY, 3, 3);
//	}
	
//	private void buildDiscoverabilityBox(){
//		//build the box
//		SimplePanel content = buildBox("Discoverability", "Discoverability Details", HistoryManager.DIMENSION_DISCOVERABILITY, 0, 1);
//		
//	}
//	
//	private void buildInteroperability(){
//		//build the box
//		SimplePanel content = buildBox("Interoperability", "Interoperability Details", HistoryManager.DIMENSION_INTEROPERABILITY, 0, 3);
//	}
//	
//	private void buildPerformance(){
//		//build the box
//		SimplePanel content = buildBox("Performance", "Performance Details", HistoryManager.DIMENSION_PERFORMANCE, 3, 1);
//	}
	
	
	
//	private SimplePanel buildBox(String dimensionName, String dimensionInfoBox, final String historyDimension, int row, int column){
//		//build the header
//		SimplePanel spHeader = new SimplePanel();
//		HTML header = new HTML("<table width=100%><tr><td style='width:50%; text-align:left; font-size: 16px;'>"+dimensionName+
//				"</td><td style='width:50%; text-align:right; font-size: 16px;'>&rarr;</td></tr></table>");
//		spHeader.setStyleName("dimensionBoxHeader");
//		spHeader.setWidget(header);
//		this.setWidget(row, column, spHeader);
//		
//		
//		//build the footer (display a loading logo while fetching the data)
//		SimplePanel spFooter = new SimplePanel();
//		spFooter.setStyleName("dimensionBoxFooter");
//		spFooter.setWidget(Ends.createLoadingWidget());
//		this.setWidget(row+1, column, spFooter);
//		
//		
//		//add the click action
//		ClickHandler clickHandler = new ClickHandler() {
//	        @Override
//	        public void onClick(ClickEvent event) {
//	        		com.requestPage(HistoryManager.PAGE_DIMENSION,null,historyDimension);
//	        }
//	    };
//		spHeader.sinkEvents(Event.ONCLICK);
//		spHeader.setTitle(dimensionInfoBox);
//		spHeader.addHandler(clickHandler, ClickEvent.getType());
//		spFooter.sinkEvents(Event.ONCLICK);
//		spFooter.setTitle(dimensionInfoBox);
//		spFooter.addHandler(clickHandler, ClickEvent.getType());
//		
//		return spFooter;
//	}
	
//	// call d3 with dom element & data
//		public static native void createBarchart(Element div, JsArrayNumber jsData)/*-{
//			$wnd.d3_barchart(div, jsData);
//		}-*/
		
		public static native void createAvailChart() /*-{
		  $wnd.createAvailChart("#avail");
		}-*/;
	
}
