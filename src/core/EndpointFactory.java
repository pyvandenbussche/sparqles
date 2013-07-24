package core;

import java.net.URI;

public class EndpointFactory {

	public static Endpoint newEndpoint(URI uri) {
		Endpoint ep = new Endpoint();
		ep.setUri(uri.toString());
		return ep;
	}

}
