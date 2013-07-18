package org.ends.ui.client;

import org.ends.ui.client.exception.EndsExceptionHandler;
import org.ends.ui.client.objects.GeneralInfo;
import org.ends.ui.client.service.EndsService;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

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
	
	private void displayHomePage(){
		
	}
	
//	private void buildFirstScreen(){
//		title.setWidget(new Label(""));
//		buttongrid.setWidget(0, 0, new Label());
//		buttongrid.setWidget(0, 1, new Label());
//		final Grid userGrid = new Grid(1,2);
//		userGrid.setWidth("100%");
//		DecoratorPanel decoRes = new DecoratorPanel();
//		decoRes.setWidget(userGrid);
//		content.setWidget(decoRes);
//		
//		Label vocabURILbl = new Label("New Vocabulary URI: ");
//		vocabURIBox = new TextBox();
////		vocabURIBox.setText("http://www.w3.org/2003/06/sw-vocab-status/ns");
//		vocabURIBox.setWidth("400px");
//		vocabURIBox.addKeyDownHandler(new KeyDownHandler() {
//		    @Override
//		    public void onKeyDown(KeyDownEvent event) {
//		        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//		        	if(vocabURIBox.getText()==null || vocabURIBox.getText().length()<5){
//		        		title.setWidget(new Label("please enter a valid vocabulary URI"));
//					}
//					else{
//						title.setWidget(new Label(""));
//					}
//		        }
//
//		    }
//
//		});
//		userGrid.setWidget(0, 0, vocabURILbl);
//		userGrid.setWidget(0, 1, vocabURIBox);
//		
//		Button btn_test = new Button("validate vocabulary");
//		btn_test.addClickHandler(new ClickHandler() {
//			
//			@Override
//			public void onClick(ClickEvent event) {
//				if(vocabURIBox.getText()==null || vocabURIBox.getText().length()<5){
//					title.setWidget(new Label("please enter a valid vocabulary URI"));
//				}
//				else{
//					title.setWidget(new Label(""));
//				}
//			}
//		});
//		buttongrid.setWidget(0, 0, btn_test);
//	}
	
	
}
