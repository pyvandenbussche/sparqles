package org.ends.ui.client;

import org.ends.ui.client.exception.EndsExceptionHandler;
import org.ends.ui.client.objects.GeneralInfo;
import org.ends.ui.client.page.MainPage;
import org.ends.ui.client.service.EndsService;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
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
	private DockPanel main;
	SimplePanel content=new SimplePanel();
	
	public void onModuleLoad() {
//		StyleInjector.inject(ResourceBundle.INSTANCE.stylesheet().getText());
		System.out.println("onModuleLoad()");
		main = new DockPanel();
		main.setStyleName("cw-DockPanel");
		main.setSpacing(4);
		main.setHorizontalAlignment(DockPanel.ALIGN_CENTER);
		main.setWidth("100%");
		RootPanel.get("content").add(main);
		main.add(content, DockPanel.CENTER);
		main.setCellWidth(content, "100%");
		content.setWidget(new HTML("yo!"));
		
		//load general information and load the screen
		EndsService.Util.getInstance().loadGeneralInfo(
				new AsyncCallback<GeneralInfo>(){

					public void onFailure(Throwable caught) {
						new EndsExceptionHandler(caught);
					}

					public void onSuccess(GeneralInfo generalInfo) {
						Ends.this.generalInfo=generalInfo;
						HTML htmlTitle = new HTML(generalInfo.getTitle());
						RootPanel.get("EndsTitle").add(htmlTitle);
						HTML htmlDescription = new HTML(generalInfo.getDescription());
						RootPanel.get("EndsDescription").add(htmlDescription);
						
						//initialize the History Manager and set a default page
						initializeHistoryManager(PAGE_HOME);
					}
				});
	}
	
	protected void displayHomePage(){
		content.setWidget(new MainPage(this));
	}
	
	
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
