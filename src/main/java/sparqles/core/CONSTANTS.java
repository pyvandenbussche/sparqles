package sparqles.core;

import java.net.URISyntaxException;

public class CONSTANTS {

	
	public static final String ATASK="ATask";
	public static final String PTASK="PTask";
	public static final String DTASK="DTask";
	public static final String FTASK="FTask";
	public static final String ITASK="ITask";
	public static Endpoint SPARQLES =null;
	static{
		try {
			SPARQLES = EndpointFactory.newEndpoint("http://sparqles.okfn.org/");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
}
