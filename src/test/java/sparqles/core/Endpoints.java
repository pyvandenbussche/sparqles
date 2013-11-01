package sparqles.core;



import java.net.URI;

import sparqles.avro.Endpoint;
import sparqles.core.EndpointFactory;

public class Endpoints {


	public static Endpoint AEMET, DBPEDIA, TEST, TEST1 ;
	static{
		try{
			AEMET = EndpointFactory.newEndpoint(new URI("http://aemet.linkeddata.es/sparql"));
			DBPEDIA = EndpointFactory.newEndpoint(new URI("http://dbpedia.org/sparql"));
			TEST = EndpointFactory.newEndpoint(new URI("http://www.rdfabout.com/sparql"));
			
			TEST1 = EndpointFactory.newEndpoint(new URI("http://ecowlim.tfri.gov.tw/sparql/query"));
		}catch(Exception e ){

		}
	}
}
