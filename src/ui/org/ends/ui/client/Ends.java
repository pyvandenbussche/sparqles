package org.ends.ui.client;

import org.ends.ui.client.exception.EndsExceptionHandler;
import org.ends.ui.client.objects.GeneralInfo;
import org.ends.ui.client.page.MainPage;
import org.ends.ui.client.page.dimension.AvailabilityPage;
import org.ends.ui.client.page.dimension.DiscoverabilityPage;
import org.ends.ui.client.page.dimension.InteroperabilityPage;
import org.ends.ui.client.page.dimension.PerformancePage;
import org.ends.ui.client.service.EndsService;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class Ends extends HistoryManager implements EntryPoint,ICom{
	
	private GeneralInfo generalInfo = null;
	SimplePanel content=new SimplePanel();
	
	public void onModuleLoad() {
//		StyleInjector.inject(ResourceBundle.INSTANCE.stylesheet().getText());
		System.out.println("onModuleLoad()");
		RootPanel.get("posts").add(content);
		
		//load general information and load the screen
		EndsService.Util.getInstance().loadGeneralInfo(
				new AsyncCallback<GeneralInfo>(){

					public void onFailure(Throwable caught) {
						new EndsExceptionHandler(caught);
					}

					public void onSuccess(GeneralInfo generalInfo) {
						Ends.this.generalInfo=generalInfo;
						changeInstanceLastUpdateDate(generalInfo.getLastUpdateDate());
//						ChangeInstanceTitle(generalInfo.getTitle());
//						ChangeInstanceDescription(generalInfo.getDescription());
						
						//initialize the History Manager and set a default page
						initializeHistoryManager(PAGE_HOME);
					}
				});
	}
	
	
	/* Page Loading */
	protected void displayHomePage(){
		ChangeInstanceTitle(generalInfo.getTitle());
		ChangeInstanceDescription(generalInfo.getDescription());
		content.setWidget(new MainPage(this));
	}
	
	protected void displayDimensionPage(String dimension){
		if(dimension.equals(HistoryManager.DIMENSION_AVAILABILITY)){
			content.setWidget(new AvailabilityPage(this));
		}else if(dimension.equals(HistoryManager.DIMENSION_INTEROPERABILITY)){
			content.setWidget(new InteroperabilityPage(this));
		}else if(dimension.equals(HistoryManager.DIMENSION_DISCOVERABILITY)){
			content.setWidget(new DiscoverabilityPage(this));
		}else if(dimension.equals(HistoryManager.DIMENSION_PERFORMANCE)){
			content.setWidget(new PerformancePage(this));
		}
	}
	
	
	
	
	/* Icom */
	
	public void ChangeInstanceTitle(String html){
		RootPanel.get("instanceTitle").clear();
		RootPanel.get("instanceTitle").add(new HTML(html));
	}
	public void ChangeInstanceDescription(String html){
		RootPanel.get("instanceDescription").clear();
		RootPanel.get("instanceDescription").add(new HTML(html));
	}
	public void changeInstanceLastUpdateDate(String date){
		//TODO
		RootPanel.get("instanceDate").clear();
		RootPanel.get("instanceDate").add(new HTML("<time datetime='2010-09-06 22:22:38'>Last update: Monday 06 September 2010, 22:22</time>"));
		
	}
	
	
	/* Utility methods */
	
	public static Widget createLoadingWidget(){
		HorizontalPanel container = new HorizontalPanel();
		container.setWidth("100%");
		container.setHeight("100%");
	    Image loadingImg = new Image("images/loading.gif");
	    container.add(loadingImg);
	    container.setCellHorizontalAlignment(loadingImg, HasHorizontalAlignment.ALIGN_CENTER);
	    container.setCellVerticalAlignment(loadingImg, HasVerticalAlignment.ALIGN_MIDDLE);
	    return container;
	}
	
}
