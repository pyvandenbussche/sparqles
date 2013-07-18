package org.ends.ui.client.service;

import org.ends.ui.client.exception.EndsException;
import org.ends.ui.client.objects.GeneralInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface EndsService extends RemoteService {

	public static final String SERVICE_URI = "/EndsService";

	public static class Util {

		public static EndsServiceAsync getInstance() {

			EndsServiceAsync instance = (EndsServiceAsync) 
			GWT.create(EndsService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
	}
	
	public GeneralInfo loadGeneralInfo() throws EndsException;
//	
//	public Vocabulary validateVocabulary(String vocabURI,boolean testIfAlreadyPresentInLOV) throws LOVException;
//	
//	public void submitVocabulary(Vocabulary vocab, String email);
	
}
