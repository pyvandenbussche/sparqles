package org.ends.ui.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;



public class EndsException extends Exception implements IsSerializable{


	public EndsException() {
		super();
	}
	
	public EndsException(String msg) {
	    super(msg);
	}
}
