package org.ends.ui.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

/**
 * This class is designed to handle the history token (part of the URL after the '#')
 * 
 * @author VandenbusscheP
 */
public abstract class HistoryManager{
	
	
	private static String HIST_VarSep="&";
	private static String HIST_ValSep="=";
	private static String HIST_Page="p";
	private static String HIST_Endpoint="ep";
	private static String HIST_Dimension="dim";
	
	public static String PAGE_HOME="home";
	public static String PAGE_LOGIN="login";
	public static String PAGE_ABOUT="about";
	public static String PAGE_DIMENSION="dimension";
	public static String PAGE_ENDPOINT="endpoint";
	public static String PAGE_ADMIN="admin";
	
	public static String DIMENSION_DISCOVERABILITY="discov";
	public static String DIMENSION_INTEROPERABILITY="interop";
	public static String DIMENSION_PERFORMANCE="perf";
	public static String DIMENSION_AVAILABILITY="avail";
	
	protected String currentPage = PAGE_HOME;
	protected String currentEndpoint = null;
	protected String currentDimension = null;
	protected String currentAdminStep = null;
	

	/**
	 * Initialize the history manager
	 */
	public void initializeHistoryManager(final String defaultPage){
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
		      public void onValueChange(ValueChangeEvent<String> event) {
		        String historyToken = event.getValue();
		        
//		        System.out.println("HISTORY: "+historyToken);
		        currentEndpoint=null;
		        currentDimension=null;
		        currentAdminStep=null;
		        //TODO handle whatever admin step needed

		        // Parse the history token
		        if(historyToken!=null && historyToken.length()>0){
		        	String[] histVar = historyToken.split(HIST_VarSep);
		        	
		        	for(int i=0; i<histVar.length; i++){
		        		String var = histVar[i];
		        		if(var!=null && var.length()>0){
		        			String[] varVal = var.split(HIST_ValSep);
		        			if(varVal!=null && varVal.length==2){
			        			if(varVal[0].equals(HIST_Page)){
			        				if(varVal[1].length()>0){
			        					if(isAValidPage(varVal[1]))currentPage=varVal[1];
			        					else currentPage=PAGE_HOME;
			        				}
			        			}
			        			else if(varVal[0].equals(HIST_Endpoint) && varVal[1].trim().length()>0){
			        				currentEndpoint=varVal[1].trim();
			        			}
			        			else if(varVal[0].equals(HIST_Dimension)){
			        				if(varVal[1].length()>0){
			        					if(isAValidPage(varVal[1]))currentDimension=varVal[1];
			        					else currentDimension=null;
			        				}
			        				currentDimension=varVal[1].trim();
			        			}
			        		}
		        		}		        		
		        	}
		        	displayPage(currentPage,currentEndpoint,currentDimension);
		        }
		        else{
		        	requestPage(defaultPage);
		        }
		      }
		});
		History.fireCurrentHistoryState();   
	}
	
	
	/**
	 * Create the new history token
	 * @param page the page to display
	 */
	private void createHistoryToken(String page, String endpoint, String dimension){
		String token="";
		if(page!=null &&isAValidPage(page)){
			token+=HIST_Page+HIST_ValSep+page;
			if(endpoint!=null){
				token+=HIST_VarSep+HIST_Endpoint+HIST_ValSep+endpoint;
			}
			if(dimension!=null && isAValidDimension(dimension)){
				token+=HIST_VarSep+HIST_Dimension+HIST_ValSep+dimension;
			}

			History.newItem(token);
		}
	}
	
	public void requestPage(String page){
		requestPage(page, null, null);
	}
	
	public void requestPage(String page, String endpoint, String dimension){
		if(isAValidPage(page)){
			createHistoryToken(page, endpoint, dimension);
		}
	}
	
	
	private boolean isAValidPage(String pageVar){
		if(
				pageVar!=null
				&&!pageVar.equals(PAGE_LOGIN)
				&&!pageVar.equals(PAGE_HOME)
				&&!pageVar.equals(PAGE_DIMENSION)
				&&!pageVar.equals(PAGE_ENDPOINT)
				&&!pageVar.equals(PAGE_ADMIN)
				&&!pageVar.equals(PAGE_ABOUT)
				)return false;
		else return true;
	}
	private boolean isAValidDimension(String dimVar){
		if(
				dimVar!=null
				&&!dimVar.equals(DIMENSION_DISCOVERABILITY)
				&&!dimVar.equals(DIMENSION_INTEROPERABILITY)
				&&!dimVar.equals(DIMENSION_PERFORMANCE)
				&&!dimVar.equals(DIMENSION_AVAILABILITY)
				)return false;
		else return true;
	}
	
	private void displayPage(String page,String endpoint, String dimension){
		if(page!=null){
			if(page.equals(PAGE_LOGIN)){
				
			}
			else if(page.equals(PAGE_DIMENSION) && dimension!=null){
				if(isAValidDimension(dimension))displayDimensionPage(dimension);
				else createHistoryToken(PAGE_HOME,null,null);
			}
			else displayHomePage();
//			else if(page.equals(PAGE_VOCABS)){
//				if(action==null)displayVocabs();
//				else{
//					editVocab(currentSubject);
//				}
//			}
//			else if(page.equals(PAGE_VOCSPACES)){
//				if(action==null)displayVocabSpaces();
//				else{
//					editVocabSpace(currentSubject);
//				}
//			}
//			else if(page.equals(PAGE_AGENTS))displayAgents();
//			else if(page.equals(PAGE_LOVMETA))displayLOVMeta();
//			else if(page.equals(PAGE_USERS)){
//				if(action==null)displayUsers();
//				else{
//					editUser(currentSubject);
//				}
//			}
//			else if(page.equals(PAGE_SETTINGS))displaySettings();
		
		}
	}
	
	/**
	 * Reload the current page
	 * Used after a local change like URI modification
	 */
	protected void reDisplayPage(){
		displayPage(currentPage,currentEndpoint, currentDimension);
	}
	
	abstract void displayHomePage();
	abstract void displayDimensionPage(String dimension);
//	abstract void displayLogin();
//	abstract void displayHome();
//	abstract void displayVocabs();
//	abstract void displayVocabSpaces();
//	abstract void displayAgents();
//	abstract void displayLOVMeta();
//	abstract void displayUsers();
//	abstract void displaySettings();
//	abstract void displayProfile();
//	
//	abstract void editVocab(String vocabURI);
//	abstract void editVocabSpace(String vocabSpaceURI);
//	abstract void editUser(String userLogin);
}
