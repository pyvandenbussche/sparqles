package sparqles.core;

import java.net.URISyntaxException;

import sparqles.avro.Endpoint;

public class CONSTANTS {

	public static final String ATASK="ATask";
	public static final String PTASK="PTask";
	public static final String DTASK="DTask";
	public static final String FTASK="FTask";
	public static final String ITASK="ITask";
	public static final String ETASK="ETask";
	
	public static final String USER_AGENT = "SPARQLES client using HTTPClient/4.2.3 (https://github.com/pyvandenbussche/sparqles)";
	public static final String ARQ_USER_AGENT = "SPARQLES client using Apache-Jena-ARQ/2.11.1 (https://github.com/pyvandenbussche/sparqles)";
	public static final int SOCKET_TIMEOUT = 16*1000;
	public static final int CONNECTION_TIMEOUT = 16*1000;
	
	
	public static Endpoint SPARQLES =null;
	static{
		try {
			SPARQLES = EndpointFactory.newEndpoint("http://sparqles.okfn.org/");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
}
