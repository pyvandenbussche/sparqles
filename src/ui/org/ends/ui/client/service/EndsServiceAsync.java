package org.ends.ui.client.service;

import org.ends.ui.client.objects.GeneralInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;



public interface EndsServiceAsync {
	public void loadGeneralInfo(AsyncCallback<GeneralInfo> callback);
//	public void validateVocabulary(String vocabURI,boolean testIfAlreadyPresentInLOV,AsyncCallback<Vocabulary> callback) throws LOVException;
//	public void submitVocabulary(Vocabulary vocab, String email,AsyncCallback<Void> callback);
	
}
