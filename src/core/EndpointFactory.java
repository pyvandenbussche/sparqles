package core;

import java.net.URI;
import java.net.URISyntaxException;

public class EndpointFactory {

	public static Endpoint newEndpoint(URI uri) {
		Endpoint ep = new Endpoint();
		ep.setUri(uri.toString());
		return ep;
	}

	public static Endpoint newEndpoint(String epURI) throws URISyntaxException {
		return newEndpoint(new URI(epURI));
		
	}

}
